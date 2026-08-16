package br.edu.uepb.map.cardgame.engine;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import br.edu.uepb.map.cardgame.api.Baralho;
import br.edu.uepb.map.cardgame.api.Carta;
import br.edu.uepb.map.cardgame.api.ContextoDePartida;
import br.edu.uepb.map.cardgame.api.EstadoPartida;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.MaoDeCartas;
import br.edu.uepb.map.cardgame.api.MaoDeCartasPadrao;
import br.edu.uepb.map.cardgame.api.excecao.EstadoDePartidaInvalidoException;

/**
 * Estado mutável interno de uma execução.
 *
 * <p>A classe implementa a porta pública usada pelo jogo, mas permanece sem
 * {@code public}. Assim, o cliente conhece operações controladas, não a estrutura
 * usada para armazenar baralho, mãos, turnos e ciclo de vida.
 *
 * @param <C> tipo de carta usado pela partida
 *
 * @author Lucas N. de Araújo
 * @version 0.0.1
 */
final class PartidaEmExecucao<C extends Carta> implements ContextoDePartida<C> {

    private final List<Jogador> jogadores;
    private final Baralho<C> baralho;
    private final Map<UUID, MaoDeCartas<C>> maos;
    private final GerenciadorDeTurnos turnos;
    private final CicloDeVidaDaPartida ciclo;
    private long numeroDoTurno;

    /**
     * Monta o estado mutável de uma execução e cria uma mão vazia por jogador.
     *
     * @param jogadores participantes da partida
     * @param baralho baralho exclusivo da execução
     * @param turnos gerenciador de ordem e rotação dos jogadores
     * @param ciclo controlador do estado do ciclo de vida
     * @throws NullPointerException se algum argumento, jogador ou identificador for nulo
     * @throws IllegalArgumentException se dois jogadores tiverem o mesmo identificador
     */
    PartidaEmExecucao(List<Jogador> jogadores,
                      Baralho<C> baralho,
                      GerenciadorDeTurnos turnos,
                      CicloDeVidaDaPartida ciclo) {
        this.jogadores = List.copyOf(
                Objects.requireNonNull(jogadores, "A lista de jogadores não pode ser nula."));
        this.baralho = Objects.requireNonNull(baralho, "O baralho não pode ser nulo.");
        this.turnos = Objects.requireNonNull(turnos, "O gerenciador de turnos não pode ser nulo.");
        this.ciclo = Objects.requireNonNull(ciclo, "O ciclo de vida não pode ser nulo.");
        this.maos = criarMaos(this.jogadores);
    }

    private static <C extends Carta> Map<UUID, MaoDeCartas<C>> criarMaos(
            List<Jogador> jogadores) {
        Map<UUID, MaoDeCartas<C>> novasMaos = new LinkedHashMap<>();
        for (Jogador jogador : jogadores) {
            UUID id = Objects.requireNonNull(
                    jogador.id(), "A identidade do jogador não pode ser nula.");
            if (novasMaos.putIfAbsent(id, new MaoDeCartasPadrao<>()) != null) {
                throw new IllegalArgumentException("Há jogadores repetidos na partida: " + id + ".");
            }
        }
        return novasMaos;
    }

    @Override
    public EstadoPartida estado() {
        return ciclo.estado();
    }

    @Override
    public List<Jogador> jogadores() {
        return jogadores;
    }

    @Override
    public Jogador jogadorAtual() {
        return turnos.jogadorAtual();
    }

    @Override
    public List<C> maoDe(Jogador jogador) {
        return localizarMao(jogador).cartas();
    }

    @Override
    public int quantidadeNoBaralho() {
        return baralho.quantidade();
    }

    @Override
    public long numeroDoTurno() {
        return numeroDoTurno;
    }

    @Override
    public C comprarDoBaralho() {
        exigirMutavel();
        return baralho.comprar();
    }

    @Override
    public void adicionarNaMao(Jogador jogador, C carta) {
        exigirMutavel();
        MaoDeCartas<C> mao = localizarMao(jogador);
        validarCartaForaDaPartida(carta);
        mao.adicionar(carta);
    }

    @Override
    public C removerDaMao(Jogador jogador, UUID cartaId) {
        exigirMutavel();
        return localizarMao(jogador).remover(
                Objects.requireNonNull(cartaId, "A identidade da carta não pode ser nula."));
    }

    @Override
    public void adicionarAoBaralho(Collection<? extends C> cartas) {
        exigirMutavel();
        Objects.requireNonNull(cartas, "A coleção de cartas não pode ser nula.");
        List<C> copia = List.copyOf(cartas);
        Set<UUID> novosIds = new HashSet<>();
        for (C carta : copia) {
            UUID id = validarCartaForaDaPartida(carta);
            if (!novosIds.add(id)) {
                throw new IllegalArgumentException(
                        "A coleção repete a identidade de uma carta: " + id + ".");
            }
        }
        copia.forEach(baralho::colocarNaBase);
    }

    @Override
    public void embaralharBaralho() {
        exigirMutavel();
        baralho.embaralhar();
    }

    /**
     * Compra a próxima carta e a entrega diretamente à mão indicada.
     *
     * @param jogador destinatário da carta
     * @throws br.edu.uepb.map.cardgame.api.excecao.BaralhoVazioException se o baralho estiver vazio
     * @throws EstadoDePartidaInvalidoException se o estado atual não permitir mutação
     * @throws NullPointerException se {@code jogador} ou seu identificador for nulo
     * @throws IllegalArgumentException se o jogador não pertencer à partida
     */
    void entregarProximaCarta(Jogador jogador) {
        exigirMutavel();
        MaoDeCartas<C> mao = localizarMao(jogador);
        C carta = baralho.comprar();
        mao.adicionar(carta);
    }

    /**
     * Atualiza o número ordinal exposto pela visão da partida.
     *
     * @param numeroDoTurno novo ordinal, maior ou igual a zero
     * @throws IllegalArgumentException se o valor for negativo
     */
    void definirNumeroDoTurno(long numeroDoTurno) {
        if (numeroDoTurno < 0) {
            throw new IllegalArgumentException("O número do turno não pode ser negativo.");
        }
        this.numeroDoTurno = numeroDoTurno;
    }

    private MaoDeCartas<C> localizarMao(Jogador jogador) {
        Objects.requireNonNull(jogador, "O jogador não pode ser nulo.");
        UUID id = Objects.requireNonNull(
                jogador.id(), "A identidade do jogador não pode ser nula.");
        MaoDeCartas<C> mao = maos.get(id);
        if (mao == null) {
            throw new IllegalArgumentException("O jogador não pertence a esta partida.");
        }
        return mao;
    }

    private UUID validarCartaForaDaPartida(C carta) {
        Objects.requireNonNull(carta, "A carta não pode ser nula.");
        UUID id = Objects.requireNonNull(carta.id(), "A identidade da carta não pode ser nula.");
        boolean noBaralho = baralho.cartas().stream().anyMatch(atual -> atual.id().equals(id));
        boolean emMao = maos.values().stream().anyMatch(mao -> mao.contem(id));
        if (noBaralho || emMao) {
            throw new IllegalArgumentException("A carta já pertence à partida: " + id + ".");
        }
        return id;
    }

    private void exigirMutavel() {
        EstadoPartida estado = ciclo.estado();
        if (estado != EstadoPartida.PREPARANDO && estado != EstadoPartida.EM_ANDAMENTO) {
            throw new EstadoDePartidaInvalidoException(
                    "O estado " + estado + " não permite modificar cartas.");
        }
    }
}
