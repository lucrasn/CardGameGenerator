package br.edu.uepb.map.cardgame.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import br.edu.uepb.map.cardgame.api.Baralho;
import br.edu.uepb.map.cardgame.api.Carta;
import br.edu.uepb.map.cardgame.api.ContextoDeDistribuicao;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.MaoDeCartas;

/** Implementação interna que associa cada jogador à sua mão durante a distribuição. */
final class ContextoDeDistribuicaoPadrao<C extends Carta>
        implements ContextoDeDistribuicao<C> {

    private final Baralho<C> baralho;
    private final List<Jogador> jogadores;
    private final List<MaoDeCartas<C>> maos;

    ContextoDeDistribuicaoPadrao(
            Baralho<C> baralho,
            List<? extends Jogador> jogadores,
            List<? extends MaoDeCartas<C>> maos) {
        this.baralho = Objects.requireNonNull(baralho, "O baralho não pode ser nulo.");
        Objects.requireNonNull(jogadores, "A lista de jogadores não pode ser nula.");
        Objects.requireNonNull(maos, "A lista de mãos não pode ser nula.");
        this.jogadores = List.copyOf(jogadores);
        this.maos = List.copyOf(maos);

        if (this.jogadores.size() != this.maos.size()) {
            throw new IllegalArgumentException("Cada jogador deve possuir exatamente uma mão.");
        }
        rejeitarReferenciasRepetidas(this.jogadores, "jogador");
        rejeitarReferenciasRepetidas(this.maos, "mão");
        validarLocalizacaoUnicaDasCartas();
    }

    @Override
    public List<Jogador> jogadores() {
        return jogadores;
    }

    @Override
    public int cartasDisponiveis() {
        return baralho.quantidade();
    }

    @Override
    public void entregarProximaCarta(Jogador jogador) {
        Objects.requireNonNull(jogador, "O jogador não pode ser nulo.");
        for (int indice = 0; indice < jogadores.size(); indice++) {
            if (jogadores.get(indice) == jogador) {
                maos.get(indice).adicionar(baralho.comprar());
                return;
            }
        }
        throw new IllegalArgumentException("O jogador informado não pertence à distribuição.");
    }

    private static void rejeitarReferenciasRepetidas(List<?> elementos, String nome) {
        Set<Object> vistos = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Object elemento : elementos) {
            if (!vistos.add(elemento)) {
                throw new IllegalArgumentException("A mesma referência de " + nome + " foi repetida.");
            }
        }
    }

    private void validarLocalizacaoUnicaDasCartas() {
        Set<UUID> identificadores = new HashSet<>();
        baralho.cartas().forEach(carta -> adicionarIdentidade(carta.id(), identificadores));
        maos.forEach(mao -> mao.cartas()
                .forEach(carta -> adicionarIdentidade(carta.id(), identificadores)));
    }

    private static void adicionarIdentidade(UUID id, Set<UUID> identificadores) {
        Objects.requireNonNull(id, "O ID da carta não pode ser nulo.");
        if (!identificadores.add(id)) {
            throw new IllegalArgumentException(
                    "A carta " + id + " aparece em mais de uma zona da partida.");
        }
    }
}
