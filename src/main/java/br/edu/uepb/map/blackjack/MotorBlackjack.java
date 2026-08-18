package br.edu.uepb.map.blackjack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import br.edu.uepb.map.blackjack.evento.CartaPedidaBlackjack;
import br.edu.uepb.map.blackjack.evento.MaoDaCasaReveladaBlackjack;
import br.edu.uepb.map.blackjack.evento.ParticipanteParouBlackjack;
import br.edu.uepb.map.cardgame.api.BaralhoFactory;
import br.edu.uepb.map.cardgame.api.ContextoDePartida;
import br.edu.uepb.map.cardgame.api.DistribuicaoAlternada;
import br.edu.uepb.map.cardgame.api.Jogada;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.PartidaConfig;
import br.edu.uepb.map.cardgame.api.ResultadoDePartida;
import br.edu.uepb.map.cardgame.api.ResultadoDoTurno;
import br.edu.uepb.map.cardgame.api.VisaoDaPartida;
import br.edu.uepb.map.cardgame.api.excecao.JogadaInvalidaException;
import br.edu.uepb.map.cardgame.engine.MotorDePartida;

/**
 * Motor concreto do Blackjack básico entre uma pessoa e a casa automatizada.
 *
 * <p>O Template Method continua pertencendo ao framework. Esta subclasse implementa
 * somente o turno variável: obter uma decisão, validá-la, comprar ou parar e devolver
 * uma diretiva. Regras, distribuição e criação do baralho entram por Strategy e
 * Factory Method na configuração.
 */
public final class MotorBlackjack extends MotorDePartida<CartaBlackjack> {

    /** Quantidade de cartas inicialmente entregue a cada participante. */
    public static final int CARTAS_INICIAIS = 2;

    private final MesaBlackjack mesa;
    private Map<Jogador, List<CartaBlackjack>> maosFinais = Map.of();

    private MotorBlackjack(
            PartidaConfig<CartaBlackjack> configuracao, MesaBlackjack mesa) {
        super(configuracao);
        this.mesa = Objects.requireNonNull(mesa, "A mesa não pode ser nula.");
    }

    /**
     * Monta uma rodada com o baralho e todas as regras oficiais do cliente.
     *
     * @param jogador participante que decide pedir ou parar
     * @param casa participante cuja Strategy deve obedecer à política da casa
     * @return motor configurado e ainda não executado
     * @throws NullPointerException se um participante, sua identidade ou sua
     *         Strategy for nulo
     * @throws IllegalArgumentException se jogador e casa tiverem a mesma identidade
     */
    public static MotorBlackjack criar(Jogador jogador, Jogador casa) {
        return criar(jogador, casa, new BaralhoBlackjackFactory());
    }

    static MotorBlackjack criar(
            Jogador jogador,
            Jogador casa,
            BaralhoFactory<CartaBlackjack> baralhoFactory) {
        MesaBlackjack mesa = new MesaBlackjack(jogador, casa);
        PartidaConfig<CartaBlackjack> configuracao =
                PartidaConfig.<CartaBlackjack>builder()
                .jogadores(List.of(jogador, casa))
                .baralhoFactory(Objects.requireNonNull(
                        baralhoFactory, "A fábrica de baralho não pode ser nula."))
                .distribuicao(new DistribuicaoAlternada<>(CARTAS_INICIAIS))
                .regraDeValidacao(new RegraValidacaoBlackjack(mesa))
                .regraDeVitoria(new RegraVitoriaBlackjack(mesa))
                .regraDePontuacao(new RegraPontuacaoBlackjack())
                .primeiroJogador(0)
                .build();
        return new MotorBlackjack(configuracao, mesa);
    }

    @Override
    protected ResultadoDoTurno executarTurno(ContextoDePartida<CartaBlackjack> contexto) {
        Jogador participante = contexto.jogadorAtual();
        revelarCasaSeNecessario(contexto, participante);

        List<Jogada> acoesPermitidas = acoesPermitidas(contexto, participante);
        Jogada escolha = participante.estrategiaDeDecisao().decidir(
                criarContextoDeDecisao(contexto, participante, acoesPermitidas));
        if (!acoesPermitidas.contains(escolha)) {
            throw new JogadaInvalidaException(
                    "A ação escolhida não está entre as opções permitidas.");
        }
        validarJogada(contexto, escolha);

        AcaoBlackjack acao = (AcaoBlackjack) escolha;
        return acao == AcaoBlackjack.PEDIR
                ? aplicarPedido(contexto, participante)
                : aplicarParada(contexto, participante);
    }

    /**
     * Consulta a mão final de um participante após a execução.
     *
     * @param participante jogador ou casa da rodada
     * @return snapshot imutável da mão final
     * @throws NullPointerException se o participante ou sua identidade for nulo
     * @throws IllegalStateException se a rodada ainda não terminou
     * @throws IllegalArgumentException se o participante não pertencer à rodada
     */
    public List<CartaBlackjack> maoFinalDe(Jogador participante) {
        Objects.requireNonNull(participante, "O participante não pode ser nulo.");
        Objects.requireNonNull(participante.id(), "O ID do participante não pode ser nulo.");
        if (maosFinais.isEmpty()) {
            throw new IllegalStateException("A rodada ainda não possui mãos finais.");
        }
        return maosFinais.entrySet().stream()
                .filter(entrada -> entrada.getKey().id().equals(participante.id()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "O participante não pertence à rodada."));
    }

