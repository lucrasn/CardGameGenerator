package br.edu.uepb.map.trinca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.EntradaSaida;
import br.edu.uepb.map.cardgame.api.Jogada;
import br.edu.uepb.map.cardgame.api.JogadorPadrao;

class DecisaoHumanaTrincaConsoleTest {

    @Test
    void deveSepararTurnoMostrarMaoETirarTopoDaOpcao() {
        EntradaSaidaFalsa io = new EntradaSaidaFalsa(0, 1);
        DecisaoHumanaTrincaConsole decisao = new DecisaoHumanaTrincaConsole(io);
        JogadorPadrao jogador = new JogadorPadrao("Ana", decisao);
        CartaTrinca sete = new CartaTrinca(Valor.SETE, Naipe.COPAS);
        CartaTrinca rei = new CartaTrinca(Valor.REI, Naipe.ESPADAS);
        Jogada monte = ComprarDoMonte.INSTANCIA;
        Jogada descarte = new ComprarDoDescarte(rei);
        var contexto = new ContextoDecisaoTrinca(
                EtapaTrinca.COMPRA,
                List.of(monte, descarte),
                jogador,
                4,
                List.of(sete),
                Optional.of(rei));

        Jogada escolhida = decisao.decidir(contexto);

        assertSame(descarte, escolhida);
        assertEquals(TelaTerminal.APAGAR_TELA_E_HISTORICO, io.mensagens.get(0));
        assertEquals(TelaTerminal.APAGAR_TELA_E_HISTORICO, io.mensagens.get(1));
        assertTrue(io.mensagens.get(0).contains("\u001B[3J"));
        assertTrue(io.mensagens.get(2).contains("TURNO 4 — Ana"));
        assertTrue(io.mensagens.get(3).contains("[7♥]"));
        assertEquals("Topo do descarte: [K♠]\n", io.mensagens.get(4));
        assertEquals("Passe o terminal para Ana.", io.solicitacoes.get(0).mensagem());
        assertEquals("Escolha de onde comprar:", io.solicitacoes.get(1).mensagem());
        assertEquals(List.of(
                "Comprar do monte",
                "Comprar do descarte",
                "Mudar visualização: Agrupar por naipe"),
                io.solicitacoes.get(1).opcoes());
    }

    @Test
    void deveAlternarOrdenacaoDuranteOJogoEPersistirPreferencia() {
        EntradaSaidaFalsa io = new EntradaSaidaFalsa(3, 0, 0);
        DecisaoHumanaTrincaConsole decisao = new DecisaoHumanaTrincaConsole(io);
        JogadorPadrao jogador = new JogadorPadrao("Bia", decisao);
        CartaTrinca asDeEspadas = new CartaTrinca(Valor.AS, Naipe.ESPADAS);
        CartaTrinca seteDeCopas = new CartaTrinca(Valor.SETE, Naipe.COPAS);
        CartaTrinca doisDeCopas = new CartaTrinca(Valor.DOIS, Naipe.COPAS);
        var contexto = new ContextoDecisaoTrinca(
                EtapaTrinca.DESCARTE,
                List.of(
                        new Descartar(seteDeCopas),
                        new Descartar(asDeEspadas),
                        new Descartar(doisDeCopas)),
                jogador,
                1,
                List.of(asDeEspadas, seteDeCopas, doisDeCopas),
                Optional.empty());

        decisao.decidir(contexto);
        int inicioDaSegundaDecisao = io.mensagens.size();
        decisao.decidir(contexto);

        assertEquals(OrdenacaoDaMao.POR_NAIPE, decisao.ordenacao());
        assertEquals("Mudar visualização: Agrupar por naipe",
                io.solicitacoes.get(0).opcoes().getLast());
        assertEquals("Mudar visualização: Ordenar por valor crescente (A até K)",
                io.solicitacoes.get(1).opcoes().getLast());
        assertTrue(io.solicitacoes.get(0).opcoes().get(0).contains("[A♠]"));
        assertTrue(io.solicitacoes.get(0).opcoes().get(1).contains("[2♥]"));
        assertTrue(io.solicitacoes.get(1).opcoes().get(0).contains("[2♥]"));
        assertTrue(io.solicitacoes.get(1).opcoes().get(2).contains("[A♠]"));
        String maoAposAlternar = io.mensagens.stream()
                .filter(mensagem -> mensagem.contains("♥ Copas: [2♥] [7♥]"))
                .findFirst()
                .orElseThrow();
        assertTrue(maoAposAlternar.contains("♠ Espadas: [A♠]"));
        assertTrue(maoAposAlternar.indexOf("Copas") < maoAposAlternar.indexOf("Espadas"));
        assertTrue(io.mensagens.subList(inicioDaSegundaDecisao, io.mensagens.size())
                .stream()
                .anyMatch(mensagem -> mensagem.contains("♥ Copas: [2♥] [7♥]")));
    }

