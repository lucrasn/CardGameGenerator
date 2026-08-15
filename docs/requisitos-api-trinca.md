# Contratos públicos necessários para a Trinca

**Destinatários:** Trilhas A, B, C e D  
**Origem:** [regras-trinca.md](regras-trinca.md)  
**Status:** proposta revisada para aprovação na Fase 0.

## 1. Decisão arquitetural que orienta os contratos

O projeto é um framework, não uma fábrica automática de jogos. A Trinca e o
Blackjack são aplicações clientes que especializam o fluxo reutilizável da partida.

Para manter coerência com o **Template Method** já adotado no projeto, a equipe deve
usar este modelo:

```text
api.MotorDePartida (classe abstrata; executar() final)
├── trinca.MotorDeTrinca
└── blackjack.MotorDeBlackjack
```

`MotorDePartida` é uma abstração pública do framework, apesar de sua implementação
usar serviços internos de `core`. O cliente pode estendê-la e importar somente `api`;
ele não pode importar `BaralhoBase`, `MaoDeCartas`, `Mesa` ou outro tipo interno.

Uma fachada `Partidas` não será usada nesta versão, pois esconderia exatamente o
ponto de extensão por herança que justifica o Template Method no relatório.

## 2. Tipos públicos e fronteira de pacotes

| Pacote | Tipos públicos propostos | Responsável |
|---|---|---|
| `api` | `Carta`, `Baralho`, `BaralhoFactory`, `EstrategiaDistribuicao`, `ContextoDeDistribuicao` | B |
| `api` | `Jogador`, `Jogada`, `EstrategiaDeDecisao`, `ContextoDeDecisao` | C |
| `api` | `MotorDePartida`, `PartidaConfig`, `EstadoPartida`, `ResultadoPartida`, `ContextoDePartida` | A |
| `api` | `RegraDeValidacaoStrategy`, `RegraDePontuacaoStrategy`, `RegraDeVitoriaStrategy`, seus contextos, `PartidaListener`, eventos e exceções | D |
| `core` | `BaralhoBase`, `MaoDeCartas`, `Mesa`, `GerenciadorDeTurnos` e infraestrutura do motor | donos das respectivas trilhas |
| `trinca` / `blackjack` | cartas, regras, ações, estratégias, listeners e motores concretos dos jogos | E, com apoio das demais trilhas |

O requisito de separação será verificado mecanicamente: nenhum arquivo nos pacotes
de jogos concretos pode importar `br.edu.uepb.map.cardgame.core`.

## 3. Baralho e distribuição - B

### 3.1 `Baralho` é um contrato público

`BaralhoFactory` precisa retornar um tipo público. A proposta é:

```java
public interface Baralho {
    boolean estaVazio();
    int quantidadeDeCartas();
    Carta comprar();
    void embaralhar();
}

public interface BaralhoFactory {
    Baralho criarBaralho();
}
```

`BaralhoBase` implementa `Baralho` em `core`. O cliente recebe e configura um
`Baralho` pela fábrica, mas nunca vê a lista interna nem depende da implementação.

Como a Trinca usa dois baralhos, uma carta deve possuir identidade única e estável
(por exemplo, `UUID id()`), além dos atributos específicos de carta francesa. Isso
evita ambiguidade entre duas cartas com mesmo valor e naipe.

### 3.2 Distribuição por contexto controlado

Uma estratégia pública não pode receber `BaralhoBase` e `MaoDeCartas`. Ela recebe um
contexto que expõe apenas operações autorizadas:

```java
public interface EstrategiaDistribuicao {
    void distribuir(ContextoDeDistribuicao contexto);
}

public interface ContextoDeDistribuicao {
    List<Jogador> jogadores();             // visão imutável
    Carta comprarDoBaralho();
    void entregarCarta(Jogador jogador, Carta carta);
}
```

A implementação `DistribuicaoAlternada` da Trinca chama essas operações para entregar
9 cartas, uma por vez, a cada jogador. O contexto verifica pré-condições e mantém a
posse das coleções internas.

## 4. Ações e decisão - C

`Jogada` é uma interface pública **não selada** e imutável por convenção. Cada jogo
define seus próprios `record`s de ação no respectivo pacote cliente:

```java
// trinca
public record Comprar(OrigemCompra origem) implements Jogada {}
public record Descartar(Carta carta) implements Jogada {}

// blackjack
public record PedirCarta() implements Jogada {}
public record Parar() implements Jogada {}
public record DobrarAposta() implements Jogada {}
public record DividirMao() implements Jogada {}
```

Assim, a Trinca mantém compra e descarte como decisões separadas, e o Blackjack pode
introduzir `Parar`, `DobrarAposta`, `DividirMao` ou outra ação sem editar o framework.

```java
public interface EstrategiaDeDecisao {
    Jogada decidir(ContextoDeDecisao contexto);
}
```

`ContextoDeDecisao` é imutável e inclui somente informações que o jogador atual pode
conhecer: identificador/nome próprio, mão própria como lista imutável, informações
públicas de mesa, quantidade de cartas no monte e as ações permitidas naquele ponto
do turno. Nunca inclui mão de adversário, ordem do monte ou coleções mutáveis.

