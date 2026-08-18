package br.edu.uepb.map.blackjack;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Resultado imutável do cálculo de uma mão de Blackjack.
 *
 * @param total melhor total possível sem tratar um estouro como pontuação válida
 * @param suave indica que ao menos um Ás continua valendo onze
 * @param blackjackNatural indica 21 pontos obtidos com exatamente duas cartas
 */
public record PontuacaoDaMaoBlackjack(
        int total, boolean suave, boolean blackjackNatural) {

    /**
     * Valida a coerência básica do valor calculado.
     *
     * @throws IllegalArgumentException se o total for negativo ou as classificações
     *         forem incompatíveis com ele
     */
    public PontuacaoDaMaoBlackjack {
        if (total < 0) {
            throw new IllegalArgumentException("O total da mão não pode ser negativo.");
        }
        if (suave && total > 21) {
            throw new IllegalArgumentException("Uma mão estourada não pode ser suave.");
        }
        if (blackjackNatural && total != 21) {
            throw new IllegalArgumentException("Um Blackjack natural deve totalizar 21.");
        }
    }

    /**
     * Calcula o melhor valor de uma coleção de cartas.
     *
     * <p>Todos os ases começam valendo onze. Enquanto o total ultrapassar 21, cada
     * Ás é rebaixado para um, reduzindo dez pontos. O procedimento também trata
     * corretamente mãos com vários ases.
     *
     * @param cartas cartas da mão, sem elementos nulos
     * @return pontuação calculada
     * @throws NullPointerException se a coleção ou alguma carta for nula
     */
    public static PontuacaoDaMaoBlackjack calcular(
            Collection<? extends CartaBlackjack> cartas) {
        List<CartaBlackjack> mao = List.copyOf(Objects.requireNonNull(
                cartas, "A mão não pode ser nula."));
        int total = 0;
        int asesComoOnze = 0;
        for (CartaBlackjack carta : mao) {
            total = Math.addExact(total, carta.valor().pontosMaximos());
            if (carta.valor().ehAs()) {
                asesComoOnze++;
            }
        }
        while (total > 21 && asesComoOnze > 0) {
            total -= 10;
            asesComoOnze--;
        }
        return new PontuacaoDaMaoBlackjack(
                total, asesComoOnze > 0, mao.size() == 2 && total == 21);
    }

    /**
     * Indica se a mão ultrapassou o limite.
     *
     * @return {@code true} quando o total é maior que 21
     */
    public boolean estourou() {
        return total > 21;
    }

    /**
     * Indica se a mão totaliza exatamente 21.
     *
     * @return {@code true} quando o total é 21
     */
    public boolean atingiuVinteEUm() {
        return total == 21;
    }
}
