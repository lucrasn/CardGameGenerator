package br.edu.uepb.map.trinca;

import java.util.Comparator;

/**
 * Preferência visual usada para organizar as cartas da mão de um participante.
 *
 * <p>A ordenação altera somente a apresentação no console e não interfere nas
 * regras nem na disposição interna das cartas mantida pelo jogo.</p>
 */
enum OrdenacaoDaMao {
    /** Agrupa as cartas por naipe e, dentro dele, por valor. */
    POR_NAIPE(
            "Agrupar por naipe",
            Comparator.comparing(CartaTrinca::naipe)
                    .thenComparingInt(carta -> carta.valor().ordem())),
    /** Ordena as cartas pelo valor crescente e usa o naipe como desempate. */
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

    /**
     * Retorna a descrição apresentada ao participante.
     *
     * @return descrição do modo de ordenação
     */
    String descricao() {
        return descricao;
    }

    /**
     * Retorna o comparador correspondente ao modo selecionado.
     *
     * @return comparador de cartas da Trinca
     */
    Comparator<CartaTrinca> comparador() {
        return comparador;
    }

    /**
     * Alterna entre a ordenação por valor e a ordenação por naipe.
     *
     * @return modo de ordenação oposto ao atual
     */
    OrdenacaoDaMao alternar() {
        return this == POR_VALOR ? POR_NAIPE : POR_VALOR;
    }

    /**
     * Monta o texto da opção que permite alterar a visualização.
     *
     * @return opção contendo a descrição do próximo modo
     */
    String opcaoDeAlternancia() {
        return "Mudar visualização: " + alternar().descricao();
    }
}
