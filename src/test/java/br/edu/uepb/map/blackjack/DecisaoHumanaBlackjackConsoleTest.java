package br.edu.uepb.map.blackjack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.EntradaSaida;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.JogadorPadrao;

class DecisaoHumanaBlackjackConsoleTest {

    @Test
    void deveOcultarCartaDaCasaEMostrarTotalDoJogador() {
        EntradaSaidaFalsa io = new EntradaSaidaFalsa(1);
        DecisaoHumanaBlackjackConsole decisao =
                new DecisaoHumanaBlackjackConsole(io, CorTerminalBlackjack.SEM_COR);
        Jogador jogador = new JogadorPadrao("Você", decisao);
        Jogador casa = new JogadorPadrao("Casa", new EstrategiaCasaBlackjack());
        List<CartaBlackjack> mao = List.of(
                carta(ValorBlackjack.DEZ, NaipeBlackjack.PAUS),
                carta(ValorBlackjack.OITO, NaipeBlackjack.COPAS));
        var contexto = contexto(
                jogador, casa, mao, Optional.empty(),
                List.of(AcaoBlackjack.PEDIR, AcaoBlackjack.PARAR));

        var escolhida = decisao.decidir(contexto);

        assertEquals(AcaoBlackjack.PARAR, escolhida);
        String tela = String.join("\n", io.mensagens);
        assertTrue(tela.contains("[??]"));
        assertTrue(tela.contains("Total: 18"));
        assertTrue(tela.contains("Cartas restantes no monte: 47"));
        assertFalse(tela.contains("Rei oculto"));
    }

    @Test
    void deveDestacarAUltimaCartaComprada() {
        EntradaSaidaFalsa io = new EntradaSaidaFalsa(0);
        DecisaoHumanaBlackjackConsole decisao =
                new DecisaoHumanaBlackjackConsole(io);
        Jogador jogador = new JogadorPadrao("Você", decisao);
        Jogador casa = new JogadorPadrao("Casa", new EstrategiaCasaBlackjack());
        CartaBlackjack comprada = carta(ValorBlackjack.CINCO, NaipeBlackjack.OUROS);
        List<CartaBlackjack> mao = List.of(
                carta(ValorBlackjack.DEZ, NaipeBlackjack.PAUS), comprada);
        var contexto = contexto(
                jogador, casa, mao, Optional.of(comprada),
                List.of(AcaoBlackjack.PEDIR, AcaoBlackjack.PARAR));

        decisao.decidir(contexto);

        String tela = String.join("\n", io.mensagens);
        assertTrue(tela.contains("Carta recebida na última jogada"));
        assertTrue(tela.contains("\u001B[38;5;226m"));
        assertTrue(tela.contains("[5♦]"));
    }

    @Test
    void deveApresentarSomentePararAoAtingirVinteEUm() {
        EntradaSaidaFalsa io = new EntradaSaidaFalsa(0);
        DecisaoHumanaBlackjackConsole decisao =
                new DecisaoHumanaBlackjackConsole(io, CorTerminalBlackjack.SEM_COR);
        Jogador jogador = new JogadorPadrao("Você", decisao);
        Jogador casa = new JogadorPadrao("Casa", new EstrategiaCasaBlackjack());
        List<CartaBlackjack> mao = List.of(
                carta(ValorBlackjack.SETE, NaipeBlackjack.PAUS),
                carta(ValorBlackjack.SETE, NaipeBlackjack.COPAS),
                carta(ValorBlackjack.SETE, NaipeBlackjack.OUROS));
        var contexto = contexto(
                jogador, casa, mao, Optional.of(mao.getLast()),
                List.of(AcaoBlackjack.PARAR));

        assertEquals(AcaoBlackjack.PARAR, decisao.decidir(contexto));
        assertEquals(List.of("Parar com esta mão"), io.opcoes.getFirst());
        assertTrue(String.join("\n", io.mensagens).contains("Você chegou a 21"));
    }

    @Test
    void deveRejeitarContextoDestinadoACasa() {
        EntradaSaidaFalsa io = new EntradaSaidaFalsa();
        DecisaoHumanaBlackjackConsole decisao =
                new DecisaoHumanaBlackjackConsole(io);
        Jogador jogador = new JogadorPadrao("Você", decisao);
        Jogador casa = new JogadorPadrao("Casa", new EstrategiaCasaBlackjack());
        List<CartaBlackjack> mao = List.of(
                carta(ValorBlackjack.DEZ, NaipeBlackjack.PAUS),
                carta(ValorBlackjack.OITO, NaipeBlackjack.COPAS));
        var contexto = new ContextoDecisaoBlackjack(
                EtapaBlackjack.DECISAO,
                List.of(AcaoBlackjack.PARAR),
                casa,
                PapelBlackjack.CASA,
                2,
                mao,
                PontuacaoDaMaoBlackjack.calcular(mao),
                jogador,
                List.of(carta(ValorBlackjack.SETE, NaipeBlackjack.ESPADAS)),
                0,
                Optional.empty(),
                47);

        assertThrows(IllegalArgumentException.class, () -> decisao.decidir(contexto));
    }

    private static ContextoDecisaoBlackjack contexto(
            Jogador jogador,
            Jogador casa,
            List<CartaBlackjack> mao,
            Optional<CartaBlackjack> comprada,
            List<AcaoBlackjack> acoes) {
        return new ContextoDecisaoBlackjack(
                EtapaBlackjack.DECISAO,
                List.copyOf(acoes),
                jogador,
                PapelBlackjack.JOGADOR,
                1,
                mao,
                PontuacaoDaMaoBlackjack.calcular(mao),
                casa,
                List.of(carta(ValorBlackjack.NOVE, NaipeBlackjack.ESPADAS)),
                1,
                comprada,
                47);
    }

    private static CartaBlackjack carta(
            ValorBlackjack valor, NaipeBlackjack naipe) {
        return new CartaBlackjack(valor, naipe);
    }

    private static final class EntradaSaidaFalsa implements EntradaSaida {
        private final java.util.ArrayDeque<Integer> respostas;
        private final List<String> mensagens = new ArrayList<>();
        private final List<List<String>> opcoes = new ArrayList<>();

        private EntradaSaidaFalsa(Integer... respostas) {
            this.respostas = new java.util.ArrayDeque<>(List.of(respostas));
        }

        @Override
        public void exibir(String mensagem) {
            mensagens.add(mensagem);
        }

        @Override
        public int solicitarOpcao(String mensagem, List<String> opcoes) {
            this.opcoes.add(List.copyOf(opcoes));
            if (respostas.isEmpty()) {
                throw new AssertionError("O teste não forneceu uma resposta.");
            }
            return respostas.removeFirst();
        }
    }
}
