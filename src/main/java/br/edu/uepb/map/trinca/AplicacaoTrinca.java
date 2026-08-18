package br.edu.uepb.map.trinca;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

import br.edu.uepb.map.cardgame.api.EntradaSaida;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.JogadorPadrao;
import br.edu.uepb.map.cardgame.api.ResultadoDePartida;
import br.edu.uepb.map.cardgame.api.evento.JogadaRejeitada;
import br.edu.uepb.map.cardgame.api.io.ControleEntradaSaida;

/**
 * Ponto de entrada da aplicação Trinca executada no console.
 *
 * <p>A classe configura os jogadores, inicia rodadas por meio do
 * {@link MotorTrinca} e mantém o placar acumulado entre as partidas.</p>
 *
 * @author Raffael Wagner Rolim Siqueira
 * @version 0.0.1
 */
public final class AplicacaoTrinca {

    private AplicacaoTrinca() {
    }

    /**
     * Inicia a aplicação interativa da Trinca.
     *
     * @param args argumentos da linha de comando; não são utilizados
     */
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

/**
 * Configura uma lista de jogadores com nomes padrão e decisões humanas via console.
 *
 * <p>Cada jogador recebe um nome no formato {@code "Jogador N"} e uma cor
 * terminal distinta para destacar suas interações no console.</p>
 *
 * @param io                    componente de entrada e saída utilizado para interação
 * @param quantidadeDeJogadores número de jogadores a configurar; deve estar entre
 *                              {@link MotorTrinca#MINIMO_DE_JOGADORES} e
 *                              {@link MotorTrinca#MAXIMO_DE_JOGADORES}
 * @return lista imutável de jogadores configurados
 * @throws NullPointerException     se {@code io} for {@code null}
 * @throws IllegalArgumentException se {@code quantidadeDeJogadores} estiver fora do intervalo permitido
 */
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

/**
 * Exibe o resultado da partida e o placar acumulado até a rodada corrente.
 *
 * <p>Lista os vencedores da rodada (ou informa ausência de vencedor) e
 * em seguida imprime a pontuação acumulada de cada jogador.</p>
 *
 * @param io              componente de entrada e saída utilizado para exibição
 * @param resultado       resultado da partida recém-encerrada
 * @param jogadores       lista de jogadores participantes, na ordem de exibição
 * @param placarAcumulado mapa de {@link UUID} do jogador para pontuação total acumulada
 * @param rodada          número sequencial da rodada recém-encerrada
 */
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

    /**
 * Acumula a pontuação obtida na partida ao placar total de cada jogador.
 *
 * <p>Para cada jogador presente em {@code jogadores}, soma a pontuação
 * retornada por {@link ResultadoDePartida#pontuacaoDe(Jogador)} ao valor
 * já registrado em {@code placarAcumulado}.</p>
 *
 * @param placarAcumulado mapa de {@link UUID} do jogador para pontuação acumulada;
 *                        será modificado in-place
 * @param resultado       resultado da partida cujas pontuações serão somadas
 * @param jogadores       lista de jogadores cujos pontos devem ser acumulados
 * @throws NullPointerException   se {@code placarAcumulado} ou {@code resultado} forem {@code null}
 * @throws NoSuchElementException se algum jogador não possuir pontuação registrada no resultado
 */
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

    /**
 * Pergunta ao usuário se deseja iniciar uma nova rodada.
 *
 * @param io componente de entrada e saída utilizado para solicitar a resposta
 * @return {@code true} se o usuário optar por jogar novamente; {@code false} caso contrário
 * @throws NullPointerException  se {@code io} for {@code null}
 * @throws IllegalStateException se o componente de E/S retornar um índice de opção inválido
 */
    static boolean desejaJogarNovamente(EntradaSaida io) {
        Objects.requireNonNull(io, "A entrada e saída não pode ser nula.");
        List<String> opcoes = List.of("Sim, jogar outra rodada", "Não, encerrar");
        int escolha = io.solicitarOpcao("Deseja jogar novamente?", opcoes);
        validarIndice(escolha, opcoes.size());
        return escolha == 0;
    }

    /**
 * Aplica o destaque de cor ao texto caso o jogador utilize decisão humana via console.
 *
 * <p>Se a estratégia de decisão do jogador não for uma instância de
 * {@link DecisaoHumanaTrincaConsole}, o texto é retornado sem modificação.</p>
 *
 * @param jogador jogador cujo estilo de cor será aplicado
 * @param texto   texto a ser formatado
 * @return texto formatado com a cor do jogador, ou o texto original sem formatação
 */
    private static String destacar(Jogador jogador, String texto) {
        if (jogador.estrategiaDeDecisao() instanceof DecisaoHumanaTrincaConsole decisao) {
            return decisao.destacar(texto);
        }
        return texto;
    }

    /**
 * Valida se o índice selecionado está dentro dos limites de uma lista de opções.
 *
 * @param indice               índice retornado pelo componente de entrada e saída
 * @param quantidadeDeOpcoes   número total de opções disponíveis
 * @throws IllegalStateException se {@code indice} for negativo ou maior ou igual a
 *                               {@code quantidadeDeOpcoes}
 */
    private static void validarIndice(int indice, int quantidadeDeOpcoes) {
        if (indice < 0 || indice >= quantidadeDeOpcoes) {
            throw new IllegalStateException(
                    "A entrada e saída retornou uma opção inválida.");
        }
    }
}
