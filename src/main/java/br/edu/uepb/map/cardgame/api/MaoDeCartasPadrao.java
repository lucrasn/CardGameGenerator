package br.edu.uepb.map.cardgame.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Implementação reutilizável de {@link MaoDeCartas} baseada em lista encapsulada.
 *
 * <p>A classe não é segura para acesso concorrente.
 *
 * @param <C> tipo de carta da mão
 * @author Júlio
 * @version 0.0.1
 */
public final class MaoDeCartasPadrao<C extends Carta> implements MaoDeCartas<C> {

    private final List<C> cartas;

    /** Cria uma mão vazia. */
    public MaoDeCartasPadrao() {
        this.cartas = new ArrayList<>();
    }

    /**
     * Cria uma mão a partir de uma cópia das cartas informadas.
     *
     * @param cartas cartas iniciais
     * @throws NullPointerException se a coleção, uma carta ou um ID for nulo
     * @throws IllegalArgumentException se houver identificadores repetidos
     */
    public MaoDeCartasPadrao(Collection<? extends C> cartas) {
        Objects.requireNonNull(cartas, "A coleção de cartas não pode ser nula.");
        this.cartas = new ArrayList<>();
        Set<UUID> identificadores = new HashSet<>();

        for (C carta : cartas) {
            validarCartaNova(carta, identificadores);
            this.cartas.add(carta);
        }
    }

    @Override
    public int quantidade() {
        return cartas.size();
    }

    @Override
    public void adicionar(C carta) {
        validarCartaNova(carta, identificadoresAtuais());
        cartas.add(carta);
    }

    @Override
    public Optional<C> buscar(UUID id) {
        Objects.requireNonNull(id, "O ID da carta não pode ser nulo.");
        return cartas.stream().filter(carta -> carta.id().equals(id)).findFirst();
    }

    @Override
    public C remover(UUID id) {
        Objects.requireNonNull(id, "O ID da carta não pode ser nulo.");
        for (int indice = 0; indice < cartas.size(); indice++) {
            if (cartas.get(indice).id().equals(id)) {
                return cartas.remove(indice);
            }
        }
        throw new NoSuchElementException("A carta " + id + " não pertence à mão.");
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
            throw new IllegalArgumentException("Já existe uma carta com o ID " + id + " na mão.");
        }
    }
}
