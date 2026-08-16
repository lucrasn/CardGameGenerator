package br.edu.uepb.map.trinca;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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

    @Test
    void deveAgruparMaiorQuantidadeDeCartasEDeixarSoltasAsDemais() {
        CartaTrinca seteDeCopas = carta(Valor.SETE, Naipe.COPAS);
        CartaTrinca seteDeOuros = carta(Valor.SETE, Naipe.OUROS);
        CartaTrinca seteDePaus = carta(Valor.SETE, Naipe.PAUS);
        CartaTrinca quatroDeEspadas = carta(Valor.QUATRO, Naipe.ESPADAS);
        CartaTrinca cincoDeEspadas = carta(Valor.CINCO, Naipe.ESPADAS);
        CartaTrinca seisDeEspadas = carta(Valor.SEIS, Naipe.ESPADAS);
        CartaTrinca reiLivre = carta(Valor.REI, Naipe.COPAS);

        List<List<CartaTrinca>> grupos = CombinacoesTrinca.agruparCombinacoes(List.of(
                reiLivre, seteDeOuros, cincoDeEspadas, seteDeCopas,
                quatroDeEspadas, seteDePaus, seisDeEspadas));

        assertEquals(2, grupos.size());
        assertTrue(grupos.stream().allMatch(CombinacoesTrinca::ehCombinacaoValida));
        var cartasAgrupadas = new HashSet<>(grupos.stream().flatMap(List::stream).toList());
        assertEquals(6, cartasAgrupadas.size());
        assertFalse(cartasAgrupadas.contains(reiLivre));
    }

    @Test
    void devePriorizarCombinacaoQueContenhaACartaRecemComprada() {
        CartaTrinca seteComprado = carta(Valor.SETE, Naipe.COPAS);
        CartaTrinca seteDeOuros = carta(Valor.SETE, Naipe.OUROS);
        CartaTrinca seteDePaus = carta(Valor.SETE, Naipe.PAUS);
        List<CartaTrinca> maoAmbigua = List.of(
                seteComprado,
                carta(Valor.CINCO, Naipe.OUROS),
                carta(Valor.SEIS, Naipe.OUROS),
                seteDeOuros,
                carta(Valor.OITO, Naipe.OUROS),
                carta(Valor.NOVE, Naipe.OUROS),
                seteDePaus);

        List<List<CartaTrinca>> semPrioridade =
                CombinacoesTrinca.agruparCombinacoes(maoAmbigua);
        List<List<CartaTrinca>> comPrioridade = CombinacoesTrinca.agruparCombinacoes(
                maoAmbigua, Optional.of(seteComprado.id()));

        assertEquals(5, semPrioridade.stream().mapToInt(List::size).sum());
        assertTrue(comPrioridade.stream()
                .anyMatch(grupo -> grupo.stream()
                        .anyMatch(carta -> carta.id().equals(seteComprado.id()))));
        assertTrue(comPrioridade.stream().allMatch(CombinacoesTrinca::ehCombinacaoValida));
    }

    private static CartaTrinca carta(Valor valor, Naipe naipe) {
        return new CartaTrinca(valor, naipe);
    }
}
