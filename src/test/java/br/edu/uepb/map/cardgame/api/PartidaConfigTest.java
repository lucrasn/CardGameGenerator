package br.edu.uepb.map.cardgame.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.apoio.CartaFalsa;
import br.edu.uepb.map.cardgame.apoio.JogadorDeTeste;

@DisplayName("PartidaConfig — Builder genérico e invariantes")
class PartidaConfigTest {

    @Test
    @DisplayName("copia jogadores e preserva os colaboradores configurados")
    void constroiConfiguracaoImutavel() {
        List<Jogador> jogadores = new ArrayList<>(List.of(
                new JogadorDeTeste("Ana"), new JogadorDeTeste("Bruno")));
        BaralhoFactory<CartaFalsa> fabrica = () -> new BaralhoPadrao<>(List.of());
        EstrategiaDeDistribuicao<CartaFalsa> distribuicao = contexto -> { };
        RegraDeValidacaoStrategy<CartaFalsa> validacao = contexto -> { };
        RegraDeVitoriaStrategy<CartaFalsa> vitoria = contexto -> Optional.empty();
        RegraDePontuacaoStrategy<CartaFalsa> pontuacao = (contexto, desfecho) -> {
            var placar = new LinkedHashMap<Jogador, Integer>();
            contexto.jogadores().forEach(jogador -> placar.put(jogador, 0));
            return placar;
        };

        PartidaConfig<CartaFalsa> config = PartidaConfig.<CartaFalsa>builder()
                .jogadores(jogadores)
                .baralhoFactory(fabrica)
                .distribuicao(distribuicao)
                .regraDeValidacao(validacao)
                .regraDeVitoria(vitoria)
                .regraDePontuacao(pontuacao)
                .primeiroJogador(1)
                .build();
        jogadores.add(new JogadorDeTeste("Carla"));

        assertEquals(2, config.jogadores().size());
        assertEquals(1, config.primeiroJogador());
        assertSame(fabrica, config.baralhoFactory());
        assertSame(distribuicao, config.distribuicao());
        assertSame(validacao, config.regraDeValidacao());
        assertSame(vitoria, config.regraDeVitoria());
        assertSame(pontuacao, config.regraDePontuacao());
        assertThrows(UnsupportedOperationException.class,
                () -> config.jogadores().add(new JogadorDeTeste("Davi")));
    }

    @Test
    @DisplayName("rejeita identidades repetidas")
    void rejeitaIdentidadesRepetidas() {
        UUID id = UUID.randomUUID();
        List<Jogador> jogadores = List.of(
                new JogadorDeTeste(id, "Ana"), new JogadorDeTeste(id, "Cópia"));

        assertThrows(IllegalArgumentException.class, () -> base(jogadores).build());
    }

    @Test
    @DisplayName("rejeita configuração incompleta, menos de dois jogadores e índice inválido")
    void validaEntradas() {
        assertThrows(NullPointerException.class,
                () -> PartidaConfig.<CartaFalsa>builder().build());
        assertThrows(IllegalArgumentException.class,
                () -> base(List.of(new JogadorDeTeste("Ana"))).build());
        assertThrows(IllegalArgumentException.class,
                () -> base(List.of(new JogadorDeTeste("Ana"), new JogadorDeTeste("Bruno")))
                        .primeiroJogador(2)
                        .build());
    }

    private static PartidaConfig.Builder<CartaFalsa> base(List<Jogador> jogadores) {
        return PartidaConfig.<CartaFalsa>builder()
                .jogadores(jogadores)
                .baralhoFactory(() -> new BaralhoPadrao<>(List.of()))
                .distribuicao(contexto -> { })
                .regraDeValidacao(contexto -> { })
                .regraDeVitoria(contexto -> Optional.empty())
                .regraDePontuacao((contexto, desfecho) -> {
                    var placar = new LinkedHashMap<Jogador, Integer>();
                    contexto.jogadores().forEach(jogador -> placar.put(jogador, 0));
                    return placar;
                });
    }
}
