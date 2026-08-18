package br.edu.uepb.map.blackjack;

import java.util.List;
import java.util.Objects;

import br.edu.uepb.map.cardgame.api.ContextoDeDecisao;
import br.edu.uepb.map.cardgame.api.EntradaSaida;
import br.edu.uepb.map.cardgame.api.EstrategiaDeDecisao;
import br.edu.uepb.map.cardgame.api.Jogada;

/** Strategy humana que apresenta a mesa pública e lê uma ação no terminal. */
public final class DecisaoHumanaBlackjackConsole implements EstrategiaDeDecisao {

    private final EntradaSaida entradaSaida;
    private final CorTerminalBlackjack cor;

    /**
     * Cria uma decisão humana com a cor padrão do jogador.
     *
     * @param entradaSaida porta usada para interação
     * @throws NullPointerException se a porta for nula
     */
    public DecisaoHumanaBlackjackConsole(EntradaSaida entradaSaida) {
        this(entradaSaida, CorTerminalBlackjack.CIANO);
    }

    DecisaoHumanaBlackjackConsole(
            EntradaSaida entradaSaida, CorTerminalBlackjack cor) {
        this.entradaSaida = Objects.requireNonNull(
                entradaSaida, "A entrada e saída não pode ser nula.");
        this.cor = Objects.requireNonNull(cor, "A cor não pode ser nula.");
    }

    /**
     * Renderiza apenas as informações autorizadas e solicita uma ação válida.
     *
     * @param contextoBase contexto especializado do Blackjack
     * @return ação escolhida
     * @throws NullPointerException se o contexto for nulo
     * @throws IllegalArgumentException se o contexto não representar o jogador
     * @throws IllegalStateException se não houver ações ou a porta devolver um índice
     *         fora das opções apresentadas
     */
    @Override
    public Jogada decidir(ContextoDeDecisao contextoBase) {
        Objects.requireNonNull(contextoBase, "O contexto não pode ser nulo.");
        if (!(contextoBase instanceof ContextoDecisaoBlackjack contexto)) {
            throw new IllegalArgumentException("A decisão exige um contexto do Blackjack.");
        }
        if (contexto.papel() != PapelBlackjack.JOGADOR) {
            throw new IllegalArgumentException("A decisão humana esperava o papel de jogador.");
        }
        if (contexto.jogadasPermitidas().isEmpty()) {
            throw new IllegalStateException("Não há ações disponíveis para o jogador.");
        }
        List<String> opcoes = contexto.jogadasPermitidas().stream()
                .map(DecisaoHumanaBlackjackConsole::descricaoDaAcao)
                .toList();

        TelaBlackjack.apagar(entradaSaida);
        entradaSaida.exibir(cor.aplicarComDestaque(
                TelaBlackjack.SEPARADOR
                        + "\n  BLACKJACK 21 — TURNO " + contexto.numeroDoTurno()
                        + "\n" + TelaBlackjack.SEPARADOR));
        entradaSaida.exibir(TelaBlackjack.montarMesaDoJogador(contexto, cor));
        if (contexto.pontuacao().atingiuVinteEUm()) {
            entradaSaida.exibir(CorTerminalBlackjack.VERDE.aplicarComDestaque(
                    "\nVocê chegou a 21. Agora é só parar."));
        }

        int indice = entradaSaida.solicitarOpcao("\nO que você deseja fazer?", opcoes);
        if (indice < 0 || indice >= contexto.jogadasPermitidas().size()) {
            throw new IllegalStateException(
                    "A entrada e saída retornou uma opção inválida.");
        }
        return contexto.jogadasPermitidas().get(indice);
    }

    private static String descricaoDaAcao(Jogada jogada) {
        if (jogada instanceof AcaoBlackjack acao) {
            return acao.descricao();
        }
        throw new IllegalStateException("O contexto contém uma ação alheia ao Blackjack.");
    }
}
