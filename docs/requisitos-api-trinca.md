# Contratos públicos necessários para a Trinca

**Destinatários:** Trilhas A, B, C e D  
**Origem:** [regras-trinca.md](regras-trinca.md)  
**Objetivo:** fechar uma API mínima que permita implementar a Trinca sem o pacote
`br.edu.uepb.map.trinca` depender de classes internas do `core`.

> Isto descreve capacidades e responsabilidades, não assinaturas Java finais. As
> assinaturas pertencem aos donos de cada trilha e devem ser congeladas na reunião
> da Fase 0.

## Contrato mínimo por trilha

| Trilha | A API precisa permitir | Tipos públicos esperados |
|---|---|---|
| **A - Motor e ciclo de vida** | Configurar jogadores, baralho, regras e listeners; iniciar a partida; executar o ciclo preparar -> distribuir -> turnos -> finalizar; expor resultado/estado somente para leitura. | `MotorDePartida`, `EstadoPartida`, configuração da partida, resultado da partida. |
| **B - Cartas, baralho e mesa** | Criar dois baralhos franceses; embaralhar; distribuir nove cartas alternadamente; comprar do monte; reciclar descarte preservando o topo; consultar o topo e descartar na mesa; consultar mão sem expor a coleção mutável. | `Carta`, `BaralhoFactory`, estratégia de distribuição, `MaoDeCartas`, `Mesa`. |
| **C - Jogadores e decisões** | Representar jogador com nome, mão e pontuação; solicitar uma decisão sem o motor conhecer console ou bot; devolver uma jogada tipada que expresse compra e descarte. A primeira UI é humano x humano. | `Jogador`, `EstrategiaDeDecisao`, `Jogada`, `OrigemCompra`, abstração de I/O. |
| **D - Regras, exceções e eventos** | Validar turno, compra e descarte; calcular pontuação; identificar nove cartas organizadas em trincas/sequências e empate; notificar eventos; sinalizar violações de domínio. | `RegraDeValidacaoStrategy`, `RegraDePontuacaoStrategy`, `RegraDeVitoriaStrategy`, `PartidaListener`, exceções de domínio. |

## Capacidades obrigatórias em detalhe

### 1. Cartas e baralho - B

- `Carta` deve permitir ao cliente concreto identificar **valor** e **naipe**, sem
  obrigar o `core` a conhecer carta francesa.
- `BaralhoFactory` deve produzir um baralho configurável; para Trinca, dois conjuntos
  de 52 cartas sem curingas.
- O baralho deve fornecer operações controladas para embaralhar, verificar se está
  vazio e remover a carta do topo.
- A distribuição deve ser uma estratégia configurável e suportar "9 cartas por
  jogador, alternando uma por vez".
- `MaoDeCartas` e `Mesa` devem expor apenas visão imutável/cópia defensiva de suas
  cartas; mutações ocorrem por métodos de domínio.

### 2. Jogada e decisão - C

Uma jogada não pode ser uma `String` lida do console. Compra e descarte são decisões
separadas: o jogador só pode escolher o descarte depois de ver a carta comprada. A
estratégia recebe uma visão somente leitura do estado e devolve uma ação tipada.
Assim, humano, bot aleatório e bot guloso usam o mesmo contrato.

### 3. Regras - D

As três estratégias devem permanecer independentes:

- **Validação:** é turno do jogador, a origem de compra é válida, a carta descartada
  pertence à mão e as quantidades de cartas são corretas (10 após compra, 9 após
  descarte).
- **Pontuação:** vitória concede 1 ponto; empate concede 0 aos dois jogadores.
- **Vitória:** depois do descarte, as nove cartas da mão formam trincas e/ou
  sequências válidas. O descarte é reciclado quando o monte termina; se não houver
  cartas a reciclar, a partida empata.

As falhas devem ser comunicadas por exceções de domínio, no mínimo
`JogadaInvalidaException`, `BaralhoVazioException` e
`EstadoDePartidaInvalidoException`. O console captura uma jogada inválida, mostra a
mensagem e permite que o mesmo jogador escolha outra ação; ele não encerra a partida.

### 4. Eventos - D, consumidos por E

`PartidaListener` deve possibilitar observar, no mínimo:

- partida iniciada/cartas distribuídas;
- turno iniciado;
- carta comprada;
- carta descartada;
- jogada inválida;
- turno encerrado;
- partida finalizada, com vencedor ou empate.

O evento de compra não deve vazar a carta da mão de um jogador adversário para a
interface de outro jogador.

## Critérios de aceite da API

A API estará pronta para a Trilha E quando for possível implementar, somente com
tipos públicos:

1. uma `CartaFrancesa` e uma fábrica de dois baralhos de 52 cartas no pacote da Trinca;
2. uma partida com dois jogadores e distribuição inicial de nove cartas;
3. duas ações tipadas: "comprar do descarte" e, depois, "descartar carta X";
4. uma regra que reconheça nove cartas organizadas em trincas e sequências após o descarte;
5. um listener de console que mostre o fluxo sem ser importado pelo motor;
6. um bot e um humano disputando a mesma partida;
7. um tratamento de jogada inválida sem corromper o estado da partida.

## Proposta de decisões para congelar na reunião

Esta é a recomendação da Trilha E. A equipe pode alterá-la na reunião, mas qualquer
mudança deve manter os critérios de aceite deste documento.

### 1. Nomes, pacotes e fronteira pública

Manter os contratos que o cliente pode importar em
`br.edu.uepb.map.cardgame.api` e as implementações internas em
`br.edu.uepb.map.cardgame.core`. O pacote `br.edu.uepb.map.trinca` importa somente
`api`.