## 5. Contextos de regras - D

As regras concretas não recebem `MaoDeCartas`, `Mesa` ou `BaralhoBase`. Cada contrato
de regra recebe seu próprio contexto público, somente para leitura:

| Estratégia | Contexto mínimo | Responsabilidade |
|---|---|---|
| `RegraDeValidacaoStrategy` | jogador atual, `Jogada`, estado/fase, mão do jogador atual, informações públicas de mesa e quantidade do monte | Aceitar ou rejeitar a ação atual. |
| `RegraDeVitoriaStrategy` | jogadores, jogador atual, mãos como visões imutáveis, estado da partida e resultado parcial | Indicar vencedor ou informar que a partida continua. |
| `RegraDePontuacaoStrategy` | jogadores, vencedor/empate e dados finais da partida | Calcular o placar final. |

Nomes sugeridos: `ContextoDeValidacao`, `ContextoDeVitoria` e
`ContextoDePontuacao`. A regra de validação lança `JogadaInvalidaException`; as regras
de vitória e pontuação retornam um resultado de domínio, sem alterar o estado.

**Limite de responsabilidade:** reciclar a pilha de descarte é uma operação da mesa,
coordenada pelo fluxo do motor antes da compra. Não pertence a
`RegraDeVitoriaStrategy`; esta apenas inspeciona se uma mão ou estado venceu.

## 6. Eventos e privacidade - D

`PartidaListener` observa eventos tipados e imutáveis. A proposta de dados é:

| Evento | Dados expostos |
|---|---|
| `PartidaIniciada` | jogadores e quantidade distribuída; nunca mãos completas |
| `TurnoIniciado` | jogador atual e número do turno |
| `CartaComprada` | jogador e origem; não a carta comprada |
| `CartaDescartada` | jogador e carta descartada, pois é pública |
| `JogadaRejeitada` | jogador, ação e mensagem segura da exceção |
| `TurnoEncerrado` | jogador |
| `PartidaFinalizada` | vencedor opcional, empate e placar |

O console recebe a própria mão através do `ContextoDeDecisao`, nunca por evento
global. Isso preserva o encapsulamento e evita vazamento de informação para o outro
jogador ou para futuras interfaces.

## 7. Exceções - D

Todas as exceções de domínio são não verificadas:

```text
PartidaException extends RuntimeException
|- JogadaInvalidaException
|- EstadoDePartidaInvalidoException
`- BaralhoVazioException
```

O motor captura `JogadaInvalidaException`, publica `JogadaRejeitada` e solicita outra
ação ao mesmo jogador. `EstadoDePartidaInvalidoException` sinaliza uso incorreto do
ciclo de vida. `BaralhoVazioException` é uma proteção de baixo nível: antes de ela
interromper a Trinca, o motor coordena a reciclagem do descarte pela mesa.

## 8. Configuração, Builder e turnos - A

`PartidaConfig` passou a ter jogadores, fábrica de baralho, estratégia de
distribuição, três regras, primeiro jogador e listeners. Portanto, um **Builder** é
justificável por legibilidade e para impedir construtores longos e posicionais:

```java
PartidaConfig config = PartidaConfig.builder()
    .jogadores(jogadores)
    .baralhoFactory(factory)
    .estrategiaDistribuicao(distribuicao)
    .regraValidacao(validacao)
    .regraVitoria(vitoria)
    .regraPontuacao(pontuacao)
    .primeiroJogador(indice)
    .listeners(listeners)
    .build();
```

Para a Trinca, a configuração concreta é: 2 jogadores, 9 cartas iniciais,
`DistribuicaoAlternada`, primeiro jogador no índice 0 e turnos em ciclo circular na
ordem da lista. A ordem circular é responsabilidade interna de
`GerenciadorDeTurnos`; distribuição continua um ponto público de extensão.

## 9. Critérios de aceite da API

A API estará pronta para a Trilha E quando for possível, usando apenas `api`:

1. criar um `Baralho` de dois baralhos franceses por uma `BaralhoFactory`;
2. especializar `MotorDePartida` em `MotorDeTrinca` sem importar `core`;
3. distribuir nove cartas alternadamente por `ContextoDeDistribuicao`;
4. criar ações próprias da Trinca e do Blackjack sem editar `Jogada`;
5. implementar validação, vitória e pontuação usando apenas seus contextos públicos;
6. reciclar o descarte no fluxo da mesa/motor, sem colocá-lo na regra de vitória;
7. apresentar eventos de console sem revelar cartas privadas;
8. tratar jogada inválida sem corromper ou encerrar a partida.

## 10. Decisões que a reunião precisa aprovar

- [ ] `Baralho`, `ContextoDeDistribuicao` e os três contextos de regra entram na API.
- [ ] O modelo de Template Method com motores concretos por jogo substitui a fachada
      `Partidas` nesta versão.
- [ ] `Jogada` é extensível e ações concretas pertencem aos pacotes dos jogos.
- [ ] O Builder de `PartidaConfig` é adotado e registrado como decisão de projeto.
- [ ] A reciclagem do descarte pertence a mesa/motor, não à regra de vitória.
