package br.edu.uepb.map.cardgame.api;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.random.RandomGenerator;

import br.edu.uepb.map.cardgame.api.excecao.BaralhoVazioException;

/**
 * Implementação reutilizável de {@link Baralho} com topo na primeira posição.
 *
 * <p>A classe não é segura para acesso concorrente. Partidas devem manipulá-la
 * por uma única linha de execução.
 *
 * @param <C> tipo de carta armazenado
 * @author Júlio
 */
public final class BaralhoPadrao<C extends Carta> implements Baralho<C> {

    private final Deque<C> cartas;

    /** Cria um baralho vazio. */
    public BaralhoPadrao() {
        this.cartas = new ArrayDeque<>();
    }

    /**
     * Cria um baralho cuja iteração recebida está ordenada do topo para a base.
     *
     * @param cartas cartas iniciais; a coleção é copiada defensivamente
     * @throws NullPointerException se a coleção, uma carta ou um ID for nulo
     * @throws IllegalArgumentException se houver identificadores repetidos
     */
    public BaralhoPadrao(Collection<? extends C> cartas) {
        Objects.requireNonNull(cartas, "A coleção de cartas não pode ser nula.");
        this.cartas = new ArrayDeque<>();

        Set<UUID> identificadores = new HashSet<>();
        for (C carta : cartas) {
            validarCartaNova(carta, identificadores);
            this.cartas.addLast(carta);
        }
    }

    @Override
    public int quantidade() {
        return cartas.size();
    }

    @Override
    public Optional<C> topo() {
        return Optional.ofNullable(cartas.peekFirst());
    }

    @Override
    public C comprar() {
        C carta = cartas.pollFirst();
        if (carta == null) {
            throw new BaralhoVazioException("Não há carta disponível para compra.");
        }
        return carta;
    }

    @Override
    public void colocarNoTopo(C carta) {
        validarCartaNova(carta, identificadoresAtuais());
        cartas.addFirst(carta);
    }

    @Override
    public void colocarNaBase(C carta) {
        validarCartaNova(carta, identificadoresAtuais());
        cartas.addLast(carta);
    }

    @Override
    public void embaralhar(RandomGenerator gerador) {
        Objects.requireNonNull(gerador, "O gerador aleatório não pode ser nulo.");
        List<C> embaralhadas = new ArrayList<>(cartas);

        for (int indice = embaralhadas.size() - 1; indice > 0; indice--) {
            int destino = gerador.nextInt(indice + 1);
            C temporaria = embaralhadas.get(indice);
            embaralhadas.set(indice, embaralhadas.get(destino));
            embaralhadas.set(destino, temporaria);
        }

        cartas.clear();
        cartas.addAll(embaralhadas);
    }

    @Override
    public List<C> cartas() {
        return List.copyOf(cartas);
    }

    private Set<UUID> identificadoresAtuais() {
        Set<UUID> identificadores = new HashSet<>();
        for (C carta : cartas) {
            identificadores.add(carta.id());
        }
        return identificadores;
    }

    private static void validarCartaNova(Carta carta, Set<UUID> identificadores) {
        Objects.requireNonNull(carta, "A carta não pode ser nula.");
        UUID id = Objects.requireNonNull(carta.id(), "O ID da carta não pode ser nulo.");
        if (!identificadores.add(id)) {
            throw new IllegalArgumentException("Já existe uma carta com o ID " + id + ".");
        }
    }
}
