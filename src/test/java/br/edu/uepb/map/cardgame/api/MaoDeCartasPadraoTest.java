package br.edu.uepb.map.cardgame.api;

import static br.edu.uepb.map.cardgame.api.apoio.CartaFalsa.comNumero;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.apoio.CartaFalsa;

@DisplayName("MaoDeCartasPadrao — operações controladas e snapshots")
class MaoDeCartasPadraoTest {

    @Nested
    @DisplayName("Consulta e remoção por identidade")
    class ConsultaERemocao {

        @Test
        @DisplayName("busca e remove a instância identificada")
        void buscaERemove() {
            CartaFalsa primeira = comNumero(1);
            CartaFalsa segunda = comNumero(2);
            var mao = new MaoDeCartasPadrao<>(List.of(primeira, segunda));

            assertTrue(mao.contem(primeira));
            assertSame(primeira, mao.buscar(primeira.id()).orElseThrow());
            assertSame(primeira, mao.remover(primeira.id()));
            assertEquals(List.of(segunda), mao.cartas());
        }

        @Test
        @DisplayName("remover carta ausente não altera a mão")
        void removeAusente() {
            CartaFalsa presente = comNumero(1);
            var mao = new MaoDeCartasPadrao<>(List.of(presente));

            assertThrows(NoSuchElementException.class, () -> mao.remover(comNumero(2)));

            assertEquals(List.of(presente), mao.cartas());
        }

        @Test
        @DisplayName("adicionar uma segunda carta com o mesmo ID é rejeitado")
        void rejeitaIdRepetido() {
            CartaFalsa presente = comNumero(1);
            var mao = new MaoDeCartasPadrao<>(List.of(presente));

            assertThrows(IllegalArgumentException.class,
                    () -> mao.adicionar(new CartaFalsa(presente.id(), "duplicada")));
        }
    }

    @Nested
    @DisplayName("Encapsulamento")
    class Encapsulamento {

        @Test
        @DisplayName("a coleção de entrada é copiada")
        void copiaNaEntrada() {
            List<CartaFalsa> original = new ArrayList<>(List.of(comNumero(1)));
            var mao = new MaoDeCartasPadrao<>(original);

            original.add(comNumero(2));

            assertEquals(1, mao.quantidade());
        }

        @Test
        @DisplayName("cartas() devolve snapshot imutável, e não uma visão viva")
        void snapshotImutavel() {
            var mao = new MaoDeCartasPadrao<>(List.of(comNumero(1)));
            List<CartaFalsa> snapshot = mao.cartas();

            mao.adicionar(comNumero(2));

            assertEquals(1, snapshot.size());
            assertThrows(UnsupportedOperationException.class,
                    () -> snapshot.add(comNumero(3)));
        }
    }

    @Test
    @DisplayName("mão vazia recebe sua primeira carta")
    void maoVaziaRecebeCarta() {
        var mao = new MaoDeCartasPadrao<CartaFalsa>();
        assertTrue(mao.estaVazia());

        mao.adicionar(comNumero(1));

        assertEquals(1, mao.quantidade());
    }
}
