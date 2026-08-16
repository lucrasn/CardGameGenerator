package br.edu.uepb.map.trinca;

import java.util.ArrayList;
import java.util.List;

import br.edu.uepb.map.cardgame.api.ContextoDePartida;
import br.edu.uepb.map.cardgame.api.Jogada;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.PartidaConfig;
import br.edu.uepb.map.cardgame.api.ResultadoDoTurno;
import br.edu.uepb.map.cardgame.api.excecao.JogadaInvalidaException;
import br.edu.uepb.map.cardgame.engine.MotorDePartida;

/** Motor concreto que implementa compra, reciclagem e descarte da Trinca. */
public final class MotorTrinca extends MotorDePartida<CartaTrinca> {

    private static final int LIMITE_DE_ESCOLHAS_DE_DESCARTE = 100;
    private final MesaTrinca mesa;

    private MotorTrinca(PartidaConfig<CartaTrinca> configuracao, MesaTrinca mesa) {
        super(configuracao);
        this.mesa = mesa;
    }

    /** Cria um motor com todas as regras oficiais da variante de nove cartas. */
    public static MotorTrinca criar(List<Jogador> jogadores) {
        MesaTrinca mesa = new MesaTrinca();
        PartidaConfig<CartaTrinca> configuracao = PartidaConfig.<CartaTrinca>builder()
                .jogadores(jogadores)
                .baralhoFactory(new BaralhoTrincaFactory())
                .distribuicao(new br.edu.uepb.map.cardgame.api.DistribuicaoAlternada<>(9))
                .regraDeValidacao(new RegraValidacaoTrinca(mesa))
                .regraDeVitoria(new RegraVitoriaTrinca(mesa))
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
                EtapaTrinca.COMPRA, compras, jogador, contexto.maoDe(jogador),
                mesa.topoDoDescarte()));
        if (!compras.contains(compra)) {
            throw new JogadaInvalidaException("A compra escolhida não está entre as opções permitidas.");
        }
        validarJogada(contexto, compra);
        aplicarCompra(contexto, jogador, compra);

        aplicarDescarteEscolhido(contexto, jogador);
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

    private void aplicarCompra(
            ContextoDePartida<CartaTrinca> contexto, Jogador jogador, Jogada compra) {
        CartaTrinca carta;
        if (compra == ComprarDoMonte.INSTANCIA) {
            if (contexto.quantidadeNoBaralho() == 0) {
                List<CartaTrinca> recicladas = mesa.retirarParaReciclagem();
                contexto.adicionarAoBaralho(recicladas);
                contexto.embaralharBaralho();
            }
            carta = contexto.comprarDoBaralho();
        } else {
            carta = mesa.comprarDoDescarte();
        }
        contexto.adicionarNaMao(jogador, carta);
    }

    private void aplicarDescarteEscolhido(
            ContextoDePartida<CartaTrinca> contexto, Jogador jogador) {
        for (int tentativa = 0; tentativa < LIMITE_DE_ESCOLHAS_DE_DESCARTE; tentativa++) {
            List<Jogada> descartes = contexto.maoDe(jogador).stream()
                    .map(Descartar::new)
                    .map(Jogada.class::cast)
                    .toList();
            Jogada escolha = jogador.estrategiaDeDecisao().decidir(new ContextoDecisaoTrinca(
                    EtapaTrinca.DESCARTE, descartes, jogador, contexto.maoDe(jogador),
                    mesa.topoDoDescarte()));
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
