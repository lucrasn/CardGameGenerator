package br.edu.uepb.map.cardgame.api;

import static br.edu.uepb.map.cardgame.api.apoio.CartaFalsa.comNumero;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.apoio.CartaFalsa;
import br.edu.uepb.map.cardgame.api.apoio.JogadorFalso;
import br.edu.uepb.map.cardgame.api.excecao.BaralhoVazioException;

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
        var contexto = new ContextoDeDistribuicaoFalso<>(
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
        var contexto = new ContextoDeDistribuicaoFalso<>(
                baralho, List.of(ana, bruno), List.of(maoAna, maoBruno));

        assertThrows(BaralhoVazioException.class,
                () -> new DistribuicaoAlternada<CartaFalsa>(2).distribuir(contexto));

        assertEquals(3, baralho.quantidade());
        assertEquals(0, maoAna.quantidade());
        assertEquals(0, maoBruno.quantidade());
    }

    private static final class ContextoDeDistribuicaoFalso<C extends Carta>
            implements ContextoDeDistribuicao<C> {

        private final Baralho<C> baralho;
        private final List<Jogador> jogadores;
        private final List<MaoDeCartas<C>> maos;

        private ContextoDeDistribuicaoFalso(
                Baralho<C> baralho,
                List<Jogador> jogadores,
                List<MaoDeCartas<C>> maos) {
            this.baralho = baralho;
            this.jogadores = List.copyOf(jogadores);
            this.maos = List.copyOf(maos);
        }

        @Override
        public List<Jogador> jogadores() {
            return jogadores;
        }

        @Override
        public int cartasDisponiveis() {
            return baralho.quantidade();
        }

        @Override
        public void entregarProximaCarta(Jogador jogador) {
            for (int indice = 0; indice < jogadores.size(); indice++) {
                if (jogadores.get(indice).id().equals(jogador.id())) {
                    maos.get(indice).adicionar(baralho.comprar());
                    return;
                }
            }
            throw new IllegalArgumentException("O jogador não pertence ao contexto de teste.");
        }
    }
}
