package br.edu.uepb.map.cardgame.cliente;

import br.edu.uepb.map.cardgame.api.ContextoDeDecisao;
import br.edu.uepb.map.cardgame.api.ContextoDeDecisaoPadrao;
import br.edu.uepb.map.cardgame.api.EstrategiaDeDecisao;
import br.edu.uepb.map.cardgame.api.EtapaDeTurno;
import br.edu.uepb.map.cardgame.api.Jogada;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Prova que jogos clientes modelam decisões sem acessar internals do framework.
 */
class ExtensibilidadeDaDecisaoTest {

    @Test
    void blackjackCriaAcoesEFasesSemAlterarFramework() {
        Jogada parar = AcaoBlackjack.PARAR;
        EtapaDeTurno etapa = EtapaBlackjack.DECISAO;

        assertSame(AcaoBlackjack.PARAR, parar);
        assertEquals(EtapaBlackjack.DECISAO, etapa);
    }

    @Test
    void estrategiaDoBlackjackPodeUsarContextoEspecializado() {
        EstrategiaDeDecisao estrategiaDaCasa = contextoBase -> {
            ContextoBlackjack contexto = (ContextoBlackjack) contextoBase;
            return contexto.totalDaMao() < 17
                    ? AcaoBlackjack.PEDIR
                    : AcaoBlackjack.PARAR;
        };

        ContextoDeDecisao comDezesseis = new ContextoBlackjackTeste(16);
        ContextoDeDecisao comDezessete = new ContextoBlackjackTeste(17);

        assertSame(AcaoBlackjack.PEDIR, estrategiaDaCasa.decidir(comDezesseis));
        assertSame(AcaoBlackjack.PARAR, estrategiaDaCasa.decidir(comDezessete));
    }

    @Test
    void trincaModelaCompraEDescarteComoJogadasTipadas() {
        Jogada comprarDoMonte = new Comprar(OrigemCompra.MONTE);
        Jogada comprarDoDescarte = new Comprar(OrigemCompra.DESCARTE);
        UUID cartaId = UUID.randomUUID();
        Jogada descartar = new Descartar(cartaId);
        ContextoDeDecisao contextoDeCompra = new ContextoDeDecisaoPadrao(
                EtapaTrinca.COMPRA,
                List.of(comprarDoMonte, comprarDoDescarte)
        );
        ContextoDeDecisao contextoDeDescarte = new ContextoDeDecisaoPadrao(
                EtapaTrinca.DESCARTE,
                List.of(descartar)
        );
        EstrategiaDeDecisao prefereDescarte = contexto -> contexto
                .jogadasPermitidas()
                .stream()
                .filter(Comprar.class::isInstance)
                .map(Comprar.class::cast)
                .filter(compra -> compra.origem() == OrigemCompra.DESCARTE)
                .findFirst()
                .orElseThrow();

        assertSame(
                comprarDoDescarte,
                prefereDescarte.decidir(contextoDeCompra)
        );
        assertEquals(EtapaTrinca.DESCARTE, contextoDeDescarte.etapa());
        assertEquals(cartaId, ((Descartar) descartar).cartaId());
    }

    private interface ContextoBlackjack extends ContextoDeDecisao {
        int totalDaMao();
    }

    private record ContextoBlackjackTeste(int totalDaMao)
            implements ContextoBlackjack {

        @Override
        public EtapaDeTurno etapa() {
            return EtapaBlackjack.DECISAO;
        }

        @Override
        public List<Jogada> jogadasPermitidas() {
            return List.of(AcaoBlackjack.PEDIR, AcaoBlackjack.PARAR);
        }
    }

    private enum AcaoBlackjack implements Jogada {
        PEDIR,
        PARAR
    }

    private enum EtapaBlackjack implements EtapaDeTurno {
        DECISAO
    }

    private record Comprar(OrigemCompra origem) implements Jogada {
    }

    private record Descartar(UUID cartaId) implements Jogada {
    }

    private enum OrigemCompra {
        MONTE,
        DESCARTE
    }

    private enum EtapaTrinca implements EtapaDeTurno {
        COMPRA,
        DESCARTE
    }
}
