package br.edu.uepb.map.cardgame.cliente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.BaralhoPadrao;
import br.edu.uepb.map.cardgame.api.ContextoDePartida;
import br.edu.uepb.map.cardgame.api.DesfechoDePartida;
import br.edu.uepb.map.cardgame.api.DistribuicaoAlternada;
import br.edu.uepb.map.cardgame.api.EventoDePartida;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.MotivoPadrao;
import br.edu.uepb.map.cardgame.api.PartidaConfig;
import br.edu.uepb.map.cardgame.api.ResultadoDoTurno;
import br.edu.uepb.map.cardgame.api.apoio.CartaFalsa;
import br.edu.uepb.map.cardgame.api.evento.CartasDistribuidas;
import br.edu.uepb.map.cardgame.api.evento.PartidaFinalizada;
import br.edu.uepb.map.cardgame.api.evento.PartidaIniciada;
import br.edu.uepb.map.cardgame.api.evento.TurnoEncerrado;
import br.edu.uepb.map.cardgame.api.evento.TurnoIniciado;
import br.edu.uepb.map.cardgame.apoio.JogadorDeTeste;
import br.edu.uepb.map.cardgame.engine.MotorDePartida;

/**
 * Prova que um jogo em package externo publica eventos sem acessar internals do engine.
 */
@DisplayName("Publicação de evento específico por jogo cliente")
class PublicacaoDeEventoDoClienteTest {

    @Test
    @DisplayName("subclasse entrega evento próprio pela infraestrutura do motor")
    void publicaEventoProprioDoJogo() throws NoSuchMethodException {
        List<Jogador> jogadores = List.of(
                new JogadorDeTeste("Ana"), new JogadorDeTeste("Bruno"));
        EventoDoJogo eventoDoJogo = new EventoDoJogo("carta descartada");
        List<EventoDePartida> recebidos = new ArrayList<>();
        MotorCliente motor = new MotorCliente(configuracao(jogadores), eventoDoJogo);

        motor.adicionarListener(recebidos::add);
        motor.executar();

        assertSame(eventoDoJogo, recebidos.get(3));
        assertEquals(List.of(
                PartidaIniciada.class,
                CartasDistribuidas.class,
                TurnoIniciado.class,
                EventoDoJogo.class,
                TurnoEncerrado.class,
                PartidaFinalizada.class),
                recebidos.stream().map(Object::getClass).toList());
        int modificadores = MotorDePartida.class
                .getDeclaredMethod("publicarEvento", EventoDePartida.class)
                .getModifiers();
        assertTrue(Modifier.isProtected(modificadores));
        assertTrue(Modifier.isFinal(modificadores));

        MotorCliente motorComEventoNulo = new MotorCliente(
                configuracao(jogadores), null);
        assertThrows(NullPointerException.class, motorComEventoNulo::executar);
    }

    private static PartidaConfig<CartaFalsa> configuracao(List<Jogador> jogadores) {
        return PartidaConfig.<CartaFalsa>builder()
                .jogadores(jogadores)
                .baralhoFactory(() -> new BaralhoPadrao<>(List.of(
                        CartaFalsa.comNumero(1), CartaFalsa.comNumero(2),
                        CartaFalsa.comNumero(3), CartaFalsa.comNumero(4))))
                .distribuicao(new DistribuicaoAlternada<>(1))
                .regraDeValidacao(contexto -> { })
                .regraDeVitoria(contexto -> contexto.numeroDoTurno() < 1
                        ? Optional.empty()
                        : Optional.of(new DesfechoDePartida(
                                List.of(contexto.jogadorAtual()), MotivoPadrao.VITORIA)))
                .regraDePontuacao((contexto, desfecho) -> {
                    Map<Jogador, Integer> placar = new LinkedHashMap<>();
                    contexto.jogadores().forEach(jogador -> placar.put(
                            jogador, desfecho.vencedores().contains(jogador) ? 10 : 0));
                    return placar;
                })
                .build();
    }

    private record EventoDoJogo(String descricao) implements EventoDePartida {
    }

    private static final class MotorCliente extends MotorDePartida<CartaFalsa> {

        private final EventoDePartida evento;

        private MotorCliente(
                PartidaConfig<CartaFalsa> configuracao, EventoDePartida evento) {
            super(configuracao);
            this.evento = evento;
        }

        @Override
        protected ResultadoDoTurno executarTurno(ContextoDePartida<CartaFalsa> contexto) {
            publicarEvento(evento);
            return ResultadoDoTurno.avancar();
        }
    }
}
