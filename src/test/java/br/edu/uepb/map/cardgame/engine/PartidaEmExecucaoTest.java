package br.edu.uepb.map.cardgame.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.BaralhoPadrao;
import br.edu.uepb.map.cardgame.api.EstadoPartida;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.apoio.CartaFalsa;
import br.edu.uepb.map.cardgame.api.excecao.EstadoDePartidaInvalidoException;
import br.edu.uepb.map.cardgame.apoio.JogadorDeTeste;

@DisplayName("PartidaEmExecucao — estado interno e porta controlada")
class PartidaEmExecucaoTest {

    private Jogador ana;
    private Jogador bruno;
    private BaralhoPadrao<CartaFalsa> baralho;
    private CicloDeVidaDaPartida ciclo;
    private PartidaEmExecucao<CartaFalsa> partida;

    @BeforeEach
    void prepararPartida() {
        ana = new JogadorDeTeste("Ana");
        bruno = new JogadorDeTeste("Bruno");
        baralho = new BaralhoPadrao<>(List.of(
                CartaFalsa.comNumero(1), CartaFalsa.comNumero(2), CartaFalsa.comNumero(3)));
        ciclo = new CicloDeVidaDaPartida();
        ciclo.transicionarPara(EstadoPartida.PREPARANDO);
        GerenciadorDeTurnos turnos = new GerenciadorDeTurnos(List.of(ana, bruno), 0);
        partida = new PartidaEmExecucao<>(List.of(ana, bruno), baralho, turnos, ciclo);
    }

    @Test
    @DisplayName("permanece interna e expõe snapshots imutáveis")
    void encapsulaEstrutura() {
        assertFalse(Modifier.isPublic(PartidaEmExecucao.class.getModifiers()));
        assertThrows(UnsupportedOperationException.class,
                () -> partida.jogadores().add(new JogadorDeTeste("Carla")));

        partida.entregarProximaCarta(ana);
        List<CartaFalsa> mao = partida.maoDe(ana);
        assertEquals(List.of(CartaFalsa.comNumero(1)), mao);
        assertThrows(UnsupportedOperationException.class,
                () -> mao.add(CartaFalsa.comNumero(9)));
    }

    @Test
    @DisplayName("compra, transfere entre zonas e preserva identidades únicas")
    void manipulaCartas() {
        CartaFalsa comprada = partida.comprarDoBaralho();
        partida.adicionarNaMao(ana, comprada);

        assertEquals(2, partida.quantidadeNoBaralho());
        assertEquals(List.of(comprada), partida.maoDe(ana));
        assertThrows(IllegalArgumentException.class,
                () -> partida.adicionarNaMao(bruno, comprada));

        CartaFalsa removida = partida.removerDaMao(ana, comprada.id());
        assertSame(comprada, removida);
        partida.adicionarAoBaralho(List.of(removida));

        assertEquals(3, partida.quantidadeNoBaralho());
        assertEquals(removida, baralho.cartas().getLast());
    }

    @Test
    @DisplayName("distribuição valida o participante antes de retirar a carta")
    void distribuicaoEhControlada() {
        Jogador estranho = new JogadorDeTeste("Estranho");

        assertThrows(IllegalArgumentException.class,
                () -> partida.entregarProximaCarta(estranho));
        assertEquals(3, partida.quantidadeNoBaralho());

        partida.entregarProximaCarta(bruno);
        assertEquals(2, partida.quantidadeNoBaralho());
        assertEquals(1, partida.maoDe(bruno).size());
    }

    @Test
    @DisplayName("rejeita coleção duplicada antes de alterar o baralho")
    void adicaoAoBaralhoEhAtomicaParaEntradaInvalida() {
        CartaFalsa nova = CartaFalsa.comNumero(10);

        assertThrows(IllegalArgumentException.class,
                () -> partida.adicionarAoBaralho(List.of(nova, nova)));
        assertEquals(3, partida.quantidadeNoBaralho());
    }

    @Test
    @DisplayName("estado final impede qualquer mutação")
    void bloqueiaMutacaoAposFinalizar() {
        ciclo.transicionarPara(EstadoPartida.EM_ANDAMENTO);
        ciclo.transicionarPara(EstadoPartida.FINALIZADA);

        assertThrows(EstadoDePartidaInvalidoException.class, partida::comprarDoBaralho);
        assertThrows(EstadoDePartidaInvalidoException.class, partida::embaralharBaralho);
        assertThrows(EstadoDePartidaInvalidoException.class,
                () -> partida.adicionarAoBaralho(List.of(CartaFalsa.comNumero(20))));
    }
}
