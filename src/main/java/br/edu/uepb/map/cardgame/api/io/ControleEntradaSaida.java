package br.edu.uepb.map.cardgame.api.io;

import br.edu.uepb.map.cardgame.api.EntradaSaida;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

/**
 * Implementação de {@link EntradaSaida} para um terminal de texto.
 *
 * <p><strong>Testabilidade.</strong> A construção padrão usa os fluxos do processo,
 * enquanto a construção com {@link Reader} e {@link Writer} permite testes sem
 * redirecionar estado global.
 *
 * @author Allan Guilherme da S. Vieira
 * @version 0.0.1
 */
public final class ControleEntradaSaida implements EntradaSaida {

    private final BufferedReader entrada;
    private final PrintWriter saida;

    /**
     * Usa a entrada e a saída padrão do processo sem assumir sua propriedade.
     */
    public ControleEntradaSaida() {
        this(
                new InputStreamReader(System.in, Charset.defaultCharset()),
                new PrintWriter(System.out, true)
        );
    }

    /**
     * Cria uma porta de console com fluxos injetados.
     *
     * @param entrada origem das respostas
     * @param saida destino das mensagens
     * @throws NullPointerException se algum fluxo for nulo
     */
    public ControleEntradaSaida(Reader entrada, Writer saida) {
        this.entrada = new BufferedReader(Objects.requireNonNull(entrada, "entrada"));
        Writer destino = Objects.requireNonNull(saida, "saida");
        this.saida = destino instanceof PrintWriter printWriter
                ? printWriter
                : new PrintWriter(destino, true);
    }

    @Override
    public void exibir(String mensagem) {
        saida.println(Objects.requireNonNull(mensagem, "mensagem"));
        saida.flush();
    }

    @Override
    public int solicitarOpcao(String mensagem, List<String> opcoes) {
        Objects.requireNonNull(mensagem, "mensagem");
        List<String> opcoesSeguras = List.copyOf(opcoes);
        if (opcoesSeguras.isEmpty()) {
            throw new IllegalArgumentException("opcoes não pode ser vazia");
        }

        while (true) {
            saida.println(mensagem);
            for (int indice = 0; indice < opcoesSeguras.size(); indice++) {
                saida.printf("%d - %s%n", indice + 1, opcoesSeguras.get(indice));
            }
            saida.flush();

            String resposta = lerLinha();
            try {
                int opcao = Integer.parseInt(resposta.strip());
                if (opcao >= 1 && opcao <= opcoesSeguras.size()) {
                    return opcao - 1;
                }
            } catch (NumberFormatException ignored) {
                // A mensagem abaixo cobre entradas não numéricas e fora da faixa.
            }
            saida.println("Opção inválida. Tente novamente.");
        }
    }

    /**
     * Solicita diretamente um número inteiro dentro de um intervalo, sem renderizar
     * uma lista de opções.
     *
     * @param mensagem mensagem apresentada antes da leitura
     * @param minimo menor valor aceito, inclusive
     * @param maximo maior valor aceito, inclusive
     * @return número informado dentro do intervalo
     * @throws NullPointerException se a mensagem for nula
     * @throws IllegalArgumentException se o intervalo for inválido
     * @throws IllegalStateException se a entrada terminar antes de um valor válido
     */
    public int solicitarInteiro(String mensagem, int minimo, int maximo) {
        Objects.requireNonNull(mensagem, "mensagem");
        if (minimo > maximo) {
            throw new IllegalArgumentException("minimo não pode ser maior que maximo");
        }

        while (true) {
            saida.println(mensagem);
            saida.flush();

            String resposta = lerLinha();
            try {
                int valor = Integer.parseInt(resposta.strip());
                if (valor >= minimo && valor <= maximo) {
                    return valor;
                }
            } catch (NumberFormatException ignored) {
                // A mensagem abaixo cobre entradas não numéricas e fora da faixa.
            }
            saida.printf("Valor inválido. Digite um número entre %d e %d.%n", minimo, maximo);
        }
    }

    private String lerLinha() {
        try {
            String linha = entrada.readLine();
            if (linha == null) {
                throw new IllegalStateException("entrada encerrada antes da escolha");
            }
            return linha;
        } catch (IOException excecao) {
            throw new UncheckedIOException("falha ao ler a entrada", excecao);
        }
    }
}
