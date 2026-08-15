package br.edu.uepb.map.cardgame.apoio;

import java.util.Objects;
import java.util.UUID;

import br.edu.uepb.map.cardgame.api.EstrategiaDeDecisao;
import br.edu.uepb.map.cardgame.api.Jogador;

/** Dublê da Trilha A que permite controlar a identidade lógica nos testes. */
public record JogadorDeTeste(UUID id, String nome) implements Jogador {

    private static final EstrategiaDeDecisao SEM_DECISAO = contexto -> {
        throw new UnsupportedOperationException("O dublê não toma decisões.");
    };

    public JogadorDeTeste {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(nome, "nome");
    }

    /** Cria um jogador com identidade aleatória. */
    public JogadorDeTeste(String nome) {
        this(UUID.randomUUID(), nome);
    }

    @Override
    public EstrategiaDeDecisao estrategiaDeDecisao() {
        return SEM_DECISAO;
    }
}
