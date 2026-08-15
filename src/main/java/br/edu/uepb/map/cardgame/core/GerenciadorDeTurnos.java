package br.edu.uepb.map.cardgame.core;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import br.edu.uepb.map.cardgame.api.Jogador;

/**
 * Controla de quem é a vez: mantém a ordem dos jogadores, o sentido de rotação e os
 * pulos pendentes.
 *
 * <p><strong>Fronteira de responsabilidade.</strong> Esta classe <em>executa</em>
 * inversão de sentido e pulo de jogador, mas nunca <em>decide</em> quando eles devem
 * acontecer — isso é regra de jogo, e chega de fora através do {@link MotorDePartida}.
 * A separação é deliberada: fluxo da partida e ordem de turnos são dois motivos de
 * mudança distintos (SRP), e juntá-los produziria a classe-Deus típica que o princípio
 * GRASP de Alta Coesão existe para evitar.
 *
 * <p><strong>Partidas de N jogadores.</strong> O enunciado pede partidas de dois
 * jogadores <em>com abertura para mais</em>. O avanço é feito por aritmética modular
 * com {@link Math#floorMod(int, int)}, sem nenhum desvio condicional sobre a
 * quantidade de jogadores — a mesma linha de código funciona para 2 e para 8.
 * Usa-se {@code floorMod} e não o operador {@code %} porque o sentido anti-horário
 * produz índice negativo, para o qual {@code %} devolveria resto negativo.
 *
 * <p><strong>Encapsulamento.</strong> A lista recebida é copiada na entrada e nunca
 * devolvida: {@link #jogadores()} entrega uma visão imutável. Alterar a lista original
 * depois de construir o gerenciador não afeta a ordem de turnos.
 *
 * @author Lucas N. de Araújo
 * @version 0.0.1
 * @since 2026-06-15
 */
public class GerenciadorDeTurnos {

    /** Mínimo exigido pelo enunciado; a implementação não impõe limite superior. */
    private static final int MINIMO_DE_JOGADORES = 2;

    private final List<Jogador> jogadores;

    private int indiceAtual;
    private SentidoDeRotacao sentido;
    private int pulosPendentes;
    private int turnosExecutados;

    /**
     * Monta a ordem de turnos a partir da lista informada, iniciando pelo primeiro
     * jogador e no sentido {@link SentidoDeRotacao#HORARIO}.
     *
     * <p>A duplicidade é verificada por <em>identidade</em> (mesma referência), e não
     * por {@code equals}: o contrato de igualdade de {@link Jogador} pertence à
     * abstração de jogador, e este gerenciador não deve depender de como ele foi
     * definido. O mesmo objeto aparecer duas vezes na ordem é sempre um defeito.
     *
     * @param jogadores ordem inicial dos jogadores; ao menos dois, sem nulos e sem repetições
     * @throws NullPointerException se a lista ou algum de seus elementos for {@code null}
     * @throws IllegalArgumentException se houver menos de dois jogadores ou repetição
     */
    public GerenciadorDeTurnos(List<Jogador> jogadores) {
        Objects.requireNonNull(jogadores, "A lista de jogadores não pode ser nula.");
        // List.copyOf já rejeita elementos nulos e produz uma cópia imutável, o que
        // atende à cópia defensiva de entrada e ao encapsulamento de saída de uma vez.
        List<Jogador> copia = List.copyOf(jogadores);

        if (copia.size() < MINIMO_DE_JOGADORES) {
            throw new IllegalArgumentException(
                    "Uma partida exige ao menos " + MINIMO_DE_JOGADORES
                            + " jogadores, mas foram informados " + copia.size() + ".");
        }
        Set<Jogador> distintos = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Jogador jogador : copia) {
            if (!distintos.add(jogador)) {
                throw new IllegalArgumentException(
                        "O mesmo jogador foi informado mais de uma vez na ordem de turnos.");
            }
        }

        this.jogadores = copia;
        this.indiceAtual = 0;
        this.sentido = SentidoDeRotacao.HORARIO;
        this.pulosPendentes = 0;
        this.turnosExecutados = 0;
    }

    /**
     * Jogador de quem é a vez neste momento.
     *
     * @return o jogador atual, nunca {@code null}
     */
    public Jogador jogadorAtual() {
        return jogadores.get(indiceAtual);
    }

    /**
     * Passa a vez, aplicando o sentido corrente e consumindo os pulos pendentes.
     *
     * @return o jogador que passou a ser o atual
     */
    public Jogador avancar() {
        int deslocamento = sentido.passo() * (1 + pulosPendentes);
        indiceAtual = Math.floorMod(indiceAtual + deslocamento, jogadores.size());
        pulosPendentes = 0;
        turnosExecutados++;
        return jogadorAtual();
    }

    /**
     * Inverte o sentido em que a vez circula. Passa a valer no próximo
     * {@link #avancar()}.
     */
    public void inverterSentido() {
        sentido = sentido.inverso();
    }

    /**
     * Agenda o salto de um ou mais jogadores no próximo {@link #avancar()}.
     *
     * <p>Chamadas sucessivas acumulam antes de serem consumidas. Quantidades maiores
     * ou iguais ao número de jogadores dão a volta na mesa, o que é comportamento
     * legítimo e fica a cargo da regra do jogo evitar, se não for o desejado.
     *
     * @param quantidade número de jogadores a saltar; zero ou positivo
     * @throws IllegalArgumentException se {@code quantidade} for negativa
     */
    public void pularProximos(int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException(
                    "A quantidade de jogadores a pular não pode ser negativa: " + quantidade + ".");
        }
        pulosPendentes += quantidade;
    }

    /**
     * Sentido em que a vez está circulando.
     *
     * @return o sentido corrente
     */
    public SentidoDeRotacao sentido() {
        return sentido;
    }

    /**
     * Ordem dos jogadores, como visão imutável.
     *
     * @return lista imutável na ordem informada na construção; tentativas de
     *         modificação resultam em {@link UnsupportedOperationException}
     */
    public List<Jogador> jogadores() {
        return jogadores;
    }

    /**
     * Quantidade de jogadores na partida.
     *
     * @return número de jogadores, sempre maior ou igual a dois
     */
    public int quantidadeDeJogadores() {
        return jogadores.size();
    }

    /**
     * Número da rodada corrente, contada a partir de um.
     *
     * <p>Trata-se de um valor <em>nominal</em>: é derivado da quantidade de turnos
     * jogados dividida pelo número de jogadores. Quando há pulos, jogadores saltados
     * não jogam na rodada, mas a rodada avança do mesmo jeito.
     *
     * @return o número da rodada corrente, começando em 1
     */
    public int rodada() {
        return turnosExecutados / jogadores.size() + 1;
    }
}
