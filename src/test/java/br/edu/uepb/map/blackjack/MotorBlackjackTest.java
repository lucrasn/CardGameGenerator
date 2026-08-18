package br.edu.uepb.map.blackjack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

import org.junit.jupiter.api.Test;

import br.edu.uepb.map.blackjack.evento.CartaPedidaBlackjack;
import br.edu.uepb.map.blackjack.evento.MaoDaCasaReveladaBlackjack;
import br.edu.uepb.map.blackjack.evento.ParticipanteParouBlackjack;
import br.edu.uepb.map.cardgame.api.Baralho;
import br.edu.uepb.map.cardgame.api.EstadoPartida;
import br.edu.uepb.map.cardgame.api.EventoDePartida;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.JogadorPadrao;
import br.edu.uepb.map.cardgame.api.excecao.BaralhoVazioException;

class MotorBlackjackTest {

    @Test
    void deveEncerrarSemTurnosQuandoJogadorReceberBlackjackNatural() {
        Jogador jogador = new JogadorPadrao("Você", contexto -> {
            throw new AssertionError("Uma mão natural não deve solicitar decisão.");
        });
        Jogador casa = new JogadorPadrao("Casa", new EstrategiaCasaBlackjack());
        MotorBlackjack motor = motorComCartas(jogador, casa,
                carta(ValorBlackjack.AS),
                carta(ValorBlackjack.NOVE),
                carta(ValorBlackjack.REI),
                carta(ValorBlackjack.OITO));

        var resultado = motor.executar();

        assertEquals(EstadoPartida.FINALIZADA, motor.estado());
        assertEquals(List.of(jogador), resultado.vencedores());
        assertEquals(MotivoBlackjack.BLACKJACK_DO_JOGADOR, resultado.motivo());
        assertEquals(2, motor.maoFinalDe(jogador).size());
        assertEquals(2, motor.maoFinalDe(casa).size());
    }

    @Test
    void deveFazerACasaComprarAteDezesseteECompararAsMaos() {
        Jogador jogador = new JogadorPadrao("Você", contexto -> AcaoBlackjack.PARAR);
        Jogador casa = new JogadorPadrao("Casa", new EstrategiaCasaBlackjack());
        MotorBlackjack motor = motorComCartas(jogador, casa,
                carta(ValorBlackjack.DEZ),
                carta(ValorBlackjack.CINCO),
                carta(ValorBlackjack.OITO),
                carta(ValorBlackjack.NOVE),
                carta(ValorBlackjack.TRES));
        java.util.ArrayList<EventoDePartida> eventos = new java.util.ArrayList<>();
        motor.adicionarListener(eventos::add);

        var resultado = motor.executar();

        assertEquals(List.of(jogador), resultado.vencedores());
        assertEquals(MotivoBlackjack.MAIOR_PONTUACAO, resultado.motivo());
        assertEquals(3, motor.maoFinalDe(casa).size());
        assertEquals(17, PontuacaoDaMaoBlackjack.calcular(
                motor.maoFinalDe(casa)).total());
        assertTrue(eventos.stream().anyMatch(MaoDaCasaReveladaBlackjack.class::isInstance));
        assertTrue(eventos.stream().anyMatch(CartaPedidaBlackjack.class::isInstance));
        assertTrue(eventos.stream().anyMatch(evento ->
                evento instanceof ParticipanteParouBlackjack parada
                        && parada.papel() == PapelBlackjack.CASA));
    }

    @Test
    void deveEncerrarImediatamenteQuandoJogadorPedirEEstourar() {
        Jogador jogador = new JogadorPadrao(
                "Você", contexto -> contexto.jogadasPermitidas().getFirst());
        Jogador casa = new JogadorPadrao("Casa", new EstrategiaCasaBlackjack());
        CartaBlackjack cartaDoEstouro = carta(ValorBlackjack.DOIS);
        MotorBlackjack motor = motorComCartas(jogador, casa,
                carta(ValorBlackjack.REI),
                carta(ValorBlackjack.CINCO),
                carta(ValorBlackjack.DAMA),
                carta(ValorBlackjack.NOVE),
                cartaDoEstouro);

        var resultado = motor.executar();

        assertEquals(List.of(casa), resultado.vencedores());
        assertEquals(MotivoBlackjack.JOGADOR_ESTOUROU, resultado.motivo());
        assertEquals(22, PontuacaoDaMaoBlackjack.calcular(
                motor.maoFinalDe(jogador)).total());
        assertEquals(cartaDoEstouro.id(),
                motor.ultimaCartaCompradaPor(jogador).orElseThrow().id());
    }

