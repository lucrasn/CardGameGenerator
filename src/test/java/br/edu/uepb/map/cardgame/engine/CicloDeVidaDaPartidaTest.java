package br.edu.uepb.map.cardgame.engine;

import static br.edu.uepb.map.cardgame.api.EstadoPartida.CONFIGURADA;
import static br.edu.uepb.map.cardgame.api.EstadoPartida.EM_ANDAMENTO;
import static br.edu.uepb.map.cardgame.api.EstadoPartida.FINALIZADA;
import static br.edu.uepb.map.cardgame.api.EstadoPartida.PREPARANDO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.excecao.EstadoDePartidaInvalidoException;

@DisplayName("CicloDeVidaDaPartida — transições internas")
class CicloDeVidaDaPartidaTest {

    @Test
    @DisplayName("percorre a sequência única do Template Method")
    void caminhoFeliz() {
        CicloDeVidaDaPartida ciclo = new CicloDeVidaDaPartida();
        assertEquals(CONFIGURADA, ciclo.estado());
        ciclo.transicionarPara(PREPARANDO);
        ciclo.transicionarPara(EM_ANDAMENTO);
        ciclo.transicionarPara(FINALIZADA);
        assertEquals(FINALIZADA, ciclo.estado());
    }

    @Test
    @DisplayName("rejeita salto, retrocesso e saída do estado terminal")
    void rejeitaTransicoesIlegais() {
        CicloDeVidaDaPartida ciclo = new CicloDeVidaDaPartida();
        assertThrows(EstadoDePartidaInvalidoException.class,
                () -> ciclo.transicionarPara(EM_ANDAMENTO));
        ciclo.transicionarPara(PREPARANDO);
        assertThrows(EstadoDePartidaInvalidoException.class,
                () -> ciclo.transicionarPara(CONFIGURADA));
        ciclo.transicionarPara(EM_ANDAMENTO);
        ciclo.transicionarPara(FINALIZADA);
        assertThrows(EstadoDePartidaInvalidoException.class,
                () -> ciclo.transicionarPara(FINALIZADA));
    }
}
