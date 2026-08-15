package br.edu.uepb.map.cardgame.api;

/**
 * Explica e classifica por que uma partida terminou.
 *
 * <p>É uma <strong>interface</strong>, e não um enum fechado, porque motivos de
 * encerramento variam entre jogos: um Blackjack quer distinguir "estourou 21" de
 * "rendição"; um Uno, "ficou sem cartas". Um enum obrigaria a editar o framework a
 * cada jogo novo — violação frontal do princípio Aberto/Fechado. Os motivos comuns
 * ficam em {@link MotivoPadrao}; jogos declaram os seus implementando esta interface.
 *
 * <p>Os dois métodos têm implementação padrão que devolve {@code false}: um motivo
 * novo não é vitória nem empate até que o jogo diga o contrário.
 *
 * @author Lucas N. de Araújo
 * @version 0.0.1
 * @since 2026-06-15
 */
public interface MotivoDeEncerramento {

    /**
     * @return {@code true} quando o motivo representa uma vitória e exige vencedor
     */
    default boolean ehVitoria() {
        return false;
    }

    /**
     * @return {@code true} quando o motivo representa um empate explícito
     */
    default boolean ehEmpate() {
        return false;
    }
}
