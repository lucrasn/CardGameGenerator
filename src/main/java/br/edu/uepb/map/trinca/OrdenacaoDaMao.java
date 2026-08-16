package br.edu.uepb.map.trinca;

import java.util.Comparator;

/** Preferência visual usada para organizar as cartas de uma pessoa. */
enum OrdenacaoDaMao {
    POR_NAIPE(
            "Agrupar por naipe",
            Comparator.comparing(CartaTrinca::naipe)
                    .thenComparingInt(carta -> carta.valor().ordem())),
    POR_VALOR(
            "Ordenar por valor crescente (A até K)",
            Comparator.comparingInt((CartaTrinca carta) -> carta.valor().ordem())
                    .thenComparing(CartaTrinca::naipe));

    private final String descricao;
    private final Comparator<CartaTrinca> comparador;

    OrdenacaoDaMao(String descricao, Comparator<CartaTrinca> comparador) {
        this.descricao = descricao;
        this.comparador = comparador;
    }

    String descricao() {
        return descricao;
    }

    Comparator<CartaTrinca> comparador() {
        return comparador;
    }

    OrdenacaoDaMao alternar() {
        return this == POR_VALOR ? POR_NAIPE : POR_VALOR;
    }

    String opcaoDeAlternancia() {
        return "Mudar visualização: " + alternar().descricao();
    }
}
