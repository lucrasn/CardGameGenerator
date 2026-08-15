package br.edu.uepb.map.cardgame.engine;

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
import br.edu.uepb.map.cardgame.api.EstadoPartida;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.MotivoPadrao;
import br.edu.uepb.map.cardgame.api.PartidaConfig;
import br.edu.uepb.map.cardgame.api.ResultadoDePartida;
import br.edu.uepb.map.cardgame.api.ResultadoDoTurno;
import br.edu.uepb.map.cardgame.api.VisaoDaPartida;
import br.edu.uepb.map.cardgame.api.apoio.CartaFalsa;
import br.edu.uepb.map.cardgame.api.excecao.EstadoDePartidaInvalidoException;
import br.edu.uepb.map.cardgame.api.excecao.JogadaInvalidaException;
import br.edu.uepb.map.cardgame.apoio.JogadorDeTeste;

@DisplayName("MotorDePartida — Template Method e ciclo completo")
class MotorDePartidaTest {

    @Test
    @DisplayName("executar é final e mantém a sequência fixa do ciclo de vida")
    void executaTemplateMethod() throws NoSuchMethodException {
        List<Jogador> jogadores = jogadores("Ana", "Bruno");
        MotorControlado motor = new MotorControlado(
                configuracao(jogadores, 1), 2, List.of(ResultadoDoTurno.avancar()), false);

        ResultadoDePartida resultado = motor.executar();

        assertTrue(Modifier.isFinal(MotorDePartida.class
                .getMethod("executar").getModifiers()));
        assertEquals(List.of(
                "preparar:PREPARANDO",
                "distribuir:1,1",
                "turno:Bruno:1",
                "turno:Ana:2",
                "encerrar:FINALIZADA"), motor.etapas);
        assertEquals(EstadoPartida.FINALIZADA, motor.estado());
        assertSame(jogadores.getFirst(), resultado.vencedorUnico().orElseThrow());
        assertEquals(10, resultado.pontuacaoDe(jogadores.getFirst()).orElseThrow());
        assertThrows(EstadoDePartidaInvalidoException.class, motor::executar);
        assertThrows(EstadoDePartidaInvalidoException.class,
                () -> motor.contextoFinal.comprarDoBaralho());
    }

    @Test
    @DisplayName("aplica inversão e pulo sem entregar o gerenciador ao jogo")
    void aplicaDiretivasDeTurno() {
        List<Jogador> jogadores = jogadores("Ana", "Bruno", "Carla");
        MotorControlado motor = new MotorControlado(
                configuracao(jogadores, 0),
                3,
                List.of(ResultadoDoTurno.inverter(), ResultadoDoTurno.pular(1)),
                false);

        motor.executar();

        assertEquals(List.of("Ana", "Carla", "Ana"), motor.ordemDosTurnos);
    }

    @Test
    @DisplayName("jogada inválida repete o mesmo turno e o mesmo participante")
    void repeteJogadaInvalida() {
        List<Jogador> jogadores = jogadores("Ana", "Bruno");
        MotorControlado motor = new MotorControlado(
                configuracao(jogadores, 0), 1, List.of(ResultadoDoTurno.avancar()), true);

        motor.executar();

        assertEquals(2, motor.tentativas);
        assertEquals(List.of("Ana"), motor.ordemDosTurnos);
    }

    @Test
    @DisplayName("pode encerrar depois da distribuição sem executar turno")
    void encerraAntesDoPrimeiroTurno() {
        List<Jogador> jogadores = jogadores("Ana", "Bruno");
        MotorControlado motor = new MotorControlado(
                configuracao(jogadores, 0), 0, List.of(), false);

        ResultadoDePartida resultado = motor.executar();

        assertTrue(motor.ordemDosTurnos.isEmpty());
        assertSame(jogadores.getFirst(), resultado.vencedorUnico().orElseThrow());
    }

    @Test
    @DisplayName("rejeita vencedor externo e placar incompleto")
    void validaSaidasDosHooks() {
        List<Jogador> jogadores = jogadores("Ana", "Bruno");
        Jogador externo = new JogadorDeTeste("Externo");
        PartidaConfig<CartaFalsa> config = configuracao(jogadores, 0);

        MotorDePartida<CartaFalsa> vencedorExterno = new MotorDePartida<>(config) {
            @Override
            protected ResultadoDoTurno executarTurno(ContextoDePartida<CartaFalsa> contexto) {
                return ResultadoDoTurno.avancar();
            }

            @Override
            protected Optional<DesfechoDePartida> avaliarDesfecho(
                    VisaoDaPartida<CartaFalsa> contexto) {
                return contexto.numeroDoTurno() == 0
                        ? Optional.empty()
                        : Optional.of(new DesfechoDePartida(
                                List.of(externo), MotivoPadrao.VITORIA));
            }
        };

        MotorDePartida<CartaFalsa> placarIncompleto = new MotorDePartida<>(config) {
            @Override
            protected ResultadoDoTurno executarTurno(ContextoDePartida<CartaFalsa> contexto) {
                return ResultadoDoTurno.avancar();
            }

            @Override
            protected Optional<DesfechoDePartida> avaliarDesfecho(
                    VisaoDaPartida<CartaFalsa> contexto) {
                return contexto.numeroDoTurno() == 0
                        ? Optional.empty()
                        : Optional.of(new DesfechoDePartida(
                                List.of(jogadores.getFirst()), MotivoPadrao.VITORIA));
            }

            @Override
            protected Map<Jogador, Integer> calcularPontuacao(
                    VisaoDaPartida<CartaFalsa> contexto, DesfechoDePartida desfecho) {
                return Map.of(jogadores.getFirst(), 1);
            }
        };

        assertThrows(IllegalStateException.class, vencedorExterno::executar);
        assertThrows(IllegalStateException.class, placarIncompleto::executar);
    }

