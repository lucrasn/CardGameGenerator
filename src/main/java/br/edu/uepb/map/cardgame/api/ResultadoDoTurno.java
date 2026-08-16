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
 */
public record ResultadoDoTurno(boolean repetirJogador,
                               boolean inverterSentido,
                               int jogadoresAPular) {

    /**
     * Cria uma diretiva e rejeita combinações incompatíveis.
     *
     * @throws IllegalArgumentException se a quantidade for negativa ou se a diretiva
     *         tentar repetir o participante e pular outros ao mesmo tempo
     */
    public ResultadoDoTurno {
        if (jogadoresAPular < 0) {
            throw new IllegalArgumentException("A quantidade de jogadores a pular não pode ser negativa.");
        }
        if (repetirJogador && jogadoresAPular > 0) {
            throw new IllegalArgumentException("Não é possível repetir o jogador e pular participantes.");
        }
    }

    /**
     * Cria uma diretiva de avanço simples.
     *
     * @return diretiva de avanço normal
     */
    public static ResultadoDoTurno avancar() {
        return new ResultadoDoTurno(false, false, 0);
    }

    /**
     * Cria uma diretiva que conserva a vez atual.
     *
     * @return diretiva que mantém o mesmo participante
     */
    public static ResultadoDoTurno repetir() {
        return new ResultadoDoTurno(true, false, 0);
    }

    /**
     * Cria uma diretiva que inverte a rotação antes de avançar.
     *
     * @return diretiva que inverte o sentido e avança
     */
    public static ResultadoDoTurno inverter() {
        return new ResultadoDoTurno(false, true, 0);
    }

    /**
     * Cria uma diretiva que salta participantes antes do próximo turno.
     *
     * @param quantidade participantes a saltar
     * @return diretiva de pulo e avanço
     * @throws IllegalArgumentException se a quantidade for negativa
     */
    public static ResultadoDoTurno pular(int quantidade) {
        return new ResultadoDoTurno(false, false, quantidade);
    }
}
