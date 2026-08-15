package br.edu.uepb.map.cardgame.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtensibilidadeDaDecisaoTest {

    @Test
    void clienteCriaAcoesEFasesSemAlterarFramework() {
        Jogada parar = new Parar("mao-1");
        EtapaDeTurno etapa = EtapaBlackjack.DECISAO;

        assertEquals("mao-1", ((Parar) parar).maoId());
        assertEquals(EtapaBlackjack.DECISAO, etapa);
    }

    private record Parar(String maoId) implements Jogada {
    }

    private enum EtapaBlackjack implements EtapaDeTurno {
        DECISAO
    }
}
