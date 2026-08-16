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
observadores e publica os seis eventos de `cardgame.api.evento`.

Falta a camada de aplicação: Trinca e Blackjack ainda não existem na `main`.

A suíte atual executa 136 testes com sucesso.

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

São dois pacotes de produção, e a fronteira entre eles é garantida pelo compilador:
uma única classe pública em `engine` significa que nenhum jogo cliente consegue
alcançar o gerenciador de turnos ou forçar uma transição de estado.

`FronteirasArquiteturaisTest` usa ArchUnit para analisar o bytecode de produção. O
build falha se `api` depender de `engine` ou de clientes, se `engine` depender de um
jogo concreto, ou se outro tipo público aparecer em `engine`.

## Executar

Requisito: JDK 26.

```bash
./mvnw test
```

Para validar também a documentação da API:

```bash
./mvnw javadoc:javadoc
```

## Documentação

- `docs/ARQUITETURA_FRAMEWORK_MAP.md`: mapa canônico e status da baseline;
- `docs/especificacao_arquitetural.md`: contratos e invariantes;
- `docs/modelo-conceitual-framework.md`: vocabulário independente de jogos;
- `docs/padroes-de-projeto.md`: padrões, SOLID e GRASP;
- `docs/divisao-responsabilidades.md`: fronteiras entre as trilhas;
- `docs/diagrama-classes.puml`: diagrama de classes em PlantUML;
- `docs/DiagramaDeClasses.drawio.xml`: o mesmo diagrama em draw.io;
- `docs/trilha-a.md`: decisões, evidências e limitações do motor;
- `docs/trilha-c.md`: decisões de jogadores e estratégias de decisão.

Trinca e Blackjack serão clientes de validação, não fontes de regras codificadas no
framework. Um mecanismo só entra no núcleo quando for reutilizável entre jogos com
mecânicas realmente diferentes.

## Licença

Este projeto é distribuído sob a [Licença MIT](LICENSE).
