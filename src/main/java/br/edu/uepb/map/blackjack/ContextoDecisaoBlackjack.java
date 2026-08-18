package br.edu.uepb.map.blackjack;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import br.edu.uepb.map.cardgame.api.ContextoDeDecisao;
import br.edu.uepb.map.cardgame.api.Jogada;
import br.edu.uepb.map.cardgame.api.Jogador;

/**
 * Visão imutável e deliberadamente limitada oferecida a uma decisão do Blackjack.
 *
 * <p>Durante a vez do jogador, somente a primeira carta da casa aparece em
 * {@code cartasVisiveisDoOponente}; a carta fechada não é entregue à Strategy. Na
 * vez da casa, a mão do jogador já pode ser observada por completo.
 *
 * @param etapa fase atual do turno
 * @param jogadasPermitidas ações legais disponíveis
 * @param participante participante que está decidindo
 * @param papel papel do participante no jogo
 * @param numeroDoTurno número lógico positivo do turno
 * @param mao mão completa do participante
 * @param pontuacao pontuação correspondente à mão
 * @param oponente outro participante da rodada
 * @param cartasVisiveisDoOponente parcela pública da mão adversária
 * @param cartasOcultasDoOponente quantidade de cartas adversárias ocultas
 * @param ultimaCartaComprada última carta comprada pelo participante, quando houver
 * @param cartasNoBaralho quantidade restante no baralho
 */
public record ContextoDecisaoBlackjack(
        EtapaBlackjack etapa,
        List<Jogada> jogadasPermitidas,
        Jogador participante,
        PapelBlackjack papel,
        long numeroDoTurno,
        List<CartaBlackjack> mao,
        PontuacaoDaMaoBlackjack pontuacao,
        Jogador oponente,
        List<CartaBlackjack> cartasVisiveisDoOponente,
        int cartasOcultasDoOponente,
        Optional<CartaBlackjack> ultimaCartaComprada,
        int cartasNoBaralho
) implements ContextoDeDecisao {

    /**
     * Valida os dados e realiza cópias defensivas das coleções.
     *
     * @throws NullPointerException se algum componente obrigatório for nulo
     * @throws IllegalArgumentException se os participantes coincidirem, os números
     *         forem inválidos, a pontuação divergir da mão ou a última carta não
     *         pertencer à mão
     */
    public ContextoDecisaoBlackjack {
        Objects.requireNonNull(etapa, "A etapa não pode ser nula.");
        jogadasPermitidas = List.copyOf(Objects.requireNonNull(
                jogadasPermitidas, "As jogadas permitidas não podem ser nulas."));
        Objects.requireNonNull(participante, "O participante não pode ser nulo.");
        Objects.requireNonNull(participante.id(), "O ID do participante não pode ser nulo.");
        Objects.requireNonNull(papel, "O papel não pode ser nulo.");
        if (numeroDoTurno < 1) {
            throw new IllegalArgumentException("O número do turno deve ser positivo.");
        }
        mao = List.copyOf(Objects.requireNonNull(mao, "A mão não pode ser nula."));
        Objects.requireNonNull(pontuacao, "A pontuação não pode ser nula.");
        if (!pontuacao.equals(PontuacaoDaMaoBlackjack.calcular(mao))) {
            throw new IllegalArgumentException("A pontuação deve corresponder à mão informada.");
        }
        Objects.requireNonNull(oponente, "O oponente não pode ser nulo.");
        Objects.requireNonNull(oponente.id(), "O ID do oponente não pode ser nulo.");
        if (participante.id().equals(oponente.id())) {
            throw new IllegalArgumentException("Participante e oponente devem ser distintos.");
        }
        cartasVisiveisDoOponente = List.copyOf(Objects.requireNonNull(
                cartasVisiveisDoOponente,
                "As cartas visíveis do oponente não podem ser nulas."));
        if (cartasOcultasDoOponente < 0) {
            throw new IllegalArgumentException(
                    "A quantidade de cartas ocultas não pode ser negativa.");
        }
        ultimaCartaComprada = Objects.requireNonNull(
                ultimaCartaComprada, "A última carta comprada não pode ser nula.");
        if (ultimaCartaComprada.isPresent()) {
            CartaBlackjack comprada = ultimaCartaComprada.orElseThrow();
            boolean pertence = mao.stream().anyMatch(carta -> carta.id().equals(comprada.id()));
            if (!pertence) {
                throw new IllegalArgumentException(
                        "A última carta comprada deve pertencer à mão apresentada.");
            }
        }
        if (cartasNoBaralho < 0) {
            throw new IllegalArgumentException(
                    "A quantidade de cartas no baralho não pode ser negativa.");
        }
    }
}
