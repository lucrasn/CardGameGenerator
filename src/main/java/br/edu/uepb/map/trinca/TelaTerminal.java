package br.edu.uepb.map.trinca;

import java.util.Objects;

import br.edu.uepb.map.cardgame.api.EntradaSaida;

/** Operações ANSI compartilhadas pela aplicação de console da Trinca. */
final class TelaTerminal {

    static final String APAGAR_TELA_E_HISTORICO = "\u001B[H\u001B[2J\u001B[3J";

    private TelaTerminal() {
    }

    static void apagar(EntradaSaida entradaSaida) {
        Objects.requireNonNull(
                entradaSaida, "A entrada e saída não pode ser nula.")
                .exibir(APAGAR_TELA_E_HISTORICO);
    }
}
