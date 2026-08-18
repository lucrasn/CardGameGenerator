package br.edu.uepb.map.trinca;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import br.edu.uepb.map.cardgame.api.EntradaSaida;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.JogadorPadrao;
import br.edu.uepb.map.cardgame.api.ResultadoDePartida;
import br.edu.uepb.map.cardgame.api.evento.JogadaRejeitada;
import br.edu.uepb.map.cardgame.api.io.ControleEntradaSaida;

/** Ponto de entrada da demonstração entre jogadores humanos em console. */
public final class AplicacaoTrinca {

    private AplicacaoTrinca() {
    }

    public static void main(String[] args) {
        ControleEntradaSaida io = new ControleEntradaSaida();
        io.exibir(CorTerminal.AZUL_CELESTE.aplicarComDestaque(
                "\n♠ ♥ ♦ ♣  TRINCA — PARTIDA EM CONSOLE  ♣ ♦ ♥ ♠\n"));
        List<Jogador> jogadores = configurarJogadores(io);
        Map<UUID, Integer> placarAcumulado = new LinkedHashMap<>();
        jogadores.forEach(jogador -> placarAcumulado.put(jogador.id(), 0));

        int rodada = 0;
        boolean jogarNovamente;
        do {
            rodada++;
            ResultadoDePartida resultado = executarRodada(io, jogadores);
            acumularPontuacao(placarAcumulado, resultado, jogadores);
            TelaTerminal.apagar(io);
            exibirResultado(io, resultado, jogadores, placarAcumulado, rodada);
            jogarNovamente = desejaJogarNovamente(io);
        } while (jogarNovamente);
    }

    private static ResultadoDePartida executarRodada(
            EntradaSaida io, List<Jogador> jogadores) {
        MotorTrinca motor = MotorTrinca.criar(jogadores);
        motor.adicionarListener(evento -> {
            if (evento instanceof JogadaRejeitada rejeitada) {
                io.exibir(CorTerminal.VERMELHO.aplicarComDestaque(
                        "Jogada rejeitada: " + rejeitada.motivo()));
            }
        });
        return motor.executar();
    }

    static List<Jogador> configurarJogadores(ControleEntradaSaida io) {
        Objects.requireNonNull(io, "A entrada e saída não pode ser nula.");
        int quantidade = io.solicitarInteiro(
                "Digite a quantidade de jogadores (mínimo 2 e máximo 5):",
                MotorTrinca.MINIMO_DE_JOGADORES,
                MotorTrinca.MAXIMO_DE_JOGADORES);
        return configurarJogadores(io, quantidade);
    }

    static List<Jogador> configurarJogadores(
            EntradaSaida io, int quantidadeDeJogadores) {
        Objects.requireNonNull(io, "A entrada e saída não pode ser nula.");
        if (quantidadeDeJogadores < MotorTrinca.MINIMO_DE_JOGADORES
                || quantidadeDeJogadores > MotorTrinca.MAXIMO_DE_JOGADORES) {
            throw new IllegalArgumentException(
                    "A quantidade deve estar entre dois e cinco jogadores.");
        }

        List<Jogador> jogadores = new ArrayList<>(quantidadeDeJogadores);
        List<CorTerminal> cores = CorTerminal.coresDeJogador();

        for (int indice = 0; indice < quantidadeDeJogadores; indice++) {
            String nome = "Jogador " + (indice + 1);
            CorTerminal cor = cores.get(indice);
            jogadores.add(new JogadorPadrao(
                    nome, new DecisaoHumanaTrincaConsole(io, cor)));
        }
        return List.copyOf(jogadores);
    }

    private static void exibirResultado(
            EntradaSaida io,
            ResultadoDePartida resultado,
            List<Jogador> jogadores,
            Map<UUID, Integer> placarAcumulado,
            int rodada) {
        io.exibir(CorTerminal.AZUL_CELESTE.aplicarComDestaque(
                "\n" + "═".repeat(58)));
        if (resultado.vencedores().isEmpty()) {
            io.exibir("Partida encerrada sem vencedor.");
        } else {
            for (Jogador vencedor : resultado.vencedores()) {
                io.exibir(destacar(vencedor, "★ Vencedor: " + vencedor.nome()));
            }
        }
        io.exibir("Placar acumulado após " + rodada
                + (rodada == 1 ? " rodada:" : " rodadas:"));
        for (Jogador jogador : jogadores) {
            io.exibir(destacar(
                    jogador,
                    "  " + jogador.nome() + ": "
                            + placarAcumulado.get(jogador.id()) + " ponto(s)"));
        }
    }

    static void acumularPontuacao(
            Map<UUID, Integer> placarAcumulado,
            ResultadoDePartida resultado,
            List<Jogador> jogadores) {
        Objects.requireNonNull(placarAcumulado, "O placar acumulado não pode ser nulo.");
        Objects.requireNonNull(resultado, "O resultado não pode ser nulo.");
        for (Jogador jogador : List.copyOf(jogadores)) {
            placarAcumulado.merge(
                    jogador.id(), resultado.pontuacaoDe(jogador).orElseThrow(), Integer::sum);
        }
    }

    static boolean desejaJogarNovamente(EntradaSaida io) {
        Objects.requireNonNull(io, "A entrada e saída não pode ser nula.");
        List<String> opcoes = List.of("Sim, jogar outra rodada", "Não, encerrar");
        int escolha = io.solicitarOpcao("Deseja jogar novamente?", opcoes);
        validarIndice(escolha, opcoes.size());
        return escolha == 0;
    }

    private static String destacar(Jogador jogador, String texto) {
        if (jogador.estrategiaDeDecisao() instanceof DecisaoHumanaTrincaConsole decisao) {
            return decisao.destacar(texto);
        }
        return texto;
    }

    private static void validarIndice(int indice, int quantidadeDeOpcoes) {
        if (indice < 0 || indice >= quantidadeDeOpcoes) {
            throw new IllegalStateException(
                    "A entrada e saída retornou uma opção inválida.");
        }
    }
}
