# Especificação arquitetural

**Versão:** baseline integrada da `main` em 16/08/2026.

**Escopo:** contratos existentes no código; itens futuros são marcados como pendentes.

## 1. Visão de módulos

| Módulo | Responsabilidade | Visibilidade |
|---|---|---|
| `cardgame.api` | contratos, valores, eventos e exceções públicas | pública |
| `cardgame.engine` | runtime e ciclo de vida | somente `MotorDePartida` público |
| `trinca` / `blackjack` | aplicações clientes | ainda pendentes na `main` |

Dependência permitida do runtime: `engine → api`. O runtime não pode importar um
pacote de jogo concreto.

## 2. Contratos de cartas

### `Carta`

Toda carta fornece `UUID id()` não nulo e estável. A API não fixa atributos visuais.

### `Baralho<C extends Carta>`

Operações implementadas:

- `quantidade()` e `estaVazio()`;
- `topo()`;
- `comprar()`;
- `colocarNoTopo(C)` e `colocarNaBase(C)`;
- `embaralhar()` e `embaralhar(RandomGenerator)`;
- `cartas()` como snapshot imutável.

`comprar()` em um baralho vazio lança `BaralhoVazioException`.

### `BaralhoFactory<C>`

`criar()` deve devolver um baralho novo e independente. O motor rejeita `null`.

### `MaoDeCartas<C>`

Disponibiliza quantidade, busca, adição, remoção e snapshot. Embora o contrato da
Trilha B possua mutadores públicos, `VisaoDaPartida` não devolve a mão; devolve
`List<C>`, impedindo que uma regra receba autoridade de mutação por acidente.

## 3. Distribuição

`EstrategiaDeDistribuicao<C>.distribuir(ContextoDeDistribuicao<C>)` recebe somente:

- lista imutável de jogadores;
- quantidade de cartas disponíveis;
- operação `entregarProximaCarta(Jogador)`.

Ela não recebe o baralho nem o mapa de mãos. `ContextoDeDistribuicaoInterno` adapta
essa porta ao estado da execução e permanece package-private.

`DistribuicaoAlternada<C>` é a implementação pronta por rodadas e valida previamente
se existem cartas suficientes.

## 4. Configuração

`PartidaConfig<C>` é imutável e criada por `PartidaConfig.<C>builder()`.

Campos obrigatórios:

- lista com pelo menos dois `Jogador`;
- `BaralhoFactory<C>`;
- `EstrategiaDeDistribuicao<C>`.

Campos obrigatórios de regra:

- `RegraDeValidacaoStrategy<C>`;
- `RegraDeVitoriaStrategy<C>`;
- `RegraDePontuacaoStrategy<C>`.

Campo opcional:

- índice do primeiro jogador, zero por padrão.

Validações:

- lista, elementos e identidades não nulos;
- identidades sem repetição;
- índice dentro do intervalo;
- cópia defensiva da lista.

As três regras integram `PartidaConfig` e são obrigatórias: uma partida não pode ser
configurada sem dizer como valida uma jogada, como reconhece o fim e como pontua.

## 5. Leitura e mutação durante a partida

### `VisaoDaPartida<C>`

Porta somente leitura:

- `estado()`;
- `jogadores()`;
- `jogadorAtual()`;
- `maoDe(Jogador)` como `List<C>` imutável;
- `quantidadeNoBaralho()`;
- `numeroDoTurno()`.

### `ContextoDePartida<C>`

Estende a visão e acrescenta:

- `comprarDoBaralho()`;
- `adicionarNaMao(Jogador, C)`;
- `removerDaMao(Jogador, UUID)`;
- `adicionarAoBaralho(Collection<? extends C>)`;
- `embaralharBaralho()`.

O contexto não permite avançar turno, alterar estado ou finalizar a partida.

Cartas adicionadas são rejeitadas quando sua identidade já está no baralho ou em
qualquer mão. Uma coleção com identidades repetidas é validada antes de alterar o
baralho.

## 6. Estado

`EstadoPartida` define o grafo:

```text
CONFIGURADA → PREPARANDO → EM_ANDAMENTO → FINALIZADA
```

`CicloDeVidaDaPartida` mantém o estado corrente e lança
`EstadoDePartidaInvalidoException` em transições ou operações incompatíveis. O enum é
o Especialista na Informação sobre destinos legais; o ciclo apenas aplica a política.

## 7. Execução

Assinatura conceitual:

```java
public abstract class MotorDePartida<C extends Carta> {
    public final ResultadoDePartida executar();
    public final EstadoPartida estado();
    public final void adicionarListener(PartidaListener listener);
    public final boolean removerListener(PartidaListener listener);

    protected abstract ResultadoDoTurno executarTurno(
            ContextoDePartida<C> contexto);

    protected final void validarJogada(
            VisaoDaPartida<C> contexto, Jogada jogada);

    protected final void publicarEvento(EventoDePartida evento);

    protected void preparar(ContextoDePartida<C> contexto);
    protected void aposDistribuir(ContextoDePartida<C> contexto);
    protected void aoEncerrar(
            VisaoDaPartida<C> contexto, ResultadoDePartida resultado);
}
```

