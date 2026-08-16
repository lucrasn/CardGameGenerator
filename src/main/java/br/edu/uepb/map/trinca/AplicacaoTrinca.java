package br.edu.uepb.map.trinca;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

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

        MotorTrinca motor = MotorTrinca.criar(jogadores);
        motor.adicionarListener(evento -> {
            if (evento instanceof JogadaRejeitada rejeitada) {
                io.exibir(CorTerminal.VERMELHO.aplicarComDestaque(
                        "Jogada rejeitada: " + rejeitada.motivo()));
            }
        });

        ResultadoDePartida resultado = motor.executar();
        exibirResultado(io, resultado, jogadores);
    }

    static List<Jogador> configurarJogadores(EntradaSaida io) {
        Objects.requireNonNull(io, "A entrada e saída não pode ser nula.");

        List<String> quantidades = IntStream.rangeClosed(
                        1, MotorTrinca.MAXIMO_DE_JOGADORES)
                .mapToObj(quantidade -> quantidade < MotorTrinca.MINIMO_DE_JOGADORES
                        ? "1 jogador — indisponível (mínimo: 2)"
                        : quantidade + " jogadores")
                .toList();
        int quantidadeDeJogadores;
        do {
            int escolhaDaQuantidade = io.solicitarOpcao(
                    "Digite a quantidade de jogadores:", quantidades);
            validarIndice(escolhaDaQuantidade, quantidades.size());
            quantidadeDeJogadores = escolhaDaQuantidade + 1;
            if (quantidadeDeJogadores < MotorTrinca.MINIMO_DE_JOGADORES) {
                io.exibir("A Trinca exige pelo menos dois jogadores.");
            }
        } while (quantidadeDeJogadores < MotorTrinca.MINIMO_DE_JOGADORES);

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
            EntradaSaida io, ResultadoDePartida resultado, List<Jogador> jogadores) {
        io.exibir(CorTerminal.AZUL_CELESTE.aplicarComDestaque(
                "\n" + "═".repeat(58)));
        if (resultado.vencedores().isEmpty()) {
            io.exibir("Partida encerrada sem vencedor.");
        } else {
            for (Jogador vencedor : resultado.vencedores()) {
                io.exibir(destacar(vencedor, "★ Vencedor: " + vencedor.nome()));
            }
        }
        io.exibir("Placar final:");
        for (Jogador jogador : jogadores) {
            io.exibir(destacar(
                    jogador,
                    "  " + jogador.nome() + ": "
                            + resultado.pontuacaoDe(jogador).orElseThrow() + " ponto(s)"));
        }
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
