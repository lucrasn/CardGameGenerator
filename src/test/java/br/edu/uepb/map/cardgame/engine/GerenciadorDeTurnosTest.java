package br.edu.uepb.map.cardgame.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.apoio.JogadorDeTeste;

@DisplayName("GerenciadorDeTurnos — implementação interna")
class GerenciadorDeTurnosTest {

    private final Jogador ana = new JogadorDeTeste("Ana");
    private final Jogador bruno = new JogadorDeTeste("Bruno");
    private final Jogador carla = new JogadorDeTeste("Carla");
    private final Jogador davi = new JogadorDeTeste("Davi");

    @Test
    @DisplayName("é package-private e começa no índice configurado")
    void internoEPrimeiroConfiguravel() {
        assertFalse(Modifier.isPublic(GerenciadorDeTurnos.class.getModifiers()));
        GerenciadorDeTurnos turnos = new GerenciadorDeTurnos(
                List.of(ana, bruno, carla), 2);
        assertSame(carla, turnos.jogadorAtual());
        assertSame(ana, turnos.avancar());
    }

    @Test
    @DisplayName("funciona para N jogadores e preserva cópia defensiva")
    void suportaNJogadores() {
        List<Jogador> origem = new ArrayList<>(List.of(ana, bruno, carla, davi));
        GerenciadorDeTurnos turnos = new GerenciadorDeTurnos(origem, 0);
        origem.clear();

        assertSame(bruno, turnos.avancar());
        assertSame(carla, turnos.avancar());
        assertSame(davi, turnos.avancar());
        assertSame(ana, turnos.avancar());
        assertEquals(4, turnos.jogadores().size());
        assertThrows(UnsupportedOperationException.class,
                () -> turnos.jogadores().add(new JogadorDeTeste("Extra")));
    }

    @Test
    @DisplayName("combina inversão, pulo e volta modular")
    void inverteEPula() {
        GerenciadorDeTurnos turnos = new GerenciadorDeTurnos(
                List.of(ana, bruno, carla, davi), 0);
        turnos.inverterSentido();
        turnos.pularProximos(1);

        assertEquals(SentidoDeRotacao.ANTI_HORARIO, turnos.sentido());
        assertSame(carla, turnos.avancar());
        assertSame(bruno, turnos.avancar());
    }

    @Test
    @DisplayName("rejeita jogador lógico repetido, mesmo em outro objeto")
    void rejeitaIdentidadeRepetida() {
        UUID id = UUID.randomUUID();
        Jogador original = new JogadorDeTeste(id, "Original");
        Jogador copia = new JogadorDeTeste(id, "Cópia");
        assertThrows(IllegalArgumentException.class,
                () -> new GerenciadorDeTurnos(List.of(original, copia), 0));
    }

    @Test
    @DisplayName("com dois jogadores a vez apenas alterna")
    void alternaComDoisJogadores() {
        GerenciadorDeTurnos turnos = new GerenciadorDeTurnos(List.of(ana, bruno), 0);

        assertSame(ana, turnos.jogadorAtual());
        assertSame(bruno, turnos.avancar());
        assertSame(ana, turnos.avancar());
        assertSame(bruno, turnos.avancar());
    }

    @Test
    @DisplayName("o sentido anti-horário dá a volta pelo índice zero sem índice negativo")
    void voltaModularNoSentidoInverso() {
        GerenciadorDeTurnos turnos = new GerenciadorDeTurnos(
                List.of(ana, bruno, carla, davi), 0);
        turnos.inverterSentido();

        // Sem Math.floorMod, 0 - 1 daria resto negativo e estouraria o índice da lista.
        assertSame(davi, turnos.avancar());
        assertSame(carla, turnos.avancar());
    }

    @Test
    @DisplayName("inverter duas vezes devolve o sentido original")
    void inversaoEhInvolutiva() {
        GerenciadorDeTurnos turnos = new GerenciadorDeTurnos(List.of(ana, bruno, carla), 0);
        turnos.inverterSentido();
        turnos.inverterSentido();

        assertEquals(SentidoDeRotacao.HORARIO, turnos.sentido());
        assertSame(bruno, turnos.avancar());
    }

    @Test
    @DisplayName("pulos acumulam antes de serem consumidos e zeram depois")
    void pulosAcumulamEZeram() {
        GerenciadorDeTurnos turnos = new GerenciadorDeTurnos(
                List.of(ana, bruno, carla, davi), 0);
        turnos.pularProximos(1);
        turnos.pularProximos(1);

        assertSame(davi, turnos.avancar());
        // Consumidos: o próximo avanço volta a andar de um em um.
        assertSame(ana, turnos.avancar());
    }

    @Test
    @DisplayName("pulo maior que a mesa dá a volta em vez de estourar o índice")
    void puloMaiorQueAMesa() {
        GerenciadorDeTurnos turnos = new GerenciadorDeTurnos(List.of(ana, bruno, carla), 0);
        turnos.pularProximos(3);

        // Deslocamento = 1 (a própria vez) + 3 pulos = 4 posições numa mesa de 3:
        // uma volta completa e mais um. Cabe à regra do jogo evitar, se não quiser.
        assertSame(bruno, turnos.avancar());
    }

    @Test
    @DisplayName("pular a mesa inteira menos um devolve a vez ao mesmo jogador")
    void puloDaVoltaCompleta() {
        GerenciadorDeTurnos turnos = new GerenciadorDeTurnos(List.of(ana, bruno, carla), 0);
        turnos.pularProximos(2);

        assertSame(ana, turnos.avancar());
    }

    @Test
    @DisplayName("pular zero equivale a um avanço simples")
    void pularZero() {
        GerenciadorDeTurnos turnos = new GerenciadorDeTurnos(List.of(ana, bruno, carla), 0);
        turnos.pularProximos(0);

        assertSame(bruno, turnos.avancar());
    }

    @Test
    @DisplayName("rejeita menos de dois jogadores, índice e pulo inválidos")
    void validaEntradas() {
        assertThrows(IllegalArgumentException.class,
                () -> new GerenciadorDeTurnos(List.of(ana, bruno), -1));
        assertThrows(NullPointerException.class,
                () -> new GerenciadorDeTurnos(null, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new GerenciadorDeTurnos(List.of(ana), 0));
        assertThrows(IllegalArgumentException.class,
                () -> new GerenciadorDeTurnos(List.of(ana, bruno), 2));
        GerenciadorDeTurnos turnos = new GerenciadorDeTurnos(List.of(ana, bruno), 0);
        assertThrows(IllegalArgumentException.class, () -> turnos.pularProximos(-1));
    }
}
