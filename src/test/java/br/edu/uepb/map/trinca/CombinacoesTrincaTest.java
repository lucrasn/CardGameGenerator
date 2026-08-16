package br.edu.uepb.map.trinca;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class CombinacoesTrincaTest {

    @Test
    void deveAceitarTresTrincas() {
        assertTrue(CombinacoesTrinca.ehMaoVencedora(List.of(
                carta(Valor.SETE, Naipe.COPAS), carta(Valor.SETE, Naipe.OUROS),
                carta(Valor.SETE, Naipe.PAUS), carta(Valor.DAMA, Naipe.COPAS),
                carta(Valor.DAMA, Naipe.OUROS), carta(Valor.DAMA, Naipe.ESPADAS),
                carta(Valor.DOIS, Naipe.COPAS), carta(Valor.DOIS, Naipe.PAUS),
                carta(Valor.DOIS, Naipe.ESPADAS))));
    }

    @Test
    void deveAceitarSequenciasComAsBaixoEAlto() {
        assertTrue(CombinacoesTrinca.ehMaoVencedora(List.of(
                carta(Valor.AS, Naipe.COPAS), carta(Valor.DOIS, Naipe.COPAS),
                carta(Valor.TRES, Naipe.COPAS), carta(Valor.DAMA, Naipe.OUROS),
                carta(Valor.REI, Naipe.OUROS), carta(Valor.AS, Naipe.OUROS),
                carta(Valor.NOVE, Naipe.COPAS), carta(Valor.NOVE, Naipe.PAUS),
                carta(Valor.NOVE, Naipe.ESPADAS))));
    }

    @Test
    void naoDevePermitirQueAsLigueReiADois() {
        assertFalse(CombinacoesTrinca.ehCombinacaoValida(List.of(
                carta(Valor.REI, Naipe.COPAS), carta(Valor.AS, Naipe.COPAS),
                carta(Valor.DOIS, Naipe.COPAS))));
    }

    @Test
    void naoDeveAceitarCartaSolta() {
        assertFalse(CombinacoesTrinca.ehMaoVencedora(List.of(
                carta(Valor.SETE, Naipe.COPAS), carta(Valor.SETE, Naipe.OUROS),
                carta(Valor.SETE, Naipe.PAUS), carta(Valor.QUATRO, Naipe.COPAS),
                carta(Valor.CINCO, Naipe.COPAS), carta(Valor.SEIS, Naipe.COPAS),
                carta(Valor.DEZ, Naipe.OUROS), carta(Valor.VALETE, Naipe.OUROS),
                carta(Valor.TRES, Naipe.ESPADAS))));
    }

    @Test
    void trincaDeveExigirNaipesDistintos() {
        assertFalse(CombinacoesTrinca.ehCombinacaoValida(List.of(
                carta(Valor.SETE, Naipe.COPAS), carta(Valor.SETE, Naipe.COPAS),
                carta(Valor.SETE, Naipe.OUROS))));
    }

    private static CartaTrinca carta(Valor valor, Naipe naipe) {
        return new CartaTrinca(valor, naipe);
    }
}