    private static PartidaConfig<CartaFalsa> configuracao(
            List<Jogador> jogadores, int primeiroJogador) {
        List<CartaFalsa> cartas = new ArrayList<>();
        for (int numero = 1; numero <= jogadores.size() * 3; numero++) {
            cartas.add(CartaFalsa.comNumero(numero));
        }
        return PartidaConfig.<CartaFalsa>builder()
                .jogadores(jogadores)
                .baralhoFactory(() -> new BaralhoPadrao<>(cartas))
                .distribuicao(new DistribuicaoAlternada<>(1))
                .primeiroJogador(primeiroJogador)
                .build();
    }

    private static List<Jogador> jogadores(String... nomes) {
        List<Jogador> jogadores = new ArrayList<>();
        for (String nome : nomes) {
            jogadores.add(new JogadorDeTeste(nome));
        }
        return List.copyOf(jogadores);
    }

    private static final class MotorControlado extends MotorDePartida<CartaFalsa> {

        private final long turnoDeEncerramento;
        private final List<ResultadoDoTurno> diretrizes;
        private final boolean rejeitarPrimeiraTentativa;
        private final List<String> etapas = new ArrayList<>();
        private final List<String> ordemDosTurnos = new ArrayList<>();
        private int turnosExecutados;
        private int tentativas;
        private ContextoDePartida<CartaFalsa> contextoFinal;

        MotorControlado(PartidaConfig<CartaFalsa> configuracao,
                        long turnoDeEncerramento,
                        List<ResultadoDoTurno> diretrizes,
                        boolean rejeitarPrimeiraTentativa) {
            super(configuracao);
            this.turnoDeEncerramento = turnoDeEncerramento;
            this.diretrizes = List.copyOf(diretrizes);
            this.rejeitarPrimeiraTentativa = rejeitarPrimeiraTentativa;
        }

        @Override
        protected void preparar(ContextoDePartida<CartaFalsa> contexto) {
            etapas.add("preparar:" + contexto.estado());
        }

        @Override
        protected void aposDistribuir(ContextoDePartida<CartaFalsa> contexto) {
            etapas.add("distribuir:" + contexto.maoDe(contexto.jogadores().get(0)).size()
                    + "," + contexto.maoDe(contexto.jogadores().get(1)).size());
        }

        @Override
        protected ResultadoDoTurno executarTurno(ContextoDePartida<CartaFalsa> contexto) {
            tentativas++;
            if (rejeitarPrimeiraTentativa && tentativas == 1) {
                throw new JogadaInvalidaException("tentativa controlada");
            }
            ordemDosTurnos.add(contexto.jogadorAtual().nome());
            etapas.add("turno:" + contexto.jogadorAtual().nome() + ":" + contexto.numeroDoTurno());
            ResultadoDoTurno resultado = diretrizes.isEmpty()
                    ? ResultadoDoTurno.avancar()
                    : diretrizes.get(Math.min(turnosExecutados, diretrizes.size() - 1));
            turnosExecutados++;
            return resultado;
        }

        @Override
        protected Optional<DesfechoDePartida> avaliarDesfecho(
                VisaoDaPartida<CartaFalsa> contexto) {
            if (contexto.numeroDoTurno() < turnoDeEncerramento) {
                return Optional.empty();
            }
            return Optional.of(new DesfechoDePartida(
                    List.of(contexto.jogadorAtual()), MotivoPadrao.VITORIA));
        }

        @Override
        protected Map<Jogador, Integer> calcularPontuacao(
                VisaoDaPartida<CartaFalsa> contexto, DesfechoDePartida desfecho) {
            Map<Jogador, Integer> placar = new LinkedHashMap<>();
            contexto.jogadores().forEach(jogador -> placar.put(
                    jogador, desfecho.vencedores().contains(jogador) ? 10 : 0));
            return placar;
        }

        @Override
        protected void aoEncerrar(
                VisaoDaPartida<CartaFalsa> contexto, ResultadoDePartida resultado) {
            etapas.add("encerrar:" + contexto.estado());
            contextoFinal = (ContextoDePartida<CartaFalsa>) contexto;
        }
    }
}
