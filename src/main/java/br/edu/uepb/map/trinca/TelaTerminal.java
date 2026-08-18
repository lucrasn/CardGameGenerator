package br.edu.uepb.map.trinca;

import java.util.Objects;

import br.edu.uepb.map.cardgame.api.EntradaSaida;

/**
 * Operações ANSI compartilhadas pela aplicação de console da Trinca.
 *
 * <p>A classe concentra os comandos de controle da tela para evitar que a
 * interface de console espalhe sequências ANSI pelo restante da aplicação.</p>
 */
final class TelaTerminal {

    /** Sequência ANSI que posiciona o cursor e apaga a tela e seu histórico. */
    static final String APAGAR_TELA_E_HISTORICO = "\u001B[H\u001B[2J\u001B[3J";

    private TelaTerminal() {
    }

    /**
     * Envia ao canal de saída o comando para limpar o terminal.
     *
     * @param entradaSaida canal pelo qual o comando será exibido
     * @throws NullPointerException se o canal de entrada e saída for nulo
     */
    static void apagar(EntradaSaida entradaSaida) {
        Objects.requireNonNull(
                entradaSaida, "A entrada e saída não pode ser nula.")
                .exibir(APAGAR_TELA_E_HISTORICO);
    }
}
