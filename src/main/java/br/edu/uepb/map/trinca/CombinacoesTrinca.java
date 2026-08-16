package br.edu.uepb.map.trinca;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Reconhece partições completas da mão em trincas e sequências. */
public final class CombinacoesTrinca {

    private CombinacoesTrinca() {
    }

    public static boolean ehMaoVencedora(List<CartaTrinca> mao) {
        return mao != null && mao.size() == 9 && podeParticionar(List.copyOf(mao));
    }

    private static boolean podeParticionar(List<CartaTrinca> restantes) {
        if (restantes.isEmpty()) {
            return true;
        }

        int quantidade = restantes.size();
        // Todo subconjunto considerado contém a primeira carta. Isso elimina
        // permutações equivalentes sem perder nenhuma partição possível.
        int combinacoes = 1 << (quantidade - 1);
        for (int mascara = 0; mascara < combinacoes; mascara++) {
            List<CartaTrinca> grupo = new ArrayList<>();
            grupo.add(restantes.getFirst());
            for (int indice = 1; indice < quantidade; indice++) {
                if ((mascara & (1 << (indice - 1))) != 0) {
                    grupo.add(restantes.get(indice));
                }
            }
            if (!ehCombinacaoValida(grupo)) {
                continue;
            }

            Set<java.util.UUID> ids = new HashSet<>();
            grupo.forEach(carta -> ids.add(carta.id()));
            List<CartaTrinca> sobra = restantes.stream()
                    .filter(carta -> !ids.contains(carta.id()))
                    .toList();
            if (podeParticionar(sobra)) {
                return true;
            }
        }
        return false;
    }

    static boolean ehCombinacaoValida(List<CartaTrinca> cartas) {
        return ehTrinca(cartas) || ehSequencia(cartas);
    }

    private static boolean ehTrinca(List<CartaTrinca> cartas) {
        if (cartas.size() != 3) {
            return false;
        }
        Valor valor = cartas.getFirst().valor();
        Set<Naipe> naipes = new HashSet<>();
        return cartas.stream().allMatch(carta -> carta.valor() == valor)
                && cartas.stream().allMatch(carta -> naipes.add(carta.naipe()));
    }

    private static boolean ehSequencia(List<CartaTrinca> cartas) {
        if (cartas.size() < 3) {
            return false;
        }
        Naipe naipe = cartas.getFirst().naipe();
        if (cartas.stream().anyMatch(carta -> carta.naipe() != naipe)) {
            return false;
        }

        Set<Integer> ordens = new HashSet<>();
        for (CartaTrinca carta : cartas) {
            if (!ordens.add(carta.valor().ordem())) {
                return false;
            }
        }
        if (saoConsecutivas(ordens)) {
            return true;
        }
        if (ordens.remove(Valor.AS.ordem())) {
            ordens.add(14);
            return saoConsecutivas(ordens);
        }
        return false;
    }

    private static boolean saoConsecutivas(Set<Integer> ordens) {
        int menor = ordens.stream().mapToInt(Integer::intValue).min().orElseThrow();
        int maior = ordens.stream().mapToInt(Integer::intValue).max().orElseThrow();
        return maior - menor + 1 == ordens.size();
    }
}
