package br.edu.uepb.map.cardgame.api.apoio;

import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.EstrategiaDeDecisao;

import java.util.UUID;

/**
 * Dublê de teste para {@link Jogador}, usado pelos testes de {@code api}.
 *
 * <p>Existe para que os testes de distribuição e de regras exercitem os contratos sem
 * depender da implementação real de jogador, que pertence a outra trilha. Fica em
 * {@code src/test}, portanto não entra no artefato de produção.
 *
 * <p>Escrito à mão em vez de gerado por biblioteca de <em>mock</em>: evita mais uma
 * dependência no {@code pom.xml} e é mais simples de explicar na defesa.
 *
 * <p>Não sobrescreve {@code equals}/{@code hashCode} de propósito — a igualdade por
 * identidade é justamente o que os testes de ordem de turnos precisam verificar.
 */
public final class JogadorFalso implements Jogador {

    private static final EstrategiaDeDecisao SEM_DECISAO = contexto -> {
        throw new UnsupportedOperationException("dublê não toma decisões");
    };

    private final UUID id = UUID.randomUUID();
    private final String nome;

    /**
     * @param nome rótulo usado apenas para tornar as falhas de teste legíveis
     */
    public JogadorFalso(String nome) {
        this.nome = nome;
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public String nome() {
        return nome;
    }

    @Override
    public EstrategiaDeDecisao estrategiaDeDecisao() {
        return SEM_DECISAO;
    }

    @Override
    public String toString() {
        return nome;
    }
}
