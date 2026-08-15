package br.edu.uepb.map.cardgame.api;

/**
 * Diretiva declarativa devolvida pelo jogo após um turno válido.
 *
 * <p>O jogo escolhe o efeito; somente o engine altera a ordem de turnos.
 *
 * @param repetirJogador mantém o participante atual no próximo turno
 * @param inverterSentido inverte a rotação antes do próximo avanço
 * @param jogadoresAPular quantidade de participantes a saltar no próximo avanço
 *
 * @author Lucas N. de Araújo
 * @version 0.0.1
 * @since 2026-06-15
 */
public record ResultadoDoTurno(boolean repetirJogador,
                               boolean inverterSentido,
                               int jogadoresAPular) {

    /** Valida combinações incompatíveis. */
    public ResultadoDoTurno {
        if (jogadoresAPular < 0) {
            throw new IllegalArgumentException("A quantidade de jogadores a pular não pode ser negativa.");
        }
        if (repetirJogador && jogadoresAPular > 0) {
            throw new IllegalArgumentException("Não é possível repetir o jogador e pular participantes.");
        }
    }

    /** @return diretiva de avanço normal */
    public static ResultadoDoTurno avancar() {
        return new ResultadoDoTurno(false, false, 0);
    }

    /** @return diretiva que mantém o mesmo participante */
    public static ResultadoDoTurno repetir() {
        return new ResultadoDoTurno(true, false, 0);
    }

    /** @return diretiva que inverte o sentido e avança */
    public static ResultadoDoTurno inverter() {
        return new ResultadoDoTurno(false, true, 0);
    }

    /**
     * @param quantidade participantes a saltar
     * @return diretiva de pulo e avanço
     */
    public static ResultadoDoTurno pular(int quantidade) {
        return new ResultadoDoTurno(false, false, quantidade);
    }
}
