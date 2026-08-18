package br.edu.uepb.map.trinca;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * Estado das zonas exclusivas da Trinca, separado do estado genérico do engine.
 *
 * <p>A mesa mantém a pilha de descarte e oferece as operações necessárias para
 * compra, descarte e reciclagem.</p>
 *
 * @author Raffael Wagner Rolim Siqueira
 * @version 0.0.1
 */
final class MesaTrinca {

    private final Deque<CartaTrinca> descarte = new ArrayDeque<>();

    /**
     * Consulta a carta visível no descarte sem removê-la.
     *
     * @return topo do descarte, ou vazio quando não houver cartas
     */
    Optional<CartaTrinca> topoDoDescarte() {
        return Optional.ofNullable(descarte.peekFirst());
    }

    /**
     * Informa a quantidade de cartas descartadas.
     *
     * @return número de cartas presentes no descarte
     */
    int quantidadeNoDescarte() {
        return descarte.size();
    }

    /**
     * Coloca uma carta no topo do descarte.
     *
     * @param carta carta que será descartada
     */
    void descartar(CartaTrinca carta) {
        descarte.addFirst(carta);
    }

    /**
     * Remove e devolve a carta visível no descarte.
     *
     * @return carta removida do topo
     * @throws IllegalStateException se a pilha estiver vazia
     */
    CartaTrinca comprarDoDescarte() {
        CartaTrinca carta = descarte.pollFirst();
        if (carta == null) {
            throw new IllegalStateException("Não há carta na pilha de descarte.");
        }
        return carta;
    }

    /**
     * Retira todas as cartas da pilha para que sejam recicladas no monte.
     *
     * @return snapshot das cartas retiradas, da mais recente para a mais antiga
     */
    List<CartaTrinca> retirarTodasParaReciclagem() {
        List<CartaTrinca> recicladas = List.copyOf(descarte);
        descarte.clear();
        return recicladas;
    }
}
