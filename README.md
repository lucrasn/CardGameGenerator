# CardGame Framework

Mini framework orientado a objetos para construir jogos de cartas em Java. O projeto
é desenvolvido para a disciplina de Métodos Avançados de Programação (MAP) da UEPB e
usa padrões GoF, SOLID e GRASP como decisões verificáveis no código.

## Estado atual

A `main` contém a parte reutilizável das Trilhas A, B, C e D integrada:

- `cardgame.api`: contratos públicos de cartas, jogadores, configuração, contexto,
  regras, eventos e resultado;
- `cardgame.engine.MotorDePartida<C>`: Template Method público do ciclo de vida;
- colaboradores internos de estado e turnos sem `public` no pacote `engine`;
- baralho e distribuição genéricos da Trilha B;
- jogadores e estratégias de decisão da Trilha C;
- regras, eventos e exceções de domínio da Trilha D.

As três Strategies de regra — `RegraDeValidacaoStrategy`, `RegraDeVitoriaStrategy` e
`RegraDePontuacaoStrategy` — são obrigatórias em `PartidaConfig` e o motor as consulta
durante a execução. O Observer está implementado: `MotorDePartida` cadastra
observadores, publica os seis eventos padrão de `cardgame.api.evento` e permite que
subclasses publiquem eventos próprios pelo ponto protegido `publicarEvento`.

Na `main`, os jogos continuam ausentes por decisão arquitetural. Esta branch
`jogo/blackjack` acrescenta um cliente completo sem modificar a API ou os internals do
engine: uma pessoa joga contra a casa automatizada, com carta fechada, Ás flexível,
Blackjack natural, estouro, empate, placar acumulado e interface ANSI de terminal. A
Trinca permanece isolada em `jogo/trinca`.

A suíte desta branch executa 168 testes com sucesso, incluindo 30 testes próprios do
Blackjack e uma fronteira ArchUnit que impede o cliente de acessar internals do engine.

### Padrões GoF em runtime

| Padrão | Onde |
|---|---|
| Template Method | `MotorDePartida.executar()`, `final` |
| Factory Method | `BaralhoFactory.criar()` |
| Strategy | distribuição, decisão, validação, vitória e pontuação |
| Observer | `MotorDePartida` + `PartidaListener` + `api.evento` |

`PartidaConfig.Builder` é decisão auxiliar de construção e fica fora dessa contagem.

## Arquitetura

```text
jogo cliente ──────> cardgame.api
       │
       └───────────> cardgame.engine.MotorDePartida

cardgame.engine ───> cardgame.api
```

Somente `MotorDePartida` é público em `engine`. `GerenciadorDeTurnos`,
`SentidoDeRotacao`, `CicloDeVidaDaPartida`, `PartidaEmExecucao` e o adaptador de
distribuição são detalhes do runtime.

O framework reutilizável ocupa os conjuntos de pacotes `api` e `engine`; o cliente
fica no pacote independente `blackjack`. A fronteira é garantida pelo compilador: uma
única classe pública em `engine` significa que nenhum jogo consegue alcançar o
gerenciador de turnos ou forçar uma transição de estado.

`FronteirasArquiteturaisTest` usa ArchUnit para analisar o bytecode de produção. O
build falha se `api` depender de `engine` ou de clientes, se `engine` depender de um
jogo concreto, ou se outro tipo público aparecer em `engine`.
`ArquiteturaBlackjackTest` complementa a proteção: o cliente só pode alcançar a API,
o próprio pacote e exatamente `engine.MotorDePartida`.

## Executar

Requisito: JDK 26.

```bash
./mvnw test
```

Para validar também a documentação da API:

```bash
./mvnw javadoc:javadoc
```

Para jogar Blackjack pelo terminal:

```bash
./mvnw compile && java -cp target/classes br.edu.uepb.map.blackjack.AplicacaoBlackjack
```

## Documentação

- [Manual do cliente](docs/manual-do-cliente.md): guia de uso da API e de criação de
  jogos clientes;
- `docs/ARQUITETURA_FRAMEWORK_MAP.md`: mapa canônico e status da baseline;
- `docs/especificacao_arquitetural.md`: contratos e invariantes;
- `docs/modelo-conceitual-framework.md`: vocabulário independente de jogos;
- `docs/padroes-de-projeto.md`: padrões, SOLID e GRASP;
- `docs/divisao-responsabilidades.md`: fronteiras entre as trilhas;
- `docs/diagrama-classes.puml`: diagrama de classes em PlantUML;
- `docs/DiagramaDeClasses.drawio.xml`: o mesmo diagrama em draw.io;
- `docs/trilha-a.md`: decisões, evidências e limitações do motor;
- `docs/trilha-c.md`: decisões de jogadores e estratégias de decisão;
- [`docs/roteiro-apresentacao-gof-solid-grasp.md`](docs/roteiro-apresentacao-gof-solid-grasp.md):
  divisão da apresentação, GoF, SOLID, GRASP e pontos de extensão nos dois clientes;
- [`docs/regras-blackjack-basico.md`](docs/regras-blackjack-basico.md): regras,
  arquitetura do cliente e roteiro de execução do Blackjack.

Trinca e Blackjack são clientes de validação, não fontes de regras codificadas no
framework. Um mecanismo só entra no núcleo quando for reutilizável entre jogos com
mecânicas realmente diferentes. Esta branch comprova essa separação: todo código do
jogo está em `br.edu.uepb.map.blackjack`.

## Licença

Este projeto é distribuído sob a [Licença MIT](LICENSE).
