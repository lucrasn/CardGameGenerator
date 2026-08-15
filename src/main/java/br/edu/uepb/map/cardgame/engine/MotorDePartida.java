package br.edu.uepb.map.cardgame.engine;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import br.edu.uepb.map.cardgame.api.Baralho;
import br.edu.uepb.map.cardgame.api.Carta;
import br.edu.uepb.map.cardgame.api.ContextoDePartida;
import br.edu.uepb.map.cardgame.api.DesfechoDePartida;
import br.edu.uepb.map.cardgame.api.EstadoPartida;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.PartidaConfig;
import br.edu.uepb.map.cardgame.api.ResultadoDePartida;
import br.edu.uepb.map.cardgame.api.ResultadoDoTurno;
import br.edu.uepb.map.cardgame.api.VisaoDaPartida;
import br.edu.uepb.map.cardgame.api.excecao.EstadoDePartidaInvalidoException;
import br.edu.uepb.map.cardgame.api.excecao.JogadaInvalidaException;

/**
 * Engine público e abstrato que controla o ciclo de vida de uma partida.
 *
 * <p>{@link #executar()} é o Template Method final: preparar, distribuir, executar
 * turnos, avaliar o desfecho, pontuar e encerrar. O jogo concreto fornece somente
 * os passos variáveis por hooks protegidos e nunca manipula o gerenciador de turnos.
 *
 * <p>Enquanto as Strategies de regra da Trilha D ainda são contratos vazios, os
 * hooks {@link #avaliarDesfecho(VisaoDaPartida)} e
 * {@link #calcularPontuacao(VisaoDaPartida, DesfechoDePartida)} mantêm o motor
 * executável sem assumir assinaturas que pertencem a outra trilha.
 *
 * @param <C> tipo de carta usado pela partida
 * @author Lucas N. de Araújo
 * @version 0.0.1
 * @since 2026-06-15
 */
public abstract class MotorDePartida<C extends Carta> {

    private static final int MAXIMO_DE_TENTATIVAS_POR_TURNO = 100;

    private final PartidaConfig<C> configuracao;
    private final CicloDeVidaDaPartida ciclo = new CicloDeVidaDaPartida();

    /**
     * Cria um motor configurado, ainda não executado.
     *
     * @param configuracao participantes, baralho e distribuição
     */
    protected MotorDePartida(PartidaConfig<C> configuracao) {
        this.configuracao = Objects.requireNonNull(
                configuracao, "A configuração da partida não pode ser nula.");
    }

    /**
     * Executa uma única vez a sequência completa da partida.
     *
     * @return resultado imutável
     * @throws EstadoDePartidaInvalidoException se chamado mais de uma vez
     */
    public final ResultadoDePartida executar() {
        ciclo.exigir(EstadoPartida.CONFIGURADA);
        ciclo.transicionarPara(EstadoPartida.PREPARANDO);

        Baralho<C> baralho = Objects.requireNonNull(
                configuracao.baralhoFactory().criar(),
                "A fábrica retornou um baralho nulo.");
        GerenciadorDeTurnos turnos = new GerenciadorDeTurnos(
                configuracao.jogadores(), configuracao.primeiroJogador());
        PartidaEmExecucao<C> partida = new PartidaEmExecucao<>(
                configuracao.jogadores(), baralho, turnos, ciclo);

        baralho.embaralhar();
        preparar(partida);
        configuracao.distribuicao().distribuir(new ContextoDeDistribuicaoInterno<>(partida));
        aposDistribuir(partida);

        ciclo.transicionarPara(EstadoPartida.EM_ANDAMENTO);
        Optional<DesfechoDePartida> desfechoInicial = avaliar(partida);
        if (desfechoInicial.isPresent()) {
            return finalizar(partida, desfechoInicial.orElseThrow());
        }

        long numeroDoTurno = 1;
        while (true) {
            partida.definirNumeroDoTurno(numeroDoTurno);
            ResultadoDoTurno resultadoDoTurno = executarAteJogadaValida(partida);

            Optional<DesfechoDePartida> desfecho = avaliar(partida);
            if (desfecho.isPresent()) {
                return finalizar(partida, desfecho.orElseThrow());
            }

            aplicarDiretiva(turnos, resultadoDoTurno);
            numeroDoTurno = Math.incrementExact(numeroDoTurno);
        }
    }

    /** @return estado público atual do ciclo de vida */
    public final EstadoPartida estado() {
        return ciclo.estado();
    }

    /** Hook opcional chamado antes da distribuição inicial. */
    protected void preparar(ContextoDePartida<C> contexto) {
        // Hook deliberadamente vazio.
    }

    /** Hook opcional chamado após a distribuição e antes do primeiro turno. */
    protected void aposDistribuir(ContextoDePartida<C> contexto) {
        // Hook deliberadamente vazio.
    }

