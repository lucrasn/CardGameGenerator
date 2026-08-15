package br.edu.uepb.map.cardgame.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.core.apoio.JogadorFalso;

@DisplayName("GerenciadorDeTurnos — ordem, sentido e pulos")
class GerenciadorDeTurnosTest {

    private Jogador ana;
    private Jogador bruno;
    private Jogador carla;
    private Jogador davi;
    private Jogador elena;

    @BeforeEach
    void criarJogadores() {
        ana = new JogadorFalso("Ana");
        bruno = new JogadorFalso("Bruno");
        carla = new JogadorFalso("Carla");
        davi = new JogadorFalso("Davi");
        elena = new JogadorFalso("Elena");
    }

    private GerenciadorDeTurnos comDois() {
        return new GerenciadorDeTurnos(List.of(ana, bruno));
    }

    private GerenciadorDeTurnos comCinco() {
        return new GerenciadorDeTurnos(List.of(ana, bruno, carla, davi, elena));
    }

    @Nested
    @DisplayName("Avanço da vez")
    class Avanco {

        @Test
        @DisplayName("começa pelo primeiro jogador informado")
        void comecaPeloPrimeiro() {
            assertSame(ana, comDois().jogadorAtual());
        }

        @Test
        @DisplayName("com dois jogadores, a vez alterna")
        void doisJogadoresAlternam() {
            GerenciadorDeTurnos turnos = comDois();
            assertSame(bruno, turnos.avancar());
            assertSame(ana, turnos.avancar());
            assertSame(bruno, turnos.avancar());
        }

        @Test
        @DisplayName("com cinco jogadores, a vez dá a volta completa — a abertura para N funciona")
        void cincoJogadoresDaoAVolta() {
            GerenciadorDeTurnos turnos = comCinco();
            assertSame(bruno, turnos.avancar());
            assertSame(carla, turnos.avancar());
            assertSame(davi, turnos.avancar());
            assertSame(elena, turnos.avancar());
            assertSame(ana, turnos.avancar());
        }
    }

    @Nested
    @DisplayName("Inversão de sentido")
    class Inversao {

        @Test
        @DisplayName("começa no sentido horário")
        void comecaHorario() {
            assertEquals(SentidoDeRotacao.HORARIO, comCinco().sentido());
        }

        @Test
        @DisplayName("invertido, o índice retrocede e dá a volta pelo fim da lista")
        void invertidoRetrocedeComWrapAround() {
            GerenciadorDeTurnos turnos = comCinco();
            turnos.inverterSentido();

            assertEquals(SentidoDeRotacao.ANTI_HORARIO, turnos.sentido());
            // Estava em Ana (índice 0): recuar precisa levar ao último, não a índice -1.
            assertSame(elena, turnos.avancar());
            assertSame(davi, turnos.avancar());
        }

        @Test
        @DisplayName("inverter duas vezes devolve ao sentido original")
        void inverterDuasVezesVoltaAoOriginal() {
            GerenciadorDeTurnos turnos = comCinco();
            turnos.inverterSentido();
            turnos.inverterSentido();

            assertEquals(SentidoDeRotacao.HORARIO, turnos.sentido());
            assertSame(bruno, turnos.avancar());
        }
    }

    @Nested
    @DisplayName("Pulo de jogador")
    class Pulo {

        @Test
        @DisplayName("pular um salta exatamente um jogador")
        void pularUm() {
            GerenciadorDeTurnos turnos = comCinco();
            turnos.pularProximos(1);
            assertSame(carla, turnos.avancar());
        }

        @Test
        @DisplayName("o pulo é consumido: o avanço seguinte volta ao normal")
        void puloEhConsumido() {
            GerenciadorDeTurnos turnos = comCinco();
            turnos.pularProximos(1);
            turnos.avancar();
            assertSame(davi, turnos.avancar());
        }

        @Test
        @DisplayName("pulos acumulam enquanto não são consumidos")
        void pulosAcumulam() {
            GerenciadorDeTurnos turnos = comCinco();
            turnos.pularProximos(1);
            turnos.pularProximos(1);
            assertSame(davi, turnos.avancar());
        }

        @Test
        @DisplayName("pulo e inversão combinam sem estourar o índice")
        void puloComInversao() {
            GerenciadorDeTurnos turnos = comCinco();
            turnos.inverterSentido();
            turnos.pularProximos(2);
            // De Ana (0), recuando 3 posições em 5 jogadores: chega em Carla (2).
            assertSame(carla, turnos.avancar());
        }

        @Test
        @DisplayName("pular quantidade negativa é rejeitado")
        void puloNegativoEhRejeitado() {
            assertThrows(IllegalArgumentException.class, () -> comCinco().pularProximos(-1));
        }
    }

    @Nested
    @DisplayName("Validação da construção")
    class Validacao {

        @Test
        @DisplayName("lista nula é rejeitada")
        void listaNula() {
            assertThrows(NullPointerException.class, () -> new GerenciadorDeTurnos(null));
        }

        @Test
        @DisplayName("jogador nulo na lista é rejeitado")
        void jogadorNulo() {
            List<Jogador> comNulo = Arrays.asList(ana, null);
            assertThrows(NullPointerException.class, () -> new GerenciadorDeTurnos(comNulo));
        }

        @Test
        @DisplayName("menos de dois jogadores é rejeitado")
        void menosDeDois() {
            assertThrows(IllegalArgumentException.class,
                    () -> new GerenciadorDeTurnos(List.of(ana)));
            assertThrows(IllegalArgumentException.class,
                    () -> new GerenciadorDeTurnos(List.of()));
        }

        @Test
        @DisplayName("o mesmo jogador duas vezes na ordem é rejeitado")
        void jogadorRepetido() {
            assertThrows(IllegalArgumentException.class,
                    () -> new GerenciadorDeTurnos(List.of(ana, bruno, ana)));
        }
    }

    @Nested
    @DisplayName("Encapsulamento da coleção interna (requisito 7)")
    class Encapsulamento {

        @Test
        @DisplayName("jogadores() não pode ser modificada por fora")
        void listaDevolvidaEhImutavel() {
            List<Jogador> devolvida = comDois().jogadores();
            assertThrows(UnsupportedOperationException.class, () -> devolvida.add(carla));
            assertThrows(UnsupportedOperationException.class, () -> devolvida.remove(0));
        }

        @Test
        @DisplayName("alterar a lista original depois não afeta a ordem de turnos")
        void copiaDefensivaNaEntrada() {
            List<Jogador> original = new ArrayList<>(List.of(ana, bruno));
            GerenciadorDeTurnos turnos = new GerenciadorDeTurnos(original);

            original.add(carla);

            assertEquals(2, turnos.quantidadeDeJogadores());
            assertSame(bruno, turnos.avancar());
            assertSame(ana, turnos.avancar());
        }
    }

    @Nested
    @DisplayName("Contagem de rodadas")
    class Rodadas {

        @Test
        @DisplayName("a rodada começa em 1 e avança quando todos jogaram")
        void rodadaAvancaAposVoltaCompleta() {
            GerenciadorDeTurnos turnos = comDois();
            assertEquals(1, turnos.rodada());

            turnos.avancar();
            assertEquals(1, turnos.rodada());

            turnos.avancar();
            assertEquals(2, turnos.rodada());
        }
    }
}
