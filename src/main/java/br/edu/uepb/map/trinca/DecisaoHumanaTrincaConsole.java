package br.edu.uepb.map.trinca;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.edu.uepb.map.cardgame.api.ContextoDeDecisao;
import br.edu.uepb.map.cardgame.api.EntradaSaida;
import br.edu.uepb.map.cardgame.api.EstrategiaDeDecisao;
import br.edu.uepb.map.cardgame.api.Jogada;

/** Decisão humana que apresenta as informações públicas do turno da Trinca. */
public final class DecisaoHumanaTrincaConsole implements EstrategiaDeDecisao {

    private static final String LIMPAR_TERMINAL = "\u001B[2J\u001B[H";
    private static final String SEPARADOR = "═".repeat(58);

    private final EntradaSaida entradaSaida;
    private final CorTerminal cor;
    private OrdenacaoDaMao ordenacao;

    /** Cria uma decisão sem cores, ordenada por valor crescente. */
    public DecisaoHumanaTrincaConsole(EntradaSaida entradaSaida) {
        this(entradaSaida, CorTerminal.SEM_COR, OrdenacaoDaMao.POR_VALOR);
    }

    DecisaoHumanaTrincaConsole(EntradaSaida entradaSaida, CorTerminal cor) {
        this(entradaSaida, cor, OrdenacaoDaMao.POR_VALOR);
    }

    DecisaoHumanaTrincaConsole(
            EntradaSaida entradaSaida, CorTerminal cor, OrdenacaoDaMao ordenacao) {
        this.entradaSaida = Objects.requireNonNull(
                entradaSaida, "A entrada e saída não pode ser nula.");
        this.cor = Objects.requireNonNull(cor, "A cor não pode ser nula.");
        this.ordenacao = Objects.requireNonNull(
                ordenacao, "A ordenação da mão não pode ser nula.");
    }

    @Override
    public Jogada decidir(ContextoDeDecisao contextoBase) {
        Objects.requireNonNull(contextoBase, "O contexto não pode ser nulo.");
        if (!(contextoBase instanceof ContextoDecisaoTrinca contexto)) {
            throw new IllegalArgumentException("A decisão exige um contexto da Trinca.");
        }

        if (contexto.jogadasPermitidas().isEmpty()) {
            throw new IllegalStateException("Não há jogadas permitidas.");
        }

        if (contexto.etapa() == EtapaTrinca.COMPRA) {
            prepararTerminalPara(contexto);
        }

        while (true) {
            List<Jogada> jogadas = ordenarJogadas(
                    contexto.etapa(), contexto.jogadasPermitidas());
            MaoOrganizada maoOrganizada = organizarMao(
                    contexto.mao(), contexto.cartaComprada());
            entradaSaida.exibir(formatarMao(maoOrganizada));
            if (contexto.etapa() == EtapaTrinca.COMPRA) {
                entradaSaida.exibir("Topo do descarte: "
                        + contexto.topoDoDescarte()
                        .map(DecisaoHumanaTrincaConsole::formatarCarta)
                        .orElse("vazio"));
            }

            List<String> opcoes = jogadas.stream()
                    .map(jogada -> formatarOpcao(
                            jogada,
                            maoOrganizada.idsAgrupados(),
                            maoOrganizada.idsDeGruposNovos()))
                    .collect(Collectors.toCollection(ArrayList::new));
            opcoes.add(ordenacao.opcaoDeAlternancia());
            int indice = entradaSaida.solicitarOpcao(
                    contexto.etapa() == EtapaTrinca.COMPRA
                            ? "Escolha de onde comprar:"
                            : "Escolha uma carta para descartar:",
                    List.copyOf(opcoes));
            validarIndice(indice, opcoes.size());

            if (indice == jogadas.size()) {
                ordenacao = ordenacao.alternar();
                entradaSaida.exibir(cor.aplicar(
                        "Visualização alterada para: " + ordenacao.descricao() + "."));
                continue;
            }

            Jogada escolhida = jogadas.get(indice);
            if (escolhida instanceof Descartar descarte
                    && maoOrganizada.idsAgrupados().contains(descarte.cartaId())
                    && !confirmarDescarteDaCombinacao(
                            descarte.carta(),
                            maoOrganizada.idsDeGruposNovos()
                                    .contains(descarte.cartaId()))) {
                entradaSaida.exibir(cor.aplicar("Descarte cancelado; escolha outra carta."));
                continue;
            }
            return escolhida;
        }
    }

