package br.edu.uepb.map.trinca;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import br.edu.uepb.map.cardgame.api.ContextoDeDecisao;
import br.edu.uepb.map.cardgame.api.EntradaSaida;
import br.edu.uepb.map.cardgame.api.EstrategiaDeDecisao;
import br.edu.uepb.map.cardgame.api.Jogada;

/** Decisão humana que apresenta as informações públicas do turno da Trinca. */
public final class DecisaoHumanaTrincaConsole implements EstrategiaDeDecisao {

    private static final String LIMPAR_TERMINAL = "\033[2J\033[H";
    private final EntradaSaida entradaSaida;

    public DecisaoHumanaTrincaConsole(EntradaSaida entradaSaida) {
        this.entradaSaida = Objects.requireNonNull(
                entradaSaida, "A entrada e saída não pode ser nula.");
    }

    @Override
    public Jogada decidir(ContextoDeDecisao contextoBase) {
        Objects.requireNonNull(contextoBase, "O contexto não pode ser nulo.");
        if (!(contextoBase instanceof ContextoDecisaoTrinca contexto)) {
            throw new IllegalArgumentException("A decisão exige um contexto da Trinca.");
        }

        List<Jogada> jogadas = contexto.jogadasPermitidas();
        if (jogadas.isEmpty()) {
            throw new IllegalStateException("Não há jogadas permitidas.");
        }

        if (contexto.etapa() == EtapaTrinca.COMPRA) {
            prepararTerminalPara(contexto);
        }
        entradaSaida.exibir("Sua mão: " + formatarMao(contexto.mao()));
        entradaSaida.exibir("Topo do descarte: "
                + contexto.topoDoDescarte().map(Object::toString).orElse("vazio"));

        List<String> opcoes = jogadas.stream().map(Object::toString).toList();
        int indice = entradaSaida.solicitarOpcao(
                contexto.etapa() == EtapaTrinca.COMPRA
                        ? "Escolha de onde comprar:"
                        : "Escolha uma carta para descartar:",
                opcoes);
        if (indice < 0 || indice >= jogadas.size()) {
            throw new IllegalStateException("A entrada e saída retornou uma opção inválida.");
        }
        return jogadas.get(indice);
    }

    private void prepararTerminalPara(ContextoDecisaoTrinca contexto) {
        limparTerminal();
        entradaSaida.solicitarOpcao(
                "Passe o terminal para " + contexto.jogador().nome() + ".",
                List.of("Mostrar minha mão"));
        limparTerminal();
    }

    private void limparTerminal() {
        entradaSaida.exibir(LIMPAR_TERMINAL);
    }

    private static String formatarMao(List<CartaTrinca> mao) {
        if (mao.isEmpty()) {
            return "vazia";
        }
        return mao.stream().map(Object::toString).collect(Collectors.joining(", "));
    }
}