    /**
     * Executa um turno completo sem avançar ou finalizar diretamente.
     *
     * @param contexto operações genéricas controladas
     * @return diretiva de controle a ser aplicada pelo engine
     */
    protected abstract ResultadoDoTurno executarTurno(ContextoDePartida<C> contexto);

    /**
     * Avalia se o estado corrente encerra a partida.
     *
     * @param contexto visão somente leitura
     * @return desfecho quando a partida terminou; vazio caso contrário
     */
    protected abstract Optional<DesfechoDePartida> avaliarDesfecho(
            VisaoDaPartida<C> contexto);

    /**
     * Calcula o placar final. A implementação padrão atribui zero a todos.
     *
     * @param contexto visão final da partida
     * @param desfecho desfecho já validado
     * @return placar contendo exatamente todos os participantes
     */
    protected Map<Jogador, Integer> calcularPontuacao(
            VisaoDaPartida<C> contexto, DesfechoDePartida desfecho) {
        Map<Jogador, Integer> placar = new LinkedHashMap<>();
        contexto.jogadores().forEach(jogador -> placar.put(jogador, 0));
        return placar;
    }

    /** Hook opcional chamado depois da transição para o estado final. */
    protected void aoEncerrar(VisaoDaPartida<C> contexto, ResultadoDePartida resultado) {
        // Hook deliberadamente vazio.
    }

    private ResultadoDoTurno executarAteJogadaValida(PartidaEmExecucao<C> partida) {
        JogadaInvalidaException ultimaRecusa = null;
        for (int tentativa = 1; tentativa <= MAXIMO_DE_TENTATIVAS_POR_TURNO; tentativa++) {
            try {
                return Objects.requireNonNull(
                        executarTurno(partida), "O turno não pode devolver uma diretiva nula.");
            } catch (JogadaInvalidaException excecao) {
                ultimaRecusa = excecao;
            }
        }
        throw new IllegalStateException(
                "O jogador " + partida.jogadorAtual().nome()
                        + " não produziu jogada válida em "
                        + MAXIMO_DE_TENTATIVAS_POR_TURNO + " tentativas no turno "
                        + partida.numeroDoTurno() + ".",
                ultimaRecusa);
    }

    private Optional<DesfechoDePartida> avaliar(PartidaEmExecucao<C> partida) {
        Optional<DesfechoDePartida> avaliacao = Objects.requireNonNull(
                avaliarDesfecho(partida), "A avaliação de desfecho não pode devolver null.");
        avaliacao.ifPresent(desfecho -> validarVencedores(partida, desfecho));
        return avaliacao;
    }

    private static void validarVencedores(
            VisaoDaPartida<?> partida, DesfechoDePartida desfecho) {
        for (Jogador vencedor : desfecho.vencedores()) {
            boolean participante = partida.jogadores().stream()
                    .anyMatch(jogador -> jogador.id().equals(vencedor.id()));
            if (!participante) {
                throw new IllegalStateException(
                        "O desfecho indicou um vencedor que não participa da partida.");
            }
        }
    }

    private ResultadoDePartida finalizar(
            PartidaEmExecucao<C> partida, DesfechoDePartida desfecho) {
        Map<Jogador, Integer> placar = Objects.requireNonNull(
                calcularPontuacao(partida, desfecho),
                "O cálculo de pontuação não pode devolver null.");
        validarPlacar(partida, placar);
        ResultadoDePartida resultado = new ResultadoDePartida(
                desfecho.vencedores(), placar, desfecho.motivo());
        ciclo.transicionarPara(EstadoPartida.FINALIZADA);
        aoEncerrar(partida, resultado);
        return resultado;
    }

    private static void validarPlacar(VisaoDaPartida<?> partida,
                                      Map<Jogador, Integer> placar) {
        Set<UUID> participantes = new HashSet<>();
        partida.jogadores().forEach(jogador -> participantes.add(jogador.id()));
        Set<UUID> pontuados = new HashSet<>();
        for (Map.Entry<Jogador, Integer> entrada : placar.entrySet()) {
            Jogador jogador = Objects.requireNonNull(
                    entrada.getKey(), "O placar não pode ter jogador nulo.");
            Objects.requireNonNull(
                    entrada.getValue(), "O placar não pode ter pontuação nula.");
            UUID id = Objects.requireNonNull(
                    jogador.id(), "Um jogador pontuado não pode ter id nulo.");
            if (!pontuados.add(id)) {
                throw new IllegalStateException(
                        "O placar contém duas entradas para a mesma identidade.");
            }
        }
        if (!participantes.equals(pontuados)) {
            throw new IllegalStateException(
                    "O placar deve registrar exatamente os participantes da partida.");
        }
    }

    private static void aplicarDiretiva(
            GerenciadorDeTurnos turnos, ResultadoDoTurno resultado) {
        if (resultado.inverterSentido()) {
            turnos.inverterSentido();
        }
        if (!resultado.repetirJogador()) {
            turnos.pularProximos(resultado.jogadoresAPular());
            turnos.avancar();
        }
    }
}
