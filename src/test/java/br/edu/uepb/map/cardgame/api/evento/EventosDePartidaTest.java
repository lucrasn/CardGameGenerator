package br.edu.uepb.map.cardgame.api.evento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.EventoDePartida;
import br.edu.uepb.map.cardgame.api.Jogada;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.JogadorPadrao;
import br.edu.uepb.map.cardgame.api.MotivoPadrao;
import br.edu.uepb.map.cardgame.api.PartidaListener;
import br.edu.uepb.map.cardgame.api.ResultadoDePartida;
import br.edu.uepb.map.cardgame.api.ResultadoDoTurno;

@DisplayName("Eventos básicos de partida")
class EventosDePartidaTest {

    private Jogador ana;
    private Jogador bruno;

    @BeforeEach
    void criarJogadores() {
        Jogada jogadaFalsa = new Jogada() { };
        ana = new JogadorPadrao("Ana", contexto -> jogadaFalsa);
        bruno = new JogadorPadrao("Bruno", contexto -> jogadaFalsa);
    }

    @Test
    @DisplayName("partida iniciada preserva um snapshot imutável dos jogadores")
    void partidaIniciadaPreservaSnapshot() {
        List<Jogador> original = new ArrayList<>(List.of(ana, bruno));
        var evento = new PartidaIniciada(original);

        original.clear();

        assertEquals(List.of(ana, bruno), evento.jogadores());
        assertThrows(UnsupportedOperationException.class,
                () -> evento.jogadores().add(ana));
    }

    @Test
    @DisplayName("cartas distribuídas informa apenas a quantidade restante no baralho")
    void cartasDistribuidasNaoExpoeMaosPrivadas() {
        var evento = new CartasDistribuidas(12);

        assertEquals(12, evento.cartasRestantesNoBaralho());
        assertThrows(IllegalArgumentException.class,
                () -> new CartasDistribuidas(-1));
    }

    @Test
    @DisplayName("turno iniciado informa número e jogador")
    void turnoIniciadoInformaDados() {
        var evento = new TurnoIniciado(1, ana);

        assertEquals(1, evento.numeroDoTurno());
        assertSame(ana, evento.jogador());
        assertThrows(IllegalArgumentException.class, () -> new TurnoIniciado(0, ana));
        assertThrows(NullPointerException.class, () -> new TurnoIniciado(1, null));
    }

    @Test
    @DisplayName("jogada rejeitada normaliza e exige um motivo")
    void jogadaRejeitadaExigeMotivo() {
        var evento = new JogadaRejeitada(2, ana, "  Carta ausente da mão.  ");

        assertEquals("Carta ausente da mão.", evento.motivo());
        assertThrows(IllegalArgumentException.class,
                () -> new JogadaRejeitada(2, ana, "   "));
        assertThrows(NullPointerException.class,
                () -> new JogadaRejeitada(2, ana, null));
    }

    @Test
    @DisplayName("turno encerrado informa a diretiva produzida")
    void turnoEncerradoInformaResultado() {
        ResultadoDoTurno resultado = ResultadoDoTurno.avancar();
        var evento = new TurnoEncerrado(3, ana, resultado);

        assertSame(resultado, evento.resultado());
        assertThrows(NullPointerException.class,
                () -> new TurnoEncerrado(3, ana, null));
    }

    @Test
    @DisplayName("partida finalizada carrega o resultado imutável")
    void partidaFinalizadaCarregaResultado() {
        var resultado = new ResultadoDePartida(
                List.of(ana), Map.of(ana, 1, bruno, 0), MotivoPadrao.VITORIA);
        var evento = new PartidaFinalizada(resultado);

        assertSame(resultado, evento.resultado());
        assertThrows(NullPointerException.class, () -> new PartidaFinalizada(null));
    }

    @Test
    @DisplayName("listener recebe qualquer implementação de evento")
    void listenerRecebeEvento() {
        AtomicReference<EventoDePartida> recebido = new AtomicReference<>();
        PartidaListener listener = recebido::set;
        EventoDePartida evento = new TurnoIniciado(1, ana);

        listener.aoOcorrer(evento);

        assertSame(evento, recebido.get());
        assertTrue(recebido.get() instanceof TurnoIniciado);
    }
}
