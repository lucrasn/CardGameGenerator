# CardGame Framework

Mini framework orientado a objetos para construir jogos de cartas em Java. O projeto
é desenvolvido para a disciplina de Métodos Avançados de Programação (MAP) da UEPB e
usa padrões GoF, SOLID e GRASP como decisões verificáveis no código.

## Estado atual

A `main` contém uma baseline integrada das Trilhas A, B e C:

- `cardgame.api`: contratos públicos de cartas, jogadores, configuração, contexto e
  resultado;
- `cardgame.engine.MotorDePartida<C>`: Template Method público do ciclo de vida;
- colaboradores internos de estado e turnos sem `public` no pacote `engine`;
- baralho e distribuição genéricos da Trilha B;
- jogadores e estratégias de decisão da Trilha C;
- exceções de domínio da Trilha D.

As interfaces `RegraDeValidacaoStrategy`, `RegraDeVitoriaStrategy`,
`RegraDePontuacaoStrategy` e `PartidaListener` ainda são placeholders vazios da
Trilha D. O motor não inventa métodos para esses contratos: até que sejam definidos,
avaliação e pontuação entram por hooks protegidos. Observer e eventos permanecem
pendentes.

A suíte atual executa 105 testes com sucesso.

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

O pacote legado `core` ainda contém artefatos de outras trilhas. A Trilha A não
depende dele e removeu suas versões antigas de estado, motor, turnos e resultado.

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
- `docs/diagrama-classes.puml`: diagrama do estado atual, incluindo pendências.
- `docs/relatorio-tecnico-trilha-a.md`: decisões, evidências e limitações do motor.

Trinca e Blackjack serão clientes de validação, não fontes de regras codificadas no
framework. Um mecanismo só entra no núcleo quando for reutilizável entre jogos com
mecânicas realmente diferentes.

## Licença

Este projeto é distribuído sob a [Licença MIT](LICENSE).
