package br.edu.uepb.map.trinca;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/** Estado das zonas exclusivas da Trinca, separado do estado genérico do engine. */
final class MesaTrinca {

    private final Deque<CartaTrinca> descarte = new ArrayDeque<>();

    Optional<CartaTrinca> topoDoDescarte() {
        return Optional.ofNullable(descarte.peekFirst());
    }

    int quantidadeNoDescarte() {
        return descarte.size();
    }

    void descartar(CartaTrinca carta) {
        descarte.addFirst(carta);
    }

    CartaTrinca comprarDoDescarte() {
        CartaTrinca carta = descarte.pollFirst();
        if (carta == null) {
            throw new IllegalStateException("Não há carta na pilha de descarte.");
        }
        return carta;
    }

    List<CartaTrinca> retirarParaReciclagem() {
        CartaTrinca topo = descarte.pollFirst();
        if (topo == null || descarte.isEmpty()) {
            if (topo != null) {
                descarte.addFirst(topo);
            }
            return List.of();
        }

        List<CartaTrinca> recicladas = new ArrayList<>(descarte);
        descarte.clear();
        descarte.addFirst(topo);
        return recicladas;
    }
}
