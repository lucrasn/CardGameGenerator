package br.edu.uepb.map.cardgame.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.apoio.JogadorDeTeste;

@DisplayName("DesfechoDePartida — avaliação devolvida pela regra de vitória")
class DesfechoDePartidaTest {

    private Jogador ana;
    private Jogador bruno;

    @BeforeEach
    void criarJogadores() {
        ana = new JogadorDeTeste("Ana");
        bruno = new JogadorDeTeste("Bruno");
    }

    @Nested
    @DisplayName("Desfechos válidos")
    class DesfechosValidos {

        @Test
        @DisplayName("vitória com um vencedor")
        void vitoriaSimples() {
            var desfecho = new DesfechoDePartida(List.of(ana), MotivoPadrao.VITORIA);

            assertEquals(List.of(ana), desfecho.vencedores());
            assertTrue(desfecho.motivo().ehVitoria());
        }

        @Test
        @DisplayName("empate pode ter dois vencedores")
        void empateComDois() {
            var desfecho = new DesfechoDePartida(List.of(ana, bruno), MotivoPadrao.EMPATE);

            assertEquals(2, desfecho.vencedores().size());
            assertTrue(desfecho.motivo().ehEmpate());
        }

        @Test
        @DisplayName("empate pode não ter vencedor algum")
        void empateSemVencedores() {
            var desfecho = new DesfechoDePartida(List.of(), MotivoPadrao.EMPATE);

            assertTrue(desfecho.vencedores().isEmpty());
        }

        @Test
        @DisplayName("esgotamento encerra sem vencedor e sem ser empate")
        void esgotamento() {
            var desfecho = new DesfechoDePartida(List.of(), MotivoPadrao.ESGOTAMENTO);

            assertTrue(desfecho.vencedores().isEmpty());
            assertEquals(false, desfecho.motivo().ehVitoria());
            assertEquals(false, desfecho.motivo().ehEmpate());
        }
    }

    @Nested
    @DisplayName("Combinações incoerentes")
    class Incoerencias {

        @Test
        @DisplayName("vitória sem vencedor é rejeitada")
        void vitoriaSemVencedor() {
            assertThrows(IllegalArgumentException.class,
                    () -> new DesfechoDePartida(List.of(), MotivoPadrao.VITORIA));
        }

        @Test
        @DisplayName("o mesmo vencedor não pode aparecer duas vezes")
        void vencedorRepetido() {
            UUID id = UUID.randomUUID();
            Jogador original = new JogadorDeTeste(id, "Original");
            Jogador copia = new JogadorDeTeste(id, "Cópia");

            assertThrows(IllegalArgumentException.class,
                    () -> new DesfechoDePartida(List.of(original, copia), MotivoPadrao.EMPATE));
        }

        @Test
        @DisplayName("um motivo não pode ser vitória e empate ao mesmo tempo")
        void motivoAmbiguo() {
            MotivoDeEncerramento ambiguo = new MotivoDeEncerramento() {
                @Override
                public boolean ehVitoria() {
                    return true;
                }

                @Override
                public boolean ehEmpate() {
                    return true;
                }
            };

            assertThrows(IllegalArgumentException.class,
                    () -> new DesfechoDePartida(List.of(ana), ambiguo));
        }

        @Test
        @DisplayName("componentes nulos são rejeitados")
        void componentesNulos() {
            assertThrows(NullPointerException.class,
                    () -> new DesfechoDePartida(null, MotivoPadrao.VITORIA));
            assertThrows(NullPointerException.class,
                    () -> new DesfechoDePartida(List.of(ana), null));
        }
    }

    @Nested
    @DisplayName("Imutabilidade (requisito 7)")
    class Imutabilidade {

        @Test
        @DisplayName("a lista devolvida não pode ser modificada")
        void listaImutavel() {
            var desfecho = new DesfechoDePartida(List.of(ana), MotivoPadrao.VITORIA);

            assertThrows(UnsupportedOperationException.class,
                    () -> desfecho.vencedores().add(bruno));
        }

        @Test
        @DisplayName("alterar a lista original depois não afeta o desfecho")
        void copiaDefensivaNaEntrada() {
            List<Jogador> vencedores = new ArrayList<>(List.of(ana));
            var desfecho = new DesfechoDePartida(vencedores, MotivoPadrao.VITORIA);

            vencedores.add(bruno);

            assertEquals(1, desfecho.vencedores().size());
        }
    }

    @Nested
    @DisplayName("MotivoDeEncerramento como ponto de extensão")
    class MotivoExtensivel {

        /** Um jogo cliente pode declarar motivos próprios sem tocar no framework. */
        private enum MotivoDoJogo implements MotivoDeEncerramento {
            RENDICAO,
            ESTOUROU_VINTE_E_UM;

            @Override
            public boolean ehVitoria() {
                return this == ESTOUROU_VINTE_E_UM;
            }
        }

        @Test
        @DisplayName("um motivo definido pelo jogo é aceito pelo desfecho")
        void motivoDoJogo() {
            var desfecho = new DesfechoDePartida(List.of(ana), MotivoDoJogo.ESTOUROU_VINTE_E_UM);

            assertTrue(desfecho.motivo().ehVitoria());
            assertEquals(MotivoDoJogo.ESTOUROU_VINTE_E_UM, desfecho.motivo());
        }

        @Test
        @DisplayName("as validações do framework valem também para motivos do jogo")
        void validacaoValeParaMotivoDoJogo() {
            assertThrows(IllegalArgumentException.class,
                    () -> new DesfechoDePartida(List.of(), MotivoDoJogo.ESTOUROU_VINTE_E_UM));
        }

        @Test
        @DisplayName("por padrão um motivo novo não é nem vitória nem empate")
        void padroesDaInterface() {
            MotivoDeEncerramento neutro = new MotivoDeEncerramento() {
            };

            assertEquals(false, neutro.ehVitoria());
            assertEquals(false, neutro.ehEmpate());
        }
    }
}
