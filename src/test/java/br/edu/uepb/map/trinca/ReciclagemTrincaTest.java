package br.edu.uepb.map.trinca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.ContextoDePartida;
import br.edu.uepb.map.cardgame.api.EstadoPartida;
import br.edu.uepb.map.cardgame.api.Jogador;

class ReciclagemTrincaTest {

    @Test
    void deveMoverTodoDescarteEmbaralharESepararUmNovoTopo() {
        CartaTrinca primeira = new CartaTrinca(Valor.AS, Naipe.COPAS);
        CartaTrinca segunda = new CartaTrinca(Valor.DOIS, Naipe.OUROS);
        CartaTrinca terceira = new CartaTrinca(Valor.TRES, Naipe.PAUS);
        MesaTrinca mesa = new MesaTrinca();
        mesa.descartar(primeira);
        mesa.descartar(segunda);
        mesa.descartar(terceira);
        ContextoFalso contexto = new ContextoFalso();

        MotorTrinca.reciclarDescarte(contexto, mesa);

        assertTrue(contexto.embaralhado);
        assertEquals(2, contexto.quantidadeNoBaralho());
        assertEquals(1, mesa.quantidadeNoDescarte());
        Set<UUID> cartasDepoisDaReciclagem = new HashSet<>(
                contexto.cartasNoBaralho.stream().map(CartaTrinca::id).toList());
        cartasDepoisDaReciclagem.add(mesa.topoDoDescarte().orElseThrow().id());
        assertEquals(Set.of(primeira.id(), segunda.id(), terceira.id()),
                cartasDepoisDaReciclagem);
    }

    private static final class ContextoFalso implements ContextoDePartida<CartaTrinca> {
        private final List<CartaTrinca> cartasNoBaralho = new ArrayList<>();
        private boolean embaralhado;

        @Override
        public CartaTrinca comprarDoBaralho() {
            if (!embaralhado) {
                throw new AssertionError("O descarte deve ser embaralhado antes do novo topo.");
            }
            return cartasNoBaralho.removeFirst();
        }

        @Override
        public void adicionarAoBaralho(Collection<? extends CartaTrinca> cartas) {
            cartasNoBaralho.addAll(cartas);
        }

        @Override
        public void embaralharBaralho() {
            Collections.reverse(cartasNoBaralho);
            embaralhado = true;
        }

        @Override
        public int quantidadeNoBaralho() {
            return cartasNoBaralho.size();
        }

        @Override
        public EstadoPartida estado() {
            return EstadoPartida.EM_ANDAMENTO;
        }

        @Override
        public List<Jogador> jogadores() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Jogador jogadorAtual() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<CartaTrinca> maoDe(Jogador jogador) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long numeroDoTurno() {
            return 1;
        }

        @Override
        public void adicionarNaMao(Jogador jogador, CartaTrinca carta) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CartaTrinca removerDaMao(Jogador jogador, UUID cartaId) {
            throw new UnsupportedOperationException();
        }
    }
}
