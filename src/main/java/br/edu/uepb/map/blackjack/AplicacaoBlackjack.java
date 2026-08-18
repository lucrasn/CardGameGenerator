package br.edu.uepb.map.blackjack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import br.edu.uepb.map.cardgame.api.EntradaSaida;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.JogadorPadrao;
import br.edu.uepb.map.cardgame.api.ResultadoDePartida;
import br.edu.uepb.map.cardgame.api.io.ControleEntradaSaida;

/** Ponto de entrada do Blackjack básico entre uma pessoa e a casa. */
public final class AplicacaoBlackjack {

    private AplicacaoBlackjack() {
    }

    /**
     * Inicia a aplicação interativa no terminal padrão.
     *
     * @param args argumentos não utilizados
     */
    public static void main(String[] args) {
        ControleEntradaSaida io = new ControleEntradaSaida();
        apresentarBoasVindas(io);
        List<Jogador> participantes = criarParticipantes(io);
        Jogador jogador = participantes.getFirst();
        Jogador casa = participantes.getLast();
        Map<UUID, Integer> placarAcumulado = new LinkedHashMap<>();
        participantes.forEach(participante -> placarAcumulado.put(participante.id(), 0));

        int rodada = 0;
        boolean jogarNovamente;
        do {
            rodada++;
            MotorBlackjack motor = MotorBlackjack.criar(jogador, casa);
            motor.adicionarListener(new ApresentadorBlackjackConsole(io));
            ResultadoDePartida resultado = motor.executar();
            acumularPontuacao(placarAcumulado, resultado, participantes);
            exibirResultado(
                    io, motor, resultado, jogador, casa, placarAcumulado, rodada);
            jogarNovamente = desejaJogarNovamente(io);
        } while (jogarNovamente);

        io.exibir(CorTerminalBlackjack.CIANO.aplicarComDestaque(
                "\nObrigado por jogar Blackjack 21!"));
    }

    static List<Jogador> criarParticipantes(EntradaSaida entradaSaida) {
        Objects.requireNonNull(entradaSaida, "A entrada e saída não pode ser nula.");
        Jogador jogador = new JogadorPadrao(
                "Você", new DecisaoHumanaBlackjackConsole(entradaSaida));
        Jogador casa = new JogadorPadrao("Casa", new EstrategiaCasaBlackjack());
        return List.of(jogador, casa);
    }

    static void acumularPontuacao(
            Map<UUID, Integer> placarAcumulado,
            ResultadoDePartida resultado,
            List<Jogador> participantes) {
        Objects.requireNonNull(placarAcumulado, "O placar acumulado não pode ser nulo.");
        Objects.requireNonNull(resultado, "O resultado não pode ser nulo.");
        for (Jogador participante : List.copyOf(participantes)) {
            placarAcumulado.merge(
                    participante.id(),
                    resultado.pontuacaoDe(participante).orElseThrow(),
                    Integer::sum);
        }
    }

    static boolean desejaJogarNovamente(EntradaSaida entradaSaida) {
        Objects.requireNonNull(entradaSaida, "A entrada e saída não pode ser nula.");
        int escolha = entradaSaida.solicitarOpcao(
                "Deseja jogar outra rodada?",
                List.of("Sim, embaralhar novamente", "Não, encerrar"));
        if (escolha < 0 || escolha > 1) {
            throw new IllegalStateException(
                    "A entrada e saída retornou uma opção inválida.");
        }
        return escolha == 0;
    }

    private static void apresentarBoasVindas(EntradaSaida entradaSaida) {
        TelaBlackjack.apagar(entradaSaida);
        entradaSaida.exibir(CorTerminalBlackjack.CIANO.aplicarComDestaque(
                "♠ ♥ ♦ ♣  BLACKJACK 21 — PARTIDA EM CONSOLE  ♣ ♦ ♥ ♠\n"));
        entradaSaida.exibir("Chegue o mais perto possível de 21 sem ultrapassar.\n"
                + "O Ás vale 1 ou 11, e a casa deve comprar até alcançar 17.\n"
                + "Uma carta da casa ficará fechada enquanto você estiver jogando.");
        int escolha = entradaSaida.solicitarOpcao(
                "Quando estiver pronto:", List.of("Começar a rodada"));
        if (escolha != 0) {
            throw new IllegalStateException(
                    "A entrada e saída retornou uma opção inválida.");
        }
    }

    private static void exibirResultado(
            EntradaSaida entradaSaida,
            MotorBlackjack motor,
            ResultadoDePartida resultado,
            Jogador jogador,
            Jogador casa,
            Map<UUID, Integer> placarAcumulado,
            int rodada) {
        TelaBlackjack.apagar(entradaSaida);
        entradaSaida.exibir(CorTerminalBlackjack.VERDE.aplicarComDestaque(
                TelaBlackjack.SEPARADOR
                        + "\n  FIM DA RODADA " + rodada
                        + "\n" + TelaBlackjack.SEPARADOR));
        exibirMaoFinal(entradaSaida, "VOCÊ", CorTerminalBlackjack.CIANO,
                motor.maoFinalDe(jogador), motor.ultimaCartaCompradaPor(jogador));
        exibirMaoFinal(entradaSaida, "CASA", CorTerminalBlackjack.DOURADO,
                motor.maoFinalDe(casa), motor.ultimaCartaCompradaPor(casa));

        if (resultado.houveEmpate()) {
            entradaSaida.exibir(CorTerminalBlackjack.DOURADO.aplicarComDestaque(
                    "\n◆ Empate — ninguém pontua nesta rodada."));
        } else if (resultado.vencedorUnico()
                .filter(vencedor -> vencedor.id().equals(jogador.id()))
                .isPresent()) {
            entradaSaida.exibir(CorTerminalBlackjack.VERDE.aplicarComDestaque(
                    "\n★ Você venceu a rodada!"));
        } else {
            entradaSaida.exibir(CorTerminalBlackjack.VERMELHO.aplicarComDestaque(
                    "\nA casa venceu a rodada."));
        }
        entradaSaida.exibir("Motivo: " + descricaoDoMotivo(resultado) + ".");
        entradaSaida.exibir("\nPlacar acumulado:"
                + "\n  Você: " + placarAcumulado.get(jogador.id())
                + "\n  Casa: " + placarAcumulado.get(casa.id()) + "\n");
    }

    private static void exibirMaoFinal(
            EntradaSaida entradaSaida,
            String titulo,
            CorTerminalBlackjack cor,
            List<CartaBlackjack> mao,
            java.util.Optional<CartaBlackjack> ultimaCarta) {
        PontuacaoDaMaoBlackjack pontuacao = PontuacaoDaMaoBlackjack.calcular(mao);
        entradaSaida.exibir("\n" + cor.aplicarComDestaque(titulo)
                + "  " + TelaBlackjack.formatarMao(mao, ultimaCarta)
                + "\nTotal: " + cor.aplicarComDestaque(
                        TelaBlackjack.formatarPontuacao(pontuacao)));
    }

    private static String descricaoDoMotivo(ResultadoDePartida resultado) {
        return resultado.motivo() instanceof MotivoBlackjack motivo
                ? motivo.descricao()
                : resultado.motivo().toString();
    }
}
