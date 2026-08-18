package br.edu.uepb.map.trinca;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import br.edu.uepb.map.cardgame.api.ContextoDeDecisao;
import br.edu.uepb.map.cardgame.api.Jogada;
import br.edu.uepb.map.cardgame.api.Jogador;

/**
 * Visão imutável oferecida à pessoa ou bot que decide a jogada.
 *
 * @param etapa fase atual de compra ou descarte
 * @param jogadasPermitidas ações que podem ser escolhidas
 * @param jogador participante que possui a vez
 * @param numeroDoTurno número positivo do turno atual
 * @param mao snapshot da mão do participante
 * @param topoDoDescarte carta pública disponível no descarte, quando houver
 * @param cartaComprada carta incorporada à mão nesta compra, presente somente na
 *        etapa de descarte
 *
 * @author Raffael Wagner Rolim Siqueira
 * @version 0.0.1
 */
public record ContextoDecisaoTrinca(
        EtapaTrinca etapa,
        List<Jogada> jogadasPermitidas,
        Jogador jogador,
        long numeroDoTurno,
        List<CartaTrinca> mao,
        Optional<CartaTrinca> topoDoDescarte,
        Optional<CartaTrinca> cartaComprada
) implements ContextoDeDecisao {

    /**
     * Cria um contexto sem registrar uma carta comprada na etapa atual.
     *
     * @param etapa fase atual de compra ou descarte
     * @param jogadasPermitidas ações que podem ser escolhidas
     * @param jogador participante que possui a vez
     * @param numeroDoTurno número positivo do turno atual
     * @param mao snapshot da mão do participante
     * @param topoDoDescarte carta pública disponível no descarte, quando houver
     */
    public ContextoDecisaoTrinca(
            EtapaTrinca etapa,
            List<Jogada> jogadasPermitidas,
            Jogador jogador,
            long numeroDoTurno,
            List<CartaTrinca> mao,
            Optional<CartaTrinca> topoDoDescarte) {
        this(etapa, jogadasPermitidas, jogador, numeroDoTurno, mao,
                topoDoDescarte, Optional.empty());
    }

    /**
     * Valida os componentes e cria cópias imutáveis das coleções recebidas.
     *
     * @throws NullPointerException se algum componente obrigatório for nulo
     * @throws IllegalArgumentException se o turno não for positivo ou se a carta
     *         comprada for incompatível com a etapa ou com a mão
     */
    public ContextoDecisaoTrinca {
        Objects.requireNonNull(etapa, "A etapa não pode ser nula.");
        jogadasPermitidas = List.copyOf(jogadasPermitidas);
        Objects.requireNonNull(jogador, "O jogador não pode ser nulo.");
        if (numeroDoTurno < 1) {
            throw new IllegalArgumentException("O número do turno deve ser positivo.");
        }
        mao = List.copyOf(mao);
        Objects.requireNonNull(topoDoDescarte, "O topo do descarte não pode ser nulo.");
        Objects.requireNonNull(cartaComprada, "A carta comprada não pode ser nula.");
        if (cartaComprada.isPresent() && etapa != EtapaTrinca.DESCARTE) {
            throw new IllegalArgumentException(
                    "A carta comprada só pode ser informada na etapa de descarte.");
        }
        if (cartaComprada.isPresent() && mao.stream().noneMatch(carta ->
                carta.id().equals(cartaComprada.orElseThrow().id()))) {
            throw new IllegalArgumentException(
                    "A carta comprada deve pertencer à mão apresentada.");
        }
    }
}
