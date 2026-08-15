package br.edu.uepb.map.cardgame.api;

/**
 * Motivos de encerramento comuns a diferentes jogos de cartas.
 *
 * <p>Implementação pronta de {@link MotivoDeEncerramento} para os casos que se repetem
 * em praticamente qualquer jogo. Um jogo com vocabulário próprio não precisa estender
 * este enum — declara o seu e continua compatível com o framework.
 *
 * @author Lucas N. de Araújo
 * @version 0.0.1
 * @since 2026-06-15
 */
public enum MotivoPadrao implements MotivoDeEncerramento {

    /** Uma condição de vitória foi satisfeita. */
    VITORIA(true, false),

    /** A regra declarou empate, com ou sem participantes empatados. */
    EMPATE(false, true),

    /** A execução não pôde continuar por esgotamento de recursos de cartas. */
    ESGOTAMENTO(false, false),

    /** A execução foi abandonada antes de uma vitória. */
    ABANDONO(false, false);

    private final boolean vitoria;
    private final boolean empate;

    MotivoPadrao(boolean vitoria, boolean empate) {
        this.vitoria = vitoria;
        this.empate = empate;
    }

    @Override
    public boolean ehVitoria() {
        return vitoria;
    }

    @Override
    public boolean ehEmpate() {
        return empate;
    }
}