    CorTerminal cor() {
        return cor;
    }

    OrdenacaoDaMao ordenacao() {
        return ordenacao;
    }

    String destacar(String texto) {
        return cor.aplicarComDestaque(texto);
    }

    private void prepararTerminalPara(ContextoDecisaoTrinca contexto) {
        limparTerminal();
        entradaSaida.solicitarOpcao(
                "Passe o terminal para " + cor.aplicarComDestaque(
                        contexto.jogador().nome()) + ".",
                List.of("Iniciar meu turno"));
        limparTerminal();
        entradaSaida.exibir(cor.aplicarComDestaque(
                SEPARADOR
                        + "\n  TURNO " + contexto.numeroDoTurno()
                        + " — " + contexto.jogador().nome()
                        + "\n" + SEPARADOR));
    }

    private boolean confirmarDescarteDaCombinacao(
            CartaTrinca carta, boolean combinacaoNova) {
        CorTerminal corDoAlerta = combinacaoNova ? CorTerminal.VERMELHO : cor;
        String tipoDeCombinacao = combinacaoNova
                ? "uma combinação recém-formada"
                : "uma combinação pronta";
        int confirmacao = entradaSaida.solicitarOpcao(
                corDoAlerta.aplicarComDestaque(
                        "Atenção: " + formatarCarta(carta)
                                + " faz parte de " + tipoDeCombinacao + "."
                                + " Deseja desfazê-la?"),
                List.of(
                        "Não, escolher outra carta",
                        "Sim, descartar mesmo assim"));
        validarIndice(confirmacao, 2);
        return confirmacao == 1;
    }

    private List<Jogada> ordenarJogadas(EtapaTrinca etapa, List<Jogada> jogadas) {
        List<Jogada> copia = new ArrayList<>(jogadas);
        if (etapa == EtapaTrinca.DESCARTE
                && copia.stream().allMatch(Descartar.class::isInstance)) {
            copia.sort((primeira, segunda) -> ordenacao.comparador().compare(
                    ((Descartar) primeira).carta(), ((Descartar) segunda).carta()));
        }
        return List.copyOf(copia);
    }

    private MaoOrganizada organizarMao(
            List<CartaTrinca> mao, Optional<CartaTrinca> cartaComprada) {
        List<List<CartaTrinca>> grupos = CombinacoesTrinca.agruparCombinacoes(
                        mao, cartaComprada.map(CartaTrinca::id))
                .stream()
                .map(grupo -> grupo.stream().sorted(ordenacao.comparador()).toList())
                .collect(Collectors.toCollection(ArrayList::new));
        grupos.sort((primeiro, segundo) -> ordenacao.comparador().compare(
                primeiro.getFirst(), segundo.getFirst()));

        Set<UUID> idsDeGruposNovos = new HashSet<>();
        cartaComprada.ifPresent(comprada -> grupos.stream()
                .filter(grupo -> grupo.stream()
                        .anyMatch(carta -> carta.id().equals(comprada.id())))
                .forEach(grupo -> grupo.forEach(carta ->
                        idsDeGruposNovos.add(carta.id()))));

        List<List<CartaTrinca>> gruposNovos = grupos.stream()
                .filter(grupo -> grupo.stream()
                        .anyMatch(carta -> idsDeGruposNovos.contains(carta.id())))
                .toList();
        List<List<CartaTrinca>> gruposAnteriores = grupos.stream()
                .filter(grupo -> grupo.stream()
                        .noneMatch(carta -> idsDeGruposNovos.contains(carta.id())))
                .toList();

        Set<UUID> idsAgrupados = new HashSet<>();
        grupos.forEach(grupo -> grupo.forEach(carta -> idsAgrupados.add(carta.id())));
        List<CartaTrinca> cartasLivres = mao.stream()
                .filter(carta -> !idsAgrupados.contains(carta.id()))
                .sorted(ordenacao.comparador())
                .toList();
        return new MaoOrganizada(
                gruposAnteriores,
                gruposNovos,
                cartasLivres,
                Set.copyOf(idsAgrupados),
                Set.copyOf(idsDeGruposNovos));
    }

