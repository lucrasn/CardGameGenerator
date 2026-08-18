package br.edu.uepb.map.blackjack;

import br.edu.uepb.map.cardgame.api.MotivoDeEncerramento;

/** Motivos específicos que explicam o encerramento de uma rodada de Blackjack. */
public enum MotivoBlackjack implements MotivoDeEncerramento {

    /** O jogador formou 21 com as duas cartas iniciais. */
    BLACKJACK_DO_JOGADOR(true, false, "Blackjack natural do jogador"),
    /** A casa formou 21 com as duas cartas iniciais. */
    BLACKJACK_DA_CASA(true, false, "Blackjack natural da casa"),
    /** Jogador e casa formaram Blackjack natural. */
    BLACKJACKS_IGUAIS(false, true, "Blackjack natural dos dois participantes"),
    /** O jogador ultrapassou 21. */
    JOGADOR_ESTOUROU(true, false, "o jogador ultrapassou 21"),
    /** A casa ultrapassou 21. */
    CASA_ESTOUROU(true, false, "a casa ultrapassou 21"),
    /** Um participante venceu por possuir o maior total válido. */
    MAIOR_PONTUACAO(true, false, "maior pontuação sem ultrapassar 21"),
    /** Os dois participantes pararam com o mesmo total. */
    PONTUACOES_IGUAIS(false, true, "pontuações iguais");

    private final boolean vitoria;
    private final boolean empate;
    private final String descricao;

    MotivoBlackjack(boolean vitoria, boolean empate, String descricao) {
        this.vitoria = vitoria;
        this.empate = empate;
        this.descricao = descricao;
    }

    @Override
    public boolean ehVitoria() {
        return vitoria;
    }

    @Override
    public boolean ehEmpate() {
        return empate;
    }

    /**
     * Devolve uma descrição adequada para a tela de resultado.
     *
     * @return descrição do motivo
     */
    public String descricao() {
        return descricao;
    }
}
