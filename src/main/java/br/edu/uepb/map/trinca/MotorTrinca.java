package br.edu.uepb.map.trinca;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import br.edu.uepb.map.cardgame.api.ContextoDePartida;
import br.edu.uepb.map.cardgame.api.Jogada;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.PartidaConfig;
import br.edu.uepb.map.cardgame.api.ResultadoDoTurno;
import br.edu.uepb.map.cardgame.api.excecao.JogadaInvalidaException;
import br.edu.uepb.map.cardgame.engine.MotorDePartida;

/** Motor concreto que implementa compra, reciclagem e descarte da Trinca. */
public final class MotorTrinca extends MotorDePartida<CartaTrinca> {

    static final int MINIMO_DE_JOGADORES = 2;
    static final int MAXIMO_DE_JOGADORES = 5;
    private static final int LIMITE_DE_ESCOLHAS_DE_DESCARTE = 100;
    private final MesaTrinca mesa;

    private MotorTrinca(PartidaConfig<CartaTrinca> configuracao, MesaTrinca mesa) {
        super(configuracao);
        this.mesa = mesa;
    }

    /**
     * Cria um motor com todas as regras oficiais da variante de nove cartas.
     *
     * @param jogadores participantes na ordem dos turnos, entre dois e cinco
     * @return motor configurado e ainda não executado
     * @throws NullPointerException se a lista for nula
     * @throws IllegalArgumentException se a quantidade estiver fora do intervalo
     *         suportado ou se a configuração dos jogadores for inválida
     */
    public static MotorTrinca criar(List<Jogador> jogadores) {
        Objects.requireNonNull(jogadores, "A lista de jogadores não pode ser nula.");
        if (jogadores.size() < MINIMO_DE_JOGADORES
                || jogadores.size() > MAXIMO_DE_JOGADORES) {
            throw new IllegalArgumentException(
                    "A Trinca aceita entre dois e cinco jogadores.");
        }
        MesaTrinca mesa = new MesaTrinca();
        PartidaConfig<CartaTrinca> configuracao = PartidaConfig.<CartaTrinca>builder()
                .jogadores(jogadores)
                .baralhoFactory(new BaralhoTrincaFactory())
                .distribuicao(new br.edu.uepb.map.cardgame.api.DistribuicaoAlternada<>(9))
                .regraDeValidacao(new RegraValidacaoTrinca(mesa))
                .regraDeVitoria(new RegraVitoriaTrinca())
                .regraDePontuacao(new RegraPontuacaoTrinca())
                .primeiroJogador(0)
                .build();
        return new MotorTrinca(configuracao, mesa);
    }

    @Override
    protected void aposDistribuir(ContextoDePartida<CartaTrinca> contexto) {
        mesa.descartar(contexto.comprarDoBaralho());
    }

    @Override
    protected ResultadoDoTurno executarTurno(ContextoDePartida<CartaTrinca> contexto) {
        Jogador jogador = contexto.jogadorAtual();
        List<Jogada> compras = comprasPermitidas(contexto);
        Jogada compra = jogador.estrategiaDeDecisao().decidir(new ContextoDecisaoTrinca(
                EtapaTrinca.COMPRA, compras, jogador, contexto.numeroDoTurno(),
                contexto.maoDe(jogador),
                mesa.topoDoDescarte()));
        if (!compras.contains(compra)) {
            throw new JogadaInvalidaException("A compra escolhida não está entre as opções permitidas.");
        }
        validarJogada(contexto, compra);
        CartaTrinca cartaComprada = aplicarCompra(contexto, jogador, compra);

        aplicarDescarteEscolhido(contexto, jogador, cartaComprada);
        return ResultadoDoTurno.avancar();
    }

    private List<Jogada> comprasPermitidas(ContextoDePartida<CartaTrinca> contexto) {
        List<Jogada> compras = new ArrayList<>(2);
        if (contexto.quantidadeNoBaralho() > 0 || mesa.quantidadeNoDescarte() > 1) {
            compras.add(ComprarDoMonte.INSTANCIA);
        }
        mesa.topoDoDescarte().ifPresent(carta -> compras.add(new ComprarDoDescarte(carta)));
        return List.copyOf(compras);
    }

    private CartaTrinca aplicarCompra(
            ContextoDePartida<CartaTrinca> contexto, Jogador jogador, Jogada compra) {
        CartaTrinca carta;
        if (compra == ComprarDoMonte.INSTANCIA) {
            if (contexto.quantidadeNoBaralho() == 0) {
                reciclarDescarte(contexto, mesa);
            }
            carta = contexto.comprarDoBaralho();
        } else {
            carta = mesa.comprarDoDescarte();
        }
        contexto.adicionarNaMao(jogador, carta);
        return carta;
    }

    static void reciclarDescarte(
            ContextoDePartida<CartaTrinca> contexto, MesaTrinca mesa) {
        List<CartaTrinca> recicladas = mesa.retirarTodasParaReciclagem();
        contexto.adicionarAoBaralho(recicladas);
        contexto.embaralharBaralho();
        mesa.descartar(contexto.comprarDoBaralho());
    }

    private void aplicarDescarteEscolhido(
            ContextoDePartida<CartaTrinca> contexto,
            Jogador jogador,
            CartaTrinca cartaComprada) {
        for (int tentativa = 0; tentativa < LIMITE_DE_ESCOLHAS_DE_DESCARTE; tentativa++) {
            List<Jogada> descartes = contexto.maoDe(jogador).stream()
                    .map(Descartar::new)
                    .map(Jogada.class::cast)
                    .toList();
            Jogada escolha = jogador.estrategiaDeDecisao().decidir(new ContextoDecisaoTrinca(
                    EtapaTrinca.DESCARTE, descartes, jogador, contexto.numeroDoTurno(),
                    contexto.maoDe(jogador),
                    mesa.topoDoDescarte(),
                    Optional.of(cartaComprada)));
            if (!descartes.contains(escolha)) {
                continue;
            }
            validarJogada(contexto, escolha);
            Descartar descarte = (Descartar) escolha;
            CartaTrinca carta = contexto.removerDaMao(jogador, descarte.cartaId());
            mesa.descartar(carta);
            return;
        }
        throw new IllegalStateException("O jogador não escolheu um descarte válido.");
    }
}