    @Test
    void deveDestacarCartaCompradaMesmoSemFormarCombinacao() {
        EntradaSaidaFalsa io = new EntradaSaidaFalsa(0);
        DecisaoHumanaTrincaConsole decisao = new DecisaoHumanaTrincaConsole(
                io, CorTerminal.AZUL_CELESTE, OrdenacaoDaMao.POR_VALOR);
        JogadorPadrao jogador = new JogadorPadrao("Bia", decisao);
        CartaTrinca doisDeCopas = new CartaTrinca(Valor.DOIS, Naipe.COPAS);
        CartaTrinca reiComprado = new CartaTrinca(Valor.REI, Naipe.ESPADAS);
        var contexto = new ContextoDecisaoTrinca(
                EtapaTrinca.DESCARTE,
                List.of(new Descartar(reiComprado), new Descartar(doisDeCopas)),
                jogador,
                2,
                List.of(doisDeCopas, reiComprado),
                Optional.empty(),
                Optional.of(reiComprado));

        decisao.decidir(contexto);

        assertTrue(io.mensagens.getFirst().contains(
                "➜ CARTA COMPRADA NESTA JOGADA: [K♠]"));
        assertTrue(io.mensagens.getFirst().contains("\u001B[38;5;39m"));
        assertTrue(io.solicitacoes.getFirst().opcoes().get(1)
                .contains("← comprada nesta jogada"));
    }

    @Test
    void devePedirConfirmacaoEPermitirEscolherOutraCarta() {
        EntradaSaidaFalsa io = new EntradaSaidaFalsa(0, 0, 3);
        DecisaoHumanaTrincaConsole decisao = new DecisaoHumanaTrincaConsole(
                io, CorTerminal.MAGENTA, OrdenacaoDaMao.POR_VALOR);
        JogadorPadrao jogador = new JogadorPadrao("Caio", decisao);
        CartaTrinca seteDeCopas = new CartaTrinca(Valor.SETE, Naipe.COPAS);
        CartaTrinca seteDeOuros = new CartaTrinca(Valor.SETE, Naipe.OUROS);
        CartaTrinca seteDePaus = new CartaTrinca(Valor.SETE, Naipe.PAUS);
        CartaTrinca rei = new CartaTrinca(Valor.REI, Naipe.ESPADAS);
        Descartar descarteDoRei = new Descartar(rei);
        List<Jogada> descartesForaDeOrdem = List.of(
                descarteDoRei,
                new Descartar(seteDePaus),
                new Descartar(seteDeCopas),
                new Descartar(seteDeOuros));
        var contexto = new ContextoDecisaoTrinca(
                EtapaTrinca.DESCARTE,
                descartesForaDeOrdem,
                jogador,
                2,
                List.of(rei, seteDePaus, seteDeCopas, seteDeOuros),
                Optional.empty());

        Jogada escolhida = decisao.decidir(contexto);

        assertSame(descarteDoRei, escolhida);
        assertTrue(io.mensagens.getFirst().contains("★ Combinações prontas"));
        assertTrue(io.mensagens.getFirst().contains("\u001B[38;5;201m"));
        assertFalse(io.mensagens.getFirst().contains("\u001B[38;5;196m"));
        assertTrue(io.solicitacoes.get(0).opcoes().getFirst().contains("combinação pronta"));
        assertTrue(io.solicitacoes.get(1).mensagem().contains("Deseja desfazê-la?"));
        assertEquals(List.of(
                "Não, escolher outra carta",
                "Sim, descartar mesmo assim"), io.solicitacoes.get(1).opcoes());
        assertTrue(io.mensagens.stream()
                .anyMatch(mensagem -> mensagem.contains("Descarte cancelado")));
        assertTrue(io.solicitacoes.get(2).opcoes().get(3).contains("[K♠]"));
    }

