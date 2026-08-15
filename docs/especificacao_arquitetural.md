# Especificação arquitetural - CardGame Framework

**Status:** rascunho estrutural; API candidata a congelamento.
**Clientes de validação:** Trinca (principal) e Blackjack (extensibilidade).

## 1. Fontes de verdade

Em caso de divergência, usar esta ordem:

1. enunciado `Projeto Final (MAP) - 2026.1.pdf`;
2. `docs/requisitos-api-trinca.md` para contratos e fronteira pública;
3. `docs/padroes-de-projeto.md` para padrões, SOLID, GRASP e UML;
4. `docs/regras-trinca.md` para comportamento da aplicação Trinca;
5. `docs/regras-blackjack.md` para a regra de mesa do segundo cliente;
6. `docs/divisao-responsabilidades.md` para donos e cronograma.

## 2. Visão geral

O produto é um mini framework orientado a objetos que controla o ciclo de vida de
uma partida e reutiliza baralho, mão, jogadores, turnos, eventos e tratamento de
erros. Jogos concretos implementam hot-spots públicos sem alterar `core`.

```text
trinca ───────┐
              ├── depende de ──> cardgame.api <── implementado por ── cardgame.core
blackjack ────┘

cardgame.core não depende de trinca nem de blackjack.
```

`MotorDePartida.executar()` é o frozen-spot principal. A subclasse concreta fornece
o turno específico do jogo; regras, distribuição e decisões são injetadas por
Strategy; baralhos são criados por Factory Method; eventos usam Observer.

## 3. Fronteira pública

A relação normativa de tipos, assinaturas, responsabilidades, contextos controlados
e testes de contrato está em [requisitos-api-trinca.md](requisitos-api-trinca.md).

Decisões centrais:

- `MotorDePartida`, `EstadoPartida` e `ResultadoDePartida` são públicos em `api`;
- `GerenciadorDeTurnos`, mesa e estado mutável permanecem em `core`;
- `BaralhoPadrao`, `MaoDeCartasPadrao` e `JogadorPadrao` são componentes públicos
  reutilizáveis, evitando duplicação nos jogos clientes;
- clientes acessam estado apenas por contextos públicos, com coleções imutáveis ou
  operações controladas;
- `Jogada`, `EtapaDeTurno` e `EventoDePartida` são abertas para novos jogos;
- Trinca e Blackjack não podem importar `cardgame.core`.

## 4. Padrões e princípios

Os quatro padrões obrigatórios são Template Method, Strategy, Factory Method e
Observer. Decorator é condicional à existência de combinações reais de validações.
Builder é usado como apoio na construção de `PartidaConfig`, mas não entra na contagem
dos padrões estudados.

Justificativas completas, alternativas rejeitadas, GRASP e SOLID estão em
[padroes-de-projeto.md](padroes-de-projeto.md).

## 5. Componentes reutilizáveis e hot-spots

**Frozen-spots:** fluxo final de `MotorDePartida.executar()`, infraestrutura de
contextos, `BaralhoPadrao`, `MaoDeCartasPadrao`, `JogadorPadrao`, turnos, notificação
e exceções.

**Hot-spots:** cartas, fábrica de baralho, distribuição, decisão, ações/fases,
validação, vitória, pontuação, eventos/listeners e execução do turno concreto.

## 6. Rastreabilidade do enunciado

| Requisito | Evidência planejada | Estado atual |
|---|---|---|
| API pública definida | catálogo e assinaturas em `requisitos-api-trinca.md` | candidata a aprovação |
| Pelo menos 5 pontos de extensão | 10 hot-spots catalogados | definido em documento |
| Separação framework/clientes | pacotes separados + teste de imports | definido; teste pendente |
| Aplicação cliente | Trinca | regras prontas; código pendente |
| Interfaces e classes abstratas | contratos Strategy/Observer/Factory + motor abstrato | esqueletos parciais |
| Exceções | hierarquia `PartidaException` | definida; código pendente |
| Encapsulamento | cópias defensivas e contextos controlados | parcial no core |
| Testes automatizados | JUnit 5 por dono + integração de E | testes de A existentes |
| Javadoc público | responsabilidade do dono de cada tipo | parcial |
| Diagrama de classes | UML com relações e multiplicidades | pendente após API |
| Exemplos de uso | montagem da Trinca e do Blackjack | pendente |
| Decisões justificadas | este documento + catálogo de padrões | em elaboração |

## 7. UML obrigatório

O diagrama final deve mostrar classes, interfaces, herança, realização, associação,
composição/agregação e multiplicidades. Deve separar visualmente `api`, `core`,
`trinca` e `blackjack`, com dependências dos jogos apontando para a API e nenhuma
dependência inversa.

O UML só deve ser fechado depois da aprovação do checklist da API, para não registrar
assinaturas que mudem durante a implementação.

## 8. Pendências bloqueantes

1. aprovação conjunta da API por A-D;
2. migração dos tipos públicos já implementados em `core`;
3. implementação de contextos, eventos e exceções;
4. aprovação da regra de mesa proposta em `regras-blackjack.md`;
5. stubs de Trinca e Blackjack compilando somente contra `api`;
6. decisão operacional entre instalar JDK 26 em todas as máquinas ou fixar Java 21;
7. UML, relatório final de até 8 páginas e roteiro da defesa.
