package br.edu.uepb.map.cardgame.api;

import static br.edu.uepb.map.cardgame.api.apoio.CartaFalsa.comNumero;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.apoio.CartaFalsa;
import br.edu.uepb.map.cardgame.api.excecao.BaralhoVazioException;

@DisplayName("BaralhoPadrao — ordem, identidade e encapsulamento")
class BaralhoPadraoTest {

    @Nested
    @DisplayName("Topo e compra")
    class TopoECompra {

        @Test
        @DisplayName("a primeira carta da coleção é o topo")
        void primeiraCartaEhTopo() {
            CartaFalsa primeira = comNumero(1);
            CartaFalsa segunda = comNumero(2);
            var baralho = new BaralhoPadrao<>(List.of(primeira, segunda));

            assertSame(primeira, baralho.topo().orElseThrow());
            assertSame(primeira, baralho.comprar());
            assertSame(segunda, baralho.topo().orElseThrow());
        }

        @Test
        @DisplayName("cartas podem ser colocadas explicitamente no topo e na base")
        void insereNoTopoENaBase() {
            CartaFalsa primeira = comNumero(1);
            CartaFalsa topo = comNumero(2);
            CartaFalsa base = comNumero(3);
            var baralho = new BaralhoPadrao<>(List.of(primeira));

            baralho.colocarNoTopo(topo);
            baralho.colocarNaBase(base);

            assertEquals(List.of(topo, primeira, base), baralho.cartas());
        }

        @Test
        @DisplayName("comprar de baralho vazio usa a exceção de domínio")
        void compraVazia() {
            var baralho = new BaralhoPadrao<CartaFalsa>();

            assertTrue(baralho.estaVazio());
            assertTrue(baralho.topo().isEmpty());
            assertThrows(BaralhoVazioException.class, baralho::comprar);
        }
    }

    @Nested
    @DisplayName("Identidade das cartas")
    class Identidade {

        @Test
        @DisplayName("duas cartas visualmente iguais são aceitas quando os IDs diferem")
        void cartasIguaisComIdsDiferentes() {
            CartaFalsa uma = new CartaFalsa(comNumero(1).id(), "sete-de-copas");
            CartaFalsa outra = new CartaFalsa(comNumero(2).id(), "sete-de-copas");

            var baralho = new BaralhoPadrao<>(List.of(uma, outra));

            assertEquals(2, baralho.quantidade());
        }

        @Test
        @DisplayName("um ID não pode representar duas cartas no mesmo baralho")
        void idRepetido() {
            CartaFalsa original = comNumero(1);
            CartaFalsa repetida = new CartaFalsa(original.id(), "outra representação");

            assertThrows(IllegalArgumentException.class,
                    () -> new BaralhoPadrao<>(List.of(original, repetida)));
        }

        @Test
        @DisplayName("cartas e identificadores nulos são rejeitados")
        void nulos() {
            assertThrows(NullPointerException.class,
                    () -> new BaralhoPadrao<CartaFalsa>(java.util.Arrays.asList((CartaFalsa) null)));
            assertThrows(NullPointerException.class,
                    () -> new BaralhoPadrao<>(List.of(new CartaFalsa(null, "sem-id"))));
        }
    }

    @Nested
    @DisplayName("Encapsulamento")
    class Encapsulamento {

        @Test
        @DisplayName("a coleção de entrada é copiada defensivamente")
        void copiaNaEntrada() {
            List<CartaFalsa> original = new ArrayList<>(List.of(comNumero(1), comNumero(2)));
            var baralho = new BaralhoPadrao<>(original);

            original.clear();

            assertEquals(2, baralho.quantidade());
        }

        @Test
        @DisplayName("cartas() devolve snapshot imutável, e não uma visão viva")
        void snapshotImutavel() {
            var baralho = new BaralhoPadrao<>(List.of(comNumero(1), comNumero(2)));
            List<CartaFalsa> snapshot = baralho.cartas();

            baralho.comprar();

            assertEquals(2, snapshot.size());
            assertThrows(UnsupportedOperationException.class,
                    () -> snapshot.add(comNumero(3)));
        }
    }

    @Test
    @DisplayName("embaralhar muda a ordem sem perder nem duplicar cartas")
    void embaralhamentoPreservaCartas() {
        List<CartaFalsa> cartas = new ArrayList<>();
        for (int numero = 1; numero <= 10; numero++) {
            cartas.add(comNumero(numero));
        }
        var baralho = new BaralhoPadrao<>(cartas);

        baralho.embaralhar(new Random(42));

        assertNotEquals(cartas, baralho.cartas());
        assertEquals(new HashSet<>(cartas), new HashSet<>(baralho.cartas()));
        assertFalse(baralho.estaVazio());
    }
}
