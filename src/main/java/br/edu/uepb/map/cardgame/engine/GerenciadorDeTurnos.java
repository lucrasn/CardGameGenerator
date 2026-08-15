package br.edu.uepb.map.cardgame.engine;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import br.edu.uepb.map.cardgame.api.Jogador;

/**
 * Controla de quem é a vez: ordem dos participantes, primeiro a jogar, sentido de
 * rotação e pulos pendentes.
 *
 * <p><strong>Fronteira de responsabilidade.</strong> Esta classe <em>executa</em>
 * inversão de sentido e pulo, mas nunca <em>decide</em> quando eles acontecem — isso é
 * regra de jogo e chega de fora, pelo {@code ResultadoDoTurno} que o motor concreto
 * devolve. Fluxo da partida e ordem de turnos são dois motivos de mudança distintos
 * (SRP); juntá-los produziria a classe-Deus que o princípio GRASP de
 * <strong>Alta Coesão</strong> existe para evitar.
 *
 * <p><strong>Partidas de N jogadores.</strong> O enunciado pede partidas de dois
 * jogadores <em>com abertura para mais</em>. O avanço usa
 * {@link Math#floorMod(long, long)} e não o operador {@code %}, porque o sentido
 * anti-horário produz índice negativo, para o qual {@code %} devolveria resto negativo.
 * A mesma linha de código funciona para 2 e para 8 participantes, sem nenhum desvio
 * condicional sobre a quantidade.
 *
 * <p>Package-private de propósito: é infraestrutura do engine, não superfície pública.
 *
 * @author Lucas N. de Araújo
 * @version 0.0.1
 * @since 2026-06-15
 */
final class GerenciadorDeTurnos {

    private final List<Jogador> jogadores;
    private int indiceAtual;
    private SentidoDeRotacao sentido = SentidoDeRotacao.HORARIO;
    private long pulosPendentes;

    /**
     * Monta a ordem de turnos, começando pelo índice informado e no sentido horário.
     *
     * <p>A duplicidade é verificada por <em>identidade lógica</em> ({@code Jogador.id()})
     * e não por {@code equals}: o contrato de igualdade de jogador pertence a outra
     * trilha, e este gerenciador não deve depender de como ele foi definido.
     *
     * @param jogadores ordem inicial; ao menos dois, sem nulos e sem identidades repetidas
     * @param primeiroJogador índice de quem começa, dentro da lista
     * @throws NullPointerException se a lista, algum elemento ou algum {@code id} for nulo
     * @throws IllegalArgumentException se houver menos de dois jogadores, identidade
     *         repetida, ou o índice inicial estiver fora da lista
     */
    GerenciadorDeTurnos(List<Jogador> jogadores, int primeiroJogador) {
        this.jogadores = copiarEValidarJogadores(jogadores);
        if (primeiroJogador < 0 || primeiroJogador >= this.jogadores.size()) {
            throw new IllegalArgumentException("O índice do primeiro jogador é inválido.");
        }
        this.indiceAtual = primeiroJogador;
    }

    private static List<Jogador> copiarEValidarJogadores(List<Jogador> jogadores) {
        Objects.requireNonNull(jogadores, "A lista de jogadores não pode ser nula.");
        List<Jogador> copia = List.copyOf(jogadores);
        if (copia.size() < 2) {
            throw new IllegalArgumentException("Uma partida exige ao menos dois jogadores.");
        }
        Set<UUID> identidades = new HashSet<>();
        for (Jogador jogador : copia) {
            UUID id = Objects.requireNonNull(jogador.id(), "A identidade do jogador não pode ser nula.");
            if (!identidades.add(id)) {
                throw new IllegalArgumentException("Há jogadores repetidos na ordem de turnos.");
            }
        }
        return copia;
    }

    /**
     * Participante de quem é a vez neste momento.
     *
     * @return o jogador atual, nunca {@code null}
     */
    Jogador jogadorAtual() {
        return jogadores.get(indiceAtual);
    }

    /**
     * Passa a vez, aplicando o sentido corrente e consumindo os pulos pendentes.
     *
     * @return o participante que passou a ser o atual
     */
    Jogador avancar() {
        long deslocamento = sentido.passo() * (1L + pulosPendentes);
        indiceAtual = (int) Math.floorMod(indiceAtual + deslocamento, jogadores.size());
        pulosPendentes = 0;
        return jogadorAtual();
    }

    /**
     * Inverte o sentido em que a vez circula. Passa a valer no próximo
     * {@link #avancar()}.
     */
    void inverterSentido() {
        sentido = sentido.inverso();
    }

    /**
     * Agenda o salto de um ou mais participantes no próximo {@link #avancar()}.
     *
     * <p>Chamadas sucessivas acumulam antes de serem consumidas. Quantidades maiores ou
     * iguais ao número de participantes dão a volta na mesa, o que é comportamento
     * legítimo — cabe à regra do jogo evitar, se não for o desejado.
     *
     * @param quantidade número de participantes a saltar; zero ou positivo
     * @throws IllegalArgumentException se {@code quantidade} for negativa
     */
    void pularProximos(int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("A quantidade de jogadores a pular não pode ser negativa.");
        }
        pulosPendentes = Math.addExact(pulosPendentes, quantidade);
    }

    /**
     * Sentido em que a vez está circulando.
     *
     * @return o sentido corrente
     */
    SentidoDeRotacao sentido() {
        return sentido;
    }

    /**
     * Ordem dos participantes.
     *
     * @return lista imutável na ordem informada na construção; tentativas de
     *         modificação resultam em {@link UnsupportedOperationException}
     */
    List<Jogador> jogadores() {
        return jogadores;
    }
}