    private String formatarMao(MaoOrganizada mao) {
        StringBuilder texto = new StringBuilder(cor.aplicarComDestaque(
                "Sua mão — " + ordenacao.descricao() + ":"));
        for (List<CartaTrinca> grupo : mao.gruposNovos()) {
            texto.append('\n').append(CorTerminal.VERMELHO.aplicarComDestaque(
                    "  ★ " + nomeDaNovaCombinacao(grupo)
                            + " FORMADA APÓS A COMPRA:"));
            texto.append("\n    ").append(CorTerminal.VERMELHO.aplicar(
                    grupo.stream()
                            .map(DecisaoHumanaTrincaConsole::formatarCarta)
                            .collect(Collectors.joining(" "))));
        }
        if (!mao.gruposAnteriores().isEmpty()) {
            texto.append('\n').append(cor.aplicarComDestaque("  ★ Combinações prontas:"));
            for (List<CartaTrinca> grupo : mao.gruposAnteriores()) {
                texto.append("\n    ").append(cor.aplicar(
                        grupo.stream()
                                .map(DecisaoHumanaTrincaConsole::formatarCarta)
                                .collect(Collectors.joining(" "))));
            }
        }
        if (!mao.cartasLivres().isEmpty()) {
            texto.append('\n').append("  Cartas livres:");
            anexarCartasOrdenadas(texto, mao.cartasLivres());
        } else if (mao.gruposAnteriores().isEmpty() && mao.gruposNovos().isEmpty()) {
            texto.append("\n    vazia");
        } else {
            texto.append("\n  Nenhuma carta livre.");
        }
        return texto.toString();
    }

    private void anexarCartasOrdenadas(StringBuilder texto, List<CartaTrinca> cartas) {
        if (ordenacao == OrdenacaoDaMao.POR_VALOR) {
            texto.append("\n    ").append(cartas.stream()
                    .map(DecisaoHumanaTrincaConsole::formatarCarta)
                    .collect(Collectors.joining(" ")));
            return;
        }
        for (Naipe naipe : Naipe.values()) {
            List<CartaTrinca> cartasDoNaipe = cartas.stream()
                    .filter(carta -> carta.naipe() == naipe)
                    .toList();
            if (!cartasDoNaipe.isEmpty()) {
                texto.append("\n    ")
                        .append(naipe.simbolo()).append(' ').append(naipe.descricao())
                        .append(": ")
                        .append(cartasDoNaipe.stream()
                                .map(DecisaoHumanaTrincaConsole::formatarCarta)
                                .collect(Collectors.joining(" ")));
            }
        }
    }

    private String formatarOpcao(
            Jogada jogada,
            Set<UUID> idsAgrupados,
            Set<UUID> idsDeGruposNovos) {
        if (!(jogada instanceof Descartar descarte)) {
            return jogada.toString();
        }
        String opcao = "Descartar " + formatarCarta(descarte.carta());
        if (idsDeGruposNovos.contains(descarte.cartaId())) {
            return CorTerminal.VERMELHO.aplicar(
                    opcao + "  ★ combinação recém-formada");
        }
        if (idsAgrupados.contains(descarte.cartaId())) {
            return cor.aplicar(opcao + "  ★ combinação pronta");
        }
        return opcao;
    }

    private static String nomeDaNovaCombinacao(List<CartaTrinca> grupo) {
        Valor primeiroValor = grupo.getFirst().valor();
        boolean ehTrinca = grupo.size() == 3
                && grupo.stream().allMatch(carta -> carta.valor() == primeiroValor);
        return ehTrinca ? "NOVA TRINCA" : "NOVA SEQUÊNCIA";
    }

    private static String formatarCarta(CartaTrinca carta) {
        return "[" + carta.valor().simbolo() + carta.naipe().simbolo() + "]";
    }

    private static void validarIndice(int indice, int quantidadeDeOpcoes) {
        if (indice < 0 || indice >= quantidadeDeOpcoes) {
            throw new IllegalStateException(
                    "A entrada e saída retornou uma opção inválida.");
        }
    }

    private void limparTerminal() {
        entradaSaida.exibir(LIMPAR_TERMINAL);
    }

    private record MaoOrganizada(
            List<List<CartaTrinca>> gruposAnteriores,
            List<List<CartaTrinca>> gruposNovos,
            List<CartaTrinca> cartasLivres,
            Set<UUID> idsAgrupados,
            Set<UUID> idsDeGruposNovos) {
    }
}
