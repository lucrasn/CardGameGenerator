package br.edu.uepb.map.cardgame.core;

import static br.edu.uepb.map.cardgame.api.apoio.CartaFalsa.comNumero;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.BaralhoPadrao;
import br.edu.uepb.map.cardgame.api.DistribuicaoAlternada;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.MaoDeCartasPadrao;
import br.edu.uepb.map.cardgame.api.apoio.CartaFalsa;
import br.edu.uepb.map.cardgame.api.excecao.BaralhoVazioException;
import br.edu.uepb.map.cardgame.core.apoio.JogadorFalso;

@DisplayName("DistribuicaoAlternada — estratégia sobre contexto controlado")
class DistribuicaoAlternadaTest {

    private Jogador ana;
    private Jogador bruno;

    @BeforeEach
    void criarJogadores() {
        ana = new JogadorFalso("Ana");
        bruno = new JogadorFalso("Bruno");
    }

    @Test
    @DisplayName("entrega uma carta por jogador a cada volta")
    void distribuiAlternadamente() {
        var baralho = new BaralhoPadrao<>(List.of(
                comNumero(1), comNumero(2), comNumero(3),
                comNumero(4), comNumero(5), comNumero(6)));
        var maoAna = new MaoDeCartasPadrao<CartaFalsa>();
        var maoBruno = new MaoDeCartasPadrao<CartaFalsa>();
        var contexto = new ContextoDeDistribuicaoPadrao<>(
                baralho, List.of(ana, bruno), List.of(maoAna, maoBruno));

        new DistribuicaoAlternada<CartaFalsa>(3).distribuir(contexto);

        assertEquals(List.of(comNumero(1), comNumero(3), comNumero(5)), maoAna.cartas());
        assertEquals(List.of(comNumero(2), comNumero(4), comNumero(6)), maoBruno.cartas());
        assertEquals(0, baralho.quantidade());
    }

    @Test
    @DisplayName("falta de cartas é detectada antes de qualquer alteração")
    void insuficienciaNaoDeixaEstadoParcial() {
        var baralho = new BaralhoPadrao<>(List.of(comNumero(1), comNumero(2), comNumero(3)));
        var maoAna = new MaoDeCartasPadrao<CartaFalsa>();
        var maoBruno = new MaoDeCartasPadrao<CartaFalsa>();
        var contexto = new ContextoDeDistribuicaoPadrao<>(
                baralho, List.of(ana, bruno), List.of(maoAna, maoBruno));

        assertThrows(BaralhoVazioException.class,
                () -> new DistribuicaoAlternada<CartaFalsa>(2).distribuir(contexto));

        assertEquals(3, baralho.quantidade());
        assertEquals(0, maoAna.quantidade());
        assertEquals(0, maoBruno.quantidade());
    }

    @Nested
    @DisplayName("Validação do contexto")
    class ValidacaoDoContexto {

        @Test
        @DisplayName("um jogador externo não pode receber carta")
        void rejeitaJogadorExterno() {
            var contexto = contextoMinimo();
            Jogador externo = new JogadorFalso("Externo");

            assertThrows(IllegalArgumentException.class,
                    () -> contexto.entregarProximaCarta(externo));
        }

        @Test
        @DisplayName("a lista de jogadores é um snapshot imutável")
        void jogadoresSaoImutaveis() {
            var contexto = contextoMinimo();

            assertThrows(UnsupportedOperationException.class,
                    () -> contexto.jogadores().add(new JogadorFalso("Externo")));
        }

        @Test
        @DisplayName("uma carta não pode começar em duas zonas")
        void rejeitaCartaEmDuasZonas() {
            CartaFalsa carta = comNumero(1);
            var baralho = new BaralhoPadrao<>(List.of(carta));
            var maoAna = new MaoDeCartasPadrao<>(List.of(carta));
            var maoBruno = new MaoDeCartasPadrao<CartaFalsa>();

            assertThrows(IllegalArgumentException.class,
                    () -> new ContextoDeDistribuicaoPadrao<>(
                            baralho, List.of(ana, bruno), List.of(maoAna, maoBruno)));
        }

        private ContextoDeDistribuicaoPadrao<CartaFalsa> contextoMinimo() {
            return new ContextoDeDistribuicaoPadrao<>(
                    new BaralhoPadrao<>(List.of(comNumero(1))),
                    List.of(ana, bruno),
                    List.of(new MaoDeCartasPadrao<>(), new MaoDeCartasPadrao<>()));
        }
    }
}