    @Test
    void deveProduzirEmpateSemConcederPontos() {
        Jogador jogador = new JogadorPadrao("Você", contexto -> AcaoBlackjack.PARAR);
        Jogador casa = new JogadorPadrao("Casa", new EstrategiaCasaBlackjack());
        MotorBlackjack motor = motorComCartas(jogador, casa,
                carta(ValorBlackjack.DEZ),
                carta(ValorBlackjack.REI),
                carta(ValorBlackjack.OITO),
                carta(ValorBlackjack.OITO));

        var resultado = motor.executar();

        assertTrue(resultado.houveEmpate());
        assertEquals(MotivoBlackjack.PONTUACOES_IGUAIS, resultado.motivo());
        assertEquals(0, resultado.pontuacaoDe(jogador).orElseThrow());
        assertEquals(0, resultado.pontuacaoDe(casa).orElseThrow());
    }

    @Test
    void naoDeveExporMaoFinalAntesDaExecucao() {
        Jogador jogador = new JogadorPadrao("Você", contexto -> AcaoBlackjack.PARAR);
        Jogador casa = new JogadorPadrao("Casa", new EstrategiaCasaBlackjack());
        MotorBlackjack motor = MotorBlackjack.criar(jogador, casa);

        assertThrows(IllegalStateException.class, () -> motor.maoFinalDe(jogador));
        assertThrows(IllegalStateException.class, motor::maosFinais);
    }

    @Test
    void devePublicarNoEventoACartaEAPontuacaoAposACompra() {
        Jogador jogador = new JogadorPadrao(
                "Você", contexto -> contexto.jogadasPermitidas().getFirst());
        Jogador casa = new JogadorPadrao("Casa", new EstrategiaCasaBlackjack());
        CartaBlackjack comprada = carta(ValorBlackjack.DOIS);
        MotorBlackjack motor = motorComCartas(jogador, casa,
                carta(ValorBlackjack.REI),
                carta(ValorBlackjack.CINCO),
                carta(ValorBlackjack.DAMA),
                carta(ValorBlackjack.NOVE),
                comprada);
        java.util.ArrayList<EventoDePartida> eventos = new java.util.ArrayList<>();
        motor.adicionarListener(eventos::add);

        motor.executar();

        CartaPedidaBlackjack evento = eventos.stream()
                .filter(CartaPedidaBlackjack.class::isInstance)
                .map(CartaPedidaBlackjack.class::cast)
                .findFirst()
                .orElseThrow();
        assertEquals(comprada.id(), evento.carta().id());
        assertEquals(22, evento.pontuacao().total());
        assertEquals(PapelBlackjack.JOGADOR, evento.papel());
        assertInstanceOf(CartaBlackjack.class, evento.maoAtual().getLast());
    }

    private static MotorBlackjack motorComCartas(
            Jogador jogador, Jogador casa, CartaBlackjack... cartas) {
        List<CartaBlackjack> ordem = List.of(cartas);
        return MotorBlackjack.criar(
                jogador, casa, () -> new BaralhoOrdenado(ordem));
    }

    private static CartaBlackjack carta(ValorBlackjack valor) {
        return new CartaBlackjack(valor, NaipeBlackjack.ESPADAS);
    }

    private static final class BaralhoOrdenado implements Baralho<CartaBlackjack> {
        private final Deque<CartaBlackjack> cartas;

        private BaralhoOrdenado(List<CartaBlackjack> cartas) {
            this.cartas = new ArrayDeque<>(cartas);
        }

        @Override
        public int quantidade() {
            return cartas.size();
        }

        @Override
        public Optional<CartaBlackjack> topo() {
            return Optional.ofNullable(cartas.peekFirst());
        }

        @Override
        public CartaBlackjack comprar() {
            CartaBlackjack carta = cartas.pollFirst();
            if (carta == null) {
                throw new BaralhoVazioException("Baralho de teste vazio.");
            }
            return carta;
        }

        @Override
        public void colocarNoTopo(CartaBlackjack carta) {
            cartas.addFirst(carta);
        }

        @Override
        public void colocarNaBase(CartaBlackjack carta) {
            cartas.addLast(carta);
        }

        @Override
        public void embaralhar(RandomGenerator gerador) {
            // Ordem deliberadamente fixa para o teste de integração.
        }

        @Override
        public List<CartaBlackjack> cartas() {
            return List.copyOf(cartas);
        }
    }
}
