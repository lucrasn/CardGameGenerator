package br.edu.uepb.map.cardgame.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.apoio.CartaFalsa;
import br.edu.uepb.map.cardgame.api.excecao.JogadaInvalidaException;

@DisplayName("Contratos Strategy das regras")
class RegrasStrategyTest {

    private final Jogador jogador = new JogadorFalso();
    private final VisaoDaPartida<CartaFalsa> partida = new VisaoFalsa(jogador);
    private final Jogada jogada = new Jogada() { };

    @Nested
    @DisplayName("Contexto de validação")
    class Contexto {

        @Test
        @DisplayName("preserva a partida e a jogada")
        void preservaComponentes() {
            var contexto = new ContextoDeValidacao<>(partida, jogada);

            assertSame(partida, contexto.partida());
            assertSame(jogada, contexto.jogada());
        }

        @Test
        @DisplayName("rejeita componentes nulos")
        void rejeitaComponentesNulos() {
            assertThrows(NullPointerException.class,
                    () -> new ContextoDeValidacao<CartaFalsa>(null, jogada));
            assertThrows(NullPointerException.class,
                    () -> new ContextoDeValidacao<CartaFalsa>(partida, null));
        }
    }

    @Nested
    @DisplayName("Regra de validação")
    class Validacao {

        @Test
        @DisplayName("aceita uma implementação substituível")
        void aceitaImplementacaoSubstituivel() {
            var contexto = new ContextoDeValidacao<>(partida, jogada);
            RegraDeValidacaoStrategy<CartaFalsa> regra =
                    recebido -> assertSame(contexto, recebido);

            regra.validar(contexto);
        }

        @Test
        @DisplayName("permite rejeitar a jogada por exceção de domínio")
        void permiteRejeitarJogada() {
            RegraDeValidacaoStrategy<CartaFalsa> regra = contexto -> {
                throw new JogadaInvalidaException("Jogada rejeitada pelo teste.");
            };

            assertThrows(JogadaInvalidaException.class,
                    () -> regra.validar(new ContextoDeValidacao<>(partida, jogada)));
        }
    }

    @Test
    @DisplayName("regra de vitória devolve desfecho opcional")
    void regraDeVitoriaDevolveDesfechoOpcional() {
        var desfecho = new DesfechoDePartida(List.of(jogador), MotivoPadrao.VITORIA);
        RegraDeVitoriaStrategy<CartaFalsa> regra = contexto -> Optional.of(desfecho);

        assertSame(desfecho, regra.avaliar(partida).orElseThrow());
    }

    @Test
    @DisplayName("regra de vitória pode indicar que a partida continua")
    void regraDeVitoriaPodeManterPartida() {
        RegraDeVitoriaStrategy<CartaFalsa> regra = contexto -> Optional.empty();

        assertTrue(regra.avaliar(partida).isEmpty());
    }

    @Test
    @DisplayName("regra de pontuação calcula um placar substituível")
    void regraDePontuacaoCalculaPlacar() {
        var desfecho = new DesfechoDePartida(List.of(jogador), MotivoPadrao.VITORIA);
        RegraDePontuacaoStrategy<CartaFalsa> regra =
                (contexto, resultado) -> Map.of(jogador, 1);

        assertEquals(Map.of(jogador, 1), regra.calcular(partida, desfecho));
    }

    private final class JogadorFalso implements Jogador {

        private final UUID id = UUID.randomUUID();

        @Override
        public UUID id() {
            return id;
        }

        @Override
        public String nome() {
            return "Jogador de teste";
        }

        @Override
        public EstrategiaDeDecisao estrategiaDeDecisao() {
            return contexto -> jogada;
        }
    }

    private record VisaoFalsa(Jogador jogador) implements VisaoDaPartida<CartaFalsa> {

        @Override
        public EstadoPartida estado() {
            return EstadoPartida.EM_ANDAMENTO;
        }

        @Override
        public List<Jogador> jogadores() {
            return List.of(jogador);
        }

        @Override
        public Jogador jogadorAtual() {
            return jogador;
        }

        @Override
        public List<CartaFalsa> maoDe(Jogador participante) {
            return List.of();
        }

        @Override
        public int quantidadeNoBaralho() {
            return 0;
        }

        @Override
        public long numeroDoTurno() {
            return 1;
        }
    }
}