    /**
     * Consulta todas as mãos após o encerramento.
     *
     * @return mapa imutável na ordem jogador-casa
     * @throws IllegalStateException se a rodada ainda não terminou
     */
    public Map<Jogador, List<CartaBlackjack>> maosFinais() {
        if (maosFinais.isEmpty()) {
            throw new IllegalStateException("A rodada ainda não possui mãos finais.");
        }
        return maosFinais;
    }

    /**
     * Consulta a última carta pedida por um participante nesta rodada.
     *
     * @param participante jogador ou casa da rodada
     * @return última carta comprada, ou vazio se ele conservou apenas as iniciais
     * @throws NullPointerException se o participante for nulo
     * @throws IllegalArgumentException se ele não pertencer à rodada
     */
    public Optional<CartaBlackjack> ultimaCartaCompradaPor(Jogador participante) {
        return mesa.ultimaCartaCompradaPor(participante);
    }

    @Override
    protected void aoEncerrar(
            VisaoDaPartida<CartaBlackjack> contexto, ResultadoDePartida resultado) {
        Map<Jogador, List<CartaBlackjack>> snapshot = new LinkedHashMap<>();
        for (Jogador participante : contexto.jogadores()) {
            snapshot.put(participante, List.copyOf(contexto.maoDe(participante)));
        }
        maosFinais = Collections.unmodifiableMap(snapshot);
    }

    private List<Jogada> acoesPermitidas(
            ContextoDePartida<CartaBlackjack> contexto, Jogador participante) {
        if (mesa.parou(participante)) {
            throw new IllegalStateException("Um participante parado não pode receber outro turno.");
        }
        List<Jogada> acoes = new ArrayList<>(2);
        PontuacaoDaMaoBlackjack pontuacao =
                PontuacaoDaMaoBlackjack.calcular(contexto.maoDe(participante));
        if (pontuacao.total() < 21 && contexto.quantidadeNoBaralho() > 0) {
            acoes.add(AcaoBlackjack.PEDIR);
        }
        acoes.add(AcaoBlackjack.PARAR);
        return List.copyOf(acoes);
    }

    private ContextoDecisaoBlackjack criarContextoDeDecisao(
            ContextoDePartida<CartaBlackjack> contexto,
            Jogador participante,
            List<Jogada> acoesPermitidas) {
        Jogador oponente = mesa.oponenteDe(participante);
        List<CartaBlackjack> mao = contexto.maoDe(participante);
        List<CartaBlackjack> maoDoOponente = contexto.maoDe(oponente);
        boolean ocultarCasa = mesa.papelDe(participante) == PapelBlackjack.JOGADOR
                && !mesa.maoDaCasaRevelada();
        List<CartaBlackjack> cartasVisiveis = ocultarCasa && !maoDoOponente.isEmpty()
                ? List.of(maoDoOponente.getFirst())
                : maoDoOponente;
        int cartasOcultas = maoDoOponente.size() - cartasVisiveis.size();
        return new ContextoDecisaoBlackjack(
                EtapaBlackjack.DECISAO,
                acoesPermitidas,
                participante,
                mesa.papelDe(participante),
                contexto.numeroDoTurno(),
                mao,
                PontuacaoDaMaoBlackjack.calcular(mao),
                oponente,
                cartasVisiveis,
                cartasOcultas,
                mesa.ultimaCartaCompradaPor(participante),
                contexto.quantidadeNoBaralho());
    }

    private ResultadoDoTurno aplicarPedido(
            ContextoDePartida<CartaBlackjack> contexto, Jogador participante) {
        CartaBlackjack carta = contexto.comprarDoBaralho();
        contexto.adicionarNaMao(participante, carta);
        mesa.registrarCompra(participante, carta);
        List<CartaBlackjack> maoAtual = contexto.maoDe(participante);
        publicarEvento(new CartaPedidaBlackjack(
                contexto.numeroDoTurno(),
                participante,
                mesa.papelDe(participante),
                carta,
                maoAtual,
                PontuacaoDaMaoBlackjack.calcular(maoAtual)));
        return ResultadoDoTurno.repetir();
    }

    private ResultadoDoTurno aplicarParada(
            ContextoDePartida<CartaBlackjack> contexto, Jogador participante) {
        mesa.registrarParada(participante);
        publicarEvento(new ParticipanteParouBlackjack(
                contexto.numeroDoTurno(),
                participante,
                mesa.papelDe(participante),
                PontuacaoDaMaoBlackjack.calcular(contexto.maoDe(participante))));
        return ResultadoDoTurno.avancar();
    }

    private void revelarCasaSeNecessario(
            ContextoDePartida<CartaBlackjack> contexto, Jogador participante) {
        if (mesa.papelDe(participante) != PapelBlackjack.CASA
                || !mesa.revelarMaoDaCasa()) {
            return;
        }
        List<CartaBlackjack> maoDoJogador = contexto.maoDe(mesa.jogador());
        List<CartaBlackjack> maoDaCasa = contexto.maoDe(mesa.casa());
        publicarEvento(new MaoDaCasaReveladaBlackjack(
                contexto.numeroDoTurno(),
                mesa.jogador(),
                maoDoJogador,
                PontuacaoDaMaoBlackjack.calcular(maoDoJogador),
                mesa.casa(),
                maoDaCasa,
                PontuacaoDaMaoBlackjack.calcular(maoDaCasa)));
    }
}
