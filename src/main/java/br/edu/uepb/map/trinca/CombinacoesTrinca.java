package br.edu.uepb.map.trinca;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Reconhece partições completas da mão em trincas e sequências. */
public final class CombinacoesTrinca {

    private CombinacoesTrinca() {
    }

    public static boolean ehMaoVencedora(List<CartaTrinca> mao) {
        return mao != null && mao.size() == 9 && podeParticionar(List.copyOf(mao));
    }

    /**
     * Encontra uma organização visual com o maior número possível de cartas agrupadas.
     *
     * <p>Os grupos devolvidos são disjuntos e cada um é uma trinca ou sequência válida.
     * Em empates, a solução com mais grupos é preferida para deixar as combinações
     * menores mais explícitas no console.
     */
    static List<List<CartaTrinca>> agruparCombinacoes(List<CartaTrinca> mao) {
        return agruparCombinacoes(mao, Optional.empty());
    }

    /**
     * Organiza a mão priorizando, quando possível, um grupo que contenha a carta
     * indicada. A prioridade resolve ambiguidades visuais após uma compra sem mudar
     * as regras usadas para reconhecer uma mão vencedora.
     */
    static List<List<CartaTrinca>> agruparCombinacoes(
            List<CartaTrinca> mao, Optional<UUID> cartaPrioritariaId) {
        List<CartaTrinca> cartas = List.copyOf(
                Objects.requireNonNull(mao, "A mão não pode ser nula."));
        Objects.requireNonNull(
                cartaPrioritariaId, "A identidade prioritária não pode ser nula.");
        if (cartas.isEmpty()) {
            return List.of();
        }
        if (cartas.size() > 10) {
            throw new IllegalArgumentException(
                    "A organização visual aceita no máximo dez cartas.");
        }

        List<Integer> combinacoesValidas = new ArrayList<>();
        int mascaraPrioritaria = 0;
        int limite = 1 << cartas.size();
        if (cartaPrioritariaId.isPresent()) {
            UUID id = cartaPrioritariaId.orElseThrow();
            for (int indice = 0; indice < cartas.size(); indice++) {
                if (cartas.get(indice).id().equals(id)) {
                    mascaraPrioritaria = 1 << indice;
                    break;
                }
            }
        }
        for (int mascara = 0; mascara < limite; mascara++) {
            if (Integer.bitCount(mascara) < 3) {
                continue;
            }
            List<CartaTrinca> grupo = cartasDaMascara(cartas, mascara);
            if (ehCombinacaoValida(grupo)) {
                combinacoesValidas.add(mascara);
            }
        }

        Agrupamento agrupamento = melhorAgrupamento(
                limite - 1, combinacoesValidas, mascaraPrioritaria, new HashMap<>());
        return agrupamento.mascaras().stream()
                .map(mascara -> List.copyOf(cartasDaMascara(cartas, mascara)))
                .toList();
    }

    private static Agrupamento melhorAgrupamento(
            int disponiveis,
            List<Integer> combinacoesValidas,
            int mascaraPrioritaria,
            Map<Integer, Agrupamento> memo) {
        if (disponiveis == 0) {
            return Agrupamento.VAZIO;
        }
        Agrupamento memorizado = memo.get(disponiveis);
        if (memorizado != null) {
            return memorizado;
        }

        int primeiraCarta = Integer.lowestOneBit(disponiveis);
        Agrupamento melhor = melhorAgrupamento(
                disponiveis ^ primeiraCarta,
                combinacoesValidas,
                mascaraPrioritaria,
                memo);
        for (int combinacao : combinacoesValidas) {
            boolean contemPrimeiraCarta = (combinacao & primeiraCarta) != 0;
            boolean estaDisponivel = (combinacao & disponiveis) == combinacao;
            if (!contemPrimeiraCarta || !estaDisponivel) {
                continue;
            }
            Agrupamento restante = melhorAgrupamento(
                    disponiveis ^ combinacao,
                    combinacoesValidas,
                    mascaraPrioritaria,
                    memo);
            Agrupamento candidato = restante.adicionar(combinacao);
            if (candidato.melhorQue(melhor, mascaraPrioritaria)) {
                melhor = candidato;
            }
        }
        memo.put(disponiveis, melhor);
        return melhor;
    }

    private static List<CartaTrinca> cartasDaMascara(
            List<CartaTrinca> cartas, int mascara) {
        List<CartaTrinca> grupo = new ArrayList<>();
        for (int indice = 0; indice < cartas.size(); indice++) {
            if ((mascara & (1 << indice)) != 0) {
                grupo.add(cartas.get(indice));
            }
        }
        return grupo;
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

    private record Agrupamento(List<Integer> mascaras, int cartasAgrupadas) {

        private static final Agrupamento VAZIO = new Agrupamento(List.of(), 0);

        private Agrupamento adicionar(int mascara) {
            List<Integer> novasMascaras = new ArrayList<>(mascaras.size() + 1);
            novasMascaras.add(mascara);
            novasMascaras.addAll(mascaras);
            return new Agrupamento(
                    List.copyOf(novasMascaras),
                    cartasAgrupadas + Integer.bitCount(mascara));
        }

        private boolean melhorQue(Agrupamento outro, int mascaraPrioritaria) {
            boolean incluiPrioritaria = inclui(mascaraPrioritaria);
            boolean outroIncluiPrioritaria = outro.inclui(mascaraPrioritaria);
            if (incluiPrioritaria != outroIncluiPrioritaria) {
                return incluiPrioritaria;
            }
            return cartasAgrupadas > outro.cartasAgrupadas
                    || (cartasAgrupadas == outro.cartasAgrupadas
                    && mascaras.size() > outro.mascaras.size());
        }

        private boolean inclui(int mascara) {
            return mascara != 0
                    && mascaras.stream().anyMatch(grupo -> (grupo & mascara) != 0);
        }
    }
}