| Pacote | Tipos propostos | Responsável |
|---|---|---|
| `api` | `Carta`, `Jogador`, `BaralhoFactory`, `Jogada`, `Compra`, `Descarte`, `OrigemCompra`, `ContextoDeDecisao`, `FaseDoTurno`, `EstrategiaDeDecisao` | B e C |
| `api` | `Partida`, `PartidaConfig`, `Partidas`, `EstadoPartida`, `ResultadoPartida` | A |
| `api` | `RegraDeValidacaoStrategy`, `RegraDePontuacaoStrategy`, `RegraDeVitoriaStrategy`, `PartidaListener`, `EventoDePartida` e exceções | D |
| `core` | `MotorDePartida`, `GerenciadorDeTurnos`, `BaralhoBase`, `MaoDeCartas`, `Mesa` e implementações das estratégias | A, B, C e D |

`Partidas` é a fachada/fábrica pública que cria uma `Partida` a partir de uma
`PartidaConfig`. Assim, a aplicação cliente não instancia nem importa
`MotorDePartida` diretamente.

### 2. Jogadas imutáveis e em duas fases

**Decisão recomendada:** `Jogada` é uma interface selada, com dois `record`s
imutáveis. Ela não deve ser um único `record` contendo compra e descarte, pois o
descarte depende da carta que acabou de ser comprada.

```java
public sealed interface Jogada permits Compra, Descarte {}

public record Compra(OrigemCompra origem) implements Jogada {}

public record Descarte(Carta carta) implements Jogada {}

public enum OrigemCompra { MONTE, DESCARTE }
```

Como a Trinca utiliza dois baralhos, duas cartas podem ter mesmo valor e naipe. Por
isso, `Carta` deve ter identidade estável e única (por exemplo, `UUID id()`), além
dos atributos específicos que o cliente concreto precisar. A remoção da mão usa essa
identidade, não apenas valor e naipe.

### 3. Contexto entregue à estratégia de decisão

**Decisão recomendada:** o motor chama uma única operação da estratégia para cada
fase do turno:

```java
Jogada decidir(ContextoDeDecisao contexto);
```

`ContextoDeDecisao` é imutável e contém somente o que o jogador atual pode conhecer:

- identificador e nome do jogador atual;
- cópia imutável da própria mão;
- topo do descarte, se existir;
- quantidade de cartas no monte;
- `FaseDoTurno` (`COMPRAR` ou `DESCARTAR`).

O motor valida que uma `Compra` seja devolvida na fase `COMPRAR` e um `Descarte` na
fase `DESCARTAR`. Mãos dos adversários, ordem futura do monte e detalhes internos da
mesa nunca entram no contexto.

### 4. Eventos e visibilidade das cartas

**Decisão recomendada:** `PartidaListener` recebe um `EventoDePartida` tipado. Os
eventos de domínio podem ser `record`s imutáveis; o listener decide como apresentá-los
no console.

| Evento | Dados permitidos |
|---|---|
| `PartidaIniciada` | jogadores e quantidade de cartas distribuídas; nunca as mãos completas |
| `TurnoIniciado` | jogador atual e número do turno |
| `CartaComprada` | jogador e origem; **não** a carta comprada |
| `CartaDescartada` | jogador e carta descartada, pois ela é pública |
| `JogadaRejeitada` | jogador, fase e mensagem da exceção |
| `TurnoEncerrado` | jogador |
| `PartidaFinalizada` | vencedor opcional, empate e placar |

O console mostra a própria mão a partir de `ContextoDeDecisao`, não a partir de
eventos globais. Isso evita vazamento de informação quando houver bot ou mais de um
cliente no futuro.

### 5. Hierarquia e política de exceções

**Decisão recomendada:** exceções de domínio são não verificadas.

```text
PartidaException extends RuntimeException
|- JogadaInvalidaException
|- EstadoDePartidaInvalidoException
`- BaralhoVazioException
```

- `JogadaInvalidaException`: erro recuperável do usuário; o motor notifica o listener
  e pede uma nova ação ao mesmo jogador.
- `EstadoDePartidaInvalidoException`: chamada incompatível com o ciclo de vida; não
  deve ser escondida pelo console.
- `BaralhoVazioException`: proteção de baixo nível; na Trinca, o motor tenta reciclar
  o descarte antes de permitir que ela resulte em empate.

### 6. Configuração de distribuição e turnos

**Decisão recomendada:** `PartidaConfig` é imutável e usa builder. Ela recebe a lista
de jogadores, fábrica de baralho, estratégias de regra, distribuição, primeiro
jogador e listeners.

Para a Trinca, a configuração congelada é:

```text
cartas iniciais: 9
distribuição: alternada, uma carta por jogador a cada rodada
primeiro jogador: índice 0 da lista configurada
turnos seguintes: ciclo circular na ordem da lista
```

`EstrategiaDistribuicao` é um ponto de extensão público, pois o enunciado exige
formas diferentes de distribuir cartas. Já a ordem circular fica dentro de
`GerenciadorDeTurnos`: ela atende à expansão para N jogadores sem introduzir uma
segunda estratégia que nenhum jogo atual precisa.

## Checklist final de aprovação

- [ ] A, B, C e D aceitam os nomes e a fronteira de pacotes da seção 1.
- [ ] C aceita `Jogada` selada, com `Compra` e `Descarte` como `record`s.
- [ ] C implementa `ContextoDeDecisao` sem dados privados dos adversários.
- [ ] D aceita os eventos e a política de visibilidade da seção 4.
- [ ] D aceita a hierarquia de exceções não verificadas da seção 5.
- [ ] A e B aceitam a configuração de distribuição e turnos da seção 6.