Vitória e pontuação não são hooks: o motor as obtém de `PartidaConfig`.

Algoritmo fixo de `executar()`:

1. exigir `CONFIGURADA`;
2. transicionar para `PREPARANDO`;
3. criar e embaralhar o baralho;
4. criar mãos, ciclo e turnos internos;
5. chamar preparação, distribuição e pós-distribuição, publicando eventos;
6. transicionar para `EM_ANDAMENTO`;
7. avaliar encerramento antes do primeiro turno;
8. executar turnos, repetindo jogadas inválidas;
9. avaliar o desfecho após cada turno;
10. aplicar repetição/inversão/pulo/avanço;
11. validar desfecho e placar;
12. transicionar para `FINALIZADA`, chamar `aoEncerrar` e devolver o resultado.

O método é `final` para preservar Inversão de Controle.

## 8. Turnos

`ResultadoDoTurno` contém:

- `repetirJogador`;
- `inverterSentido`;
- `jogadoresAPular`.

Combinações inválidas são recusadas, por exemplo repetir e pular simultaneamente.
O gerenciador interno usa `Math.floorMod` e aceita qualquer quantidade de jogadores
maior ou igual a dois.

Se `executarTurno` lançar `JogadaInvalidaException`, o hook é repetido sem avançar a
vez. Cem recusas consecutivas encerram a tentativa com `IllegalStateException`.

## 9. Desfecho e resultado

`DesfechoDePartida` contém vencedores e motivo. `ResultadoDePartida` acrescenta o
placar final. Ambos copiam coleções defensivamente e usam identidade lógica de
jogador (`UUID`) nas validações.

Regras:

- motivo de vitória exige pelo menos um vencedor;
- motivo não pode ser vitória e empate ao mesmo tempo;
- vencedores não se repetem por identidade;
- vencedor precisa participar da partida;
- todo vencedor aparece no placar;
- o placar produzido pelo motor cobre exatamente os participantes.

`MotivoDeEncerramento` é extensível. O enum `MotivoPadrao` oferece casos comuns.

## 10. Encapsulamento

- listas de jogadores são copiadas na entrada;
- baralho e mãos devolvem snapshots;
- `VisaoDaPartida` não expõe `MaoDeCartas` mutável;
- mapa de placar e vencedores são imutáveis;
- colaboradores de `engine` não são públicos;
- nenhuma mutação de carta é aceita depois de `FINALIZADA`.

## 11. Regras e eventos

As três Strategies de regra recebem apenas leitura e são chamadas pelo motor dentro do
algoritmo final:

| Contrato | Quando é chamado |
|---|---|
| `RegraDeValidacaoStrategy<C>` | pelo jogo, via `validarJogada`, antes de aplicar a ação |
| `RegraDeVitoriaStrategy<C>` | após a distribuição e ao fim de cada turno |
| `RegraDePontuacaoStrategy<C>` | uma vez, na finalização |

O motor valida o que recebe de volta: desfecho não nulo, vencedor participante da
partida, placar cobrindo exatamente os participantes.

Publicação de eventos:

```text
PartidaIniciada → CartasDistribuidas
  → (TurnoIniciado → [JogadaRejeitada]* → TurnoEncerrado)*
  → PartidaFinalizada
```

A notificação percorre uma cópia da lista de ouvintes e isola cada um em seu próprio
`try/catch`. Um ouvinte com defeito não interrompe a partida, e um ouvinte que se
descadastra durante o callback não provoca `ConcurrentModificationException`.

Os seis eventos acima são os marcos padrão do ciclo. Dentro dos hooks, subclasses
podem intercalar eventos específicos do jogo chamando o método protegido e final
`publicarEvento(EventoDePartida)`. O método rejeita `null` e conserva a mesma política
de ordem, snapshot e isolamento; apenas o conteúdo e o momento do evento específico
ficam sob responsabilidade do jogo cliente.

## 12. Critérios de aceitação arquitetural

- `./mvnw test` passa;
- um cliente compila usando somente `api` e `engine.MotorDePartida`;
- API não importa engine;
- engine não importa clientes;
- nenhum detalhe interno de `engine` é público;
- Trinca e Blackjack reutilizam o mesmo algoritmo de ciclo de vida;
- novos jogos não exigem condicionais por tipo dentro do framework.

As regras sobre dependências de `api`, dependências de `engine` e visibilidade dos
internos são verificadas sobre o bytecode por `FronteirasArquiteturaisTest`. Uma
violação faz `./mvnw test` e o build falharem, mesmo que a dependência não apareça
como um `import` explícito.

Estado medido nesta versão: **137 testes aprovados**. Os testes dos clientes concretos
ainda são entrega futura.