    @Test
    void deveDestacarNovaTrincaConfirmarDescarteEVoltarParaCorNormal() {
        EntradaSaidaFalsa io = new EntradaSaidaFalsa(3, 0, 6, 0, 1);
        DecisaoHumanaTrincaConsole decisao = new DecisaoHumanaTrincaConsole(
                io, CorTerminal.VERDE_LIMA, OrdenacaoDaMao.POR_VALOR);
        JogadorPadrao jogador = new JogadorPadrao("Duda", decisao);
        CartaTrinca quatroDeEspadas = new CartaTrinca(Valor.QUATRO, Naipe.ESPADAS);
        CartaTrinca cincoDeEspadas = new CartaTrinca(Valor.CINCO, Naipe.ESPADAS);
        CartaTrinca seisDeEspadas = new CartaTrinca(Valor.SEIS, Naipe.ESPADAS);
        CartaTrinca seteDeCopas = new CartaTrinca(Valor.SETE, Naipe.COPAS);
        CartaTrinca seteDeOuros = new CartaTrinca(Valor.SETE, Naipe.OUROS);
        CartaTrinca seteComprado = new CartaTrinca(Valor.SETE, Naipe.PAUS);
        CartaTrinca reiLivre = new CartaTrinca(Valor.REI, Naipe.COPAS);
        Descartar descarteDoRei = new Descartar(reiLivre);
        List<CartaTrinca> mao = List.of(
                quatroDeEspadas, cincoDeEspadas, seisDeEspadas,
                seteDeCopas, seteDeOuros, seteComprado, reiLivre);
        var contexto = new ContextoDecisaoTrinca(
                EtapaTrinca.DESCARTE,
                List.of(
                        new Descartar(seteComprado),
                        new Descartar(seisDeEspadas),
                        new Descartar(seteDeCopas),
                        new Descartar(quatroDeEspadas),
                        new Descartar(seteDeOuros),
                        new Descartar(cincoDeEspadas),
                        descarteDoRei),
                jogador,
                3,
                mao,
                Optional.empty(),
                Optional.of(seteComprado));

        Jogada escolhida = decisao.decidir(contexto);
        int inicioDaExibicaoAposDescarte = io.mensagens.size();
        List<CartaTrinca> maoAposDescarte = List.of(
                quatroDeEspadas, cincoDeEspadas, seisDeEspadas,
                seteDeCopas, seteDeOuros, seteComprado);
        decisao.decidir(new ContextoDecisaoTrinca(
                EtapaTrinca.DESCARTE,
                maoAposDescarte.stream().map(Descartar::new).map(Jogada.class::cast).toList(),
                jogador,
                4,
                maoAposDescarte,
                Optional.empty()));

        assertSame(descarteDoRei, escolhida);
        assertTrue(io.mensagens.getFirst().contains(
                "NOVA TRINCA FORMADA APÓS A COMPRA"));
        assertTrue(io.mensagens.getFirst().contains("\u001B[38;5;196m"));
        assertTrue(io.mensagens.getFirst().contains("★ Combinações prontas"));
        assertTrue(io.mensagens.getFirst().contains("\u001B[38;5;46m"));
        assertTrue(io.solicitacoes.get(0).opcoes().get(3)
                .contains("combinação recém-formada"));
        assertTrue(io.solicitacoes.get(0).opcoes().get(3)
                .contains("\u001B[38;5;196m"));
        assertTrue(io.solicitacoes.get(1).mensagem().contains("recém-formada"));
        assertTrue(io.solicitacoes.get(1).mensagem().contains("\u001B[38;5;196m"));
        String maoExibidaAposDescarte = io.mensagens
                .subList(inicioDaExibicaoAposDescarte, io.mensagens.size())
                .getFirst();
        assertFalse(maoExibidaAposDescarte.contains("\u001B[38;5;196m"));
        assertTrue(maoExibidaAposDescarte.contains("\u001B[38;5;46m"));
        assertTrue(maoExibidaAposDescarte.contains("★ Combinações prontas"));
    }

    @Test
    void devePermitirConfirmarDescarteDeCartaAgrupada() {
        EntradaSaidaFalsa io = new EntradaSaidaFalsa(0, 1);
        DecisaoHumanaTrincaConsole decisao = new DecisaoHumanaTrincaConsole(
                io, CorTerminal.LARANJA, OrdenacaoDaMao.POR_VALOR);
        JogadorPadrao jogador = new JogadorPadrao("Duda", decisao);
        CartaTrinca seteDeCopas = new CartaTrinca(Valor.SETE, Naipe.COPAS);
        CartaTrinca seteDeOuros = new CartaTrinca(Valor.SETE, Naipe.OUROS);
        CartaTrinca seteDePaus = new CartaTrinca(Valor.SETE, Naipe.PAUS);
        Descartar primeiroDescarte = new Descartar(seteDeCopas);
        var contexto = new ContextoDecisaoTrinca(
                EtapaTrinca.DESCARTE,
                List.of(
                        primeiroDescarte,
                        new Descartar(seteDeOuros),
                        new Descartar(seteDePaus)),
                jogador,
                3,
                List.of(seteDeCopas, seteDeOuros, seteDePaus),
                Optional.empty());

        Jogada escolhida = decisao.decidir(contexto);

        assertSame(primeiroDescarte, escolhida);
        assertEquals(2, io.solicitacoes.size());
    }

    private static final class EntradaSaidaFalsa implements EntradaSaida {
        private final Deque<Integer> respostas;
        private final List<String> mensagens = new ArrayList<>();
        private final List<Solicitacao> solicitacoes = new ArrayList<>();

        private EntradaSaidaFalsa(Integer... respostas) {
            this.respostas = new ArrayDeque<>(Arrays.asList(respostas));
        }

        @Override
        public void exibir(String mensagem) {
            mensagens.add(mensagem);
        }

        @Override
        public int solicitarOpcao(String mensagem, List<String> opcoes) {
            solicitacoes.add(new Solicitacao(mensagem, List.copyOf(opcoes)));
            if (respostas.isEmpty()) {
                throw new AssertionError("O teste não forneceu resposta para: " + mensagem);
            }
            return respostas.removeFirst();
        }
    }

    private record Solicitacao(String mensagem, List<String> opcoes) {
    }
}
