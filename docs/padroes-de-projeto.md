# Padrões de projeto, SOLID e GRASP — CardGame Framework

**Disciplina:** Métodos Avançados de Programação — UEPB

**Status:** catálogo correspondente à baseline implementada na branch local
`trilha/a-motor`; publicação do código na `main` pendente

**Padrões GoF contabilizados:** Template Method, Strategy, Factory Method e Observer

## 1. Regra de escopo

O enunciado exige pelo menos quatro padrões GoF estudados na disciplina e alerta para
overengineering. O material da equipe registra Observer, Strategy, Decorator, Template
Method e Factory Method.

A baseline usa quatro deles. Decorator foi analisado, mas não foi implementado sem uma
necessidade concreta. Builder auxilia a configuração, porém não entra na contagem dos
GoF da disciplina.

| Padrão | Evidência | Estado |
|---|---|---|
| Template Method | `engine.MotorDePartida` | implementado |
| Strategy | distribuição, decisão e três regras | implementado |
| Factory Method | `BaralhoFactory.criarBaralho()` | implementado |
| Observer | `PartidaListener` e eventos | implementado |
| Decorator | composição de validações | analisado e adiado |

## 2. Template Method — `MotorDePartida`

### 2.1 Intenção

Reutilizar a estrutura de um algoritmo, permitindo que subclasses redefinam etapas
específicas sem alterar sua sequência.

### 2.2 Participantes

| Papel GoF | Elemento do projeto |
|---|---|
| AbstractClass | `engine.MotorDePartida` |
| Template Method | `executar()`, público e `final` |
| Primitive Operation | `executarTurno(ContextoDePartida)` |
| Hooks | `preparar`, `aposDistribuir`, `aoEncerrar` |
| ConcreteClass | motores concretos de Trinca e Blackjack |

### 2.3 Problema resolvido

Jogos compartilham criar/embaralhar baralho, distribuir, iniciar, repetir jogada
inválida, avaliar vitória, aplicar a diretiva do turno, pontuar e finalizar. Sem o
padrão, cada cliente copiaria o laço e poderia esquecer estados, eventos ou invariantes.

O método `executar()` final é o frozen-spot. A subclasse fornece a mecânica do turno,
mas não avança o gerenciador nem encerra a partida por conta própria.

### 2.4 Inversão de Controle

O cliente não chama uma sequência de serviços para fabricar a partida. Ele configura e
estende o motor; depois, o framework chama seus hooks. Essa inversão distingue um
framework de uma coleção de utilitários.

### 2.5 Trade-off

Template Method cria acoplamento por herança. A baseline limita esse custo:

- uma única operação abstrata obrigatória;
- três hooks opcionais pequenos;
- nenhum atributo protegido;
- estado acessível apenas por interfaces públicas controladas.

Usar Strategy para o fluxo inteiro foi rejeitado: a sequência não deve variar em tempo
de execução; ela é justamente a invariável que o framework protege.

## 3. Strategy — comportamentos substituíveis

### 3.1 Intenção

Definir uma família de algoritmos, encapsular cada um e torná-los intercambiáveis por
composição.

### 3.2 Strategies implementadas

| Variação | Abstração | Contexto |
|---|---|---|
| distribuição | `EstrategiaDeDistribuicao` | `ContextoDeDistribuicao` |
| decisão | `EstrategiaDeDecisao` | `ContextoDeDecisao` |
| validação | `RegraDeValidacaoStrategy` | `ContextoDeValidacao` |
| vitória | `RegraDeVitoriaStrategy` | `VisaoDaPartida` |
| pontuação | `RegraDePontuacaoStrategy` | visão + desfecho |

Implementações reutilizáveis de decisão já existentes:

- `DecisaoAleatoria`;
- `DecisaoGulosa`;
- `DecisaoHumanaConsole`.

Distribuição e regras concretas dos clientes podem ser classes ou lambdas, pois os
contratos são interfaces funcionais.

### 3.3 Problema resolvido

O motor não contém condicionais como `if (jogo == TRINCA)`. Cada configuração injeta
algoritmos próprios. Novo jogo adiciona implementações e não altera o engine, atendendo
OCP e DIP.

Validação, vitória e pontuação são interfaces separadas. Uma interface única
`RegraDoJogo` obrigaria clientes a depender de métodos que não usam e violaria ISP.

### 3.4 Decisão por composição

`JogadorPadrao` pode receber uma `EstrategiaDeDecisao` e trocá-la mantendo o mesmo
UUID. Humano, bot e dealer são comportamentos, não subclasses obrigatórias.

`ContextoDeDecisao` oferece etapa e ações permitidas. Jogos podem acrescentar dados
públicos por subtipo sem revelar internals.

### 3.5 Alternativas rejeitadas

- `enum TipoDeJogo` com `switch`: fecha a extensão e viola OCP;
- subclasses `JogadorHumano`/`JogadorBot`: combina identidade e decisão por herança;
- uma Strategy única para todas as regras: baixa coesão e interface larga.

## 4. Factory Method — criação do baralho

### 4.1 Intenção

Definir um método de criação contra uma abstração e deixar a implementação concreta
decidir qual produto fornecer.

### 4.2 Participantes

| Papel GoF | Elemento do projeto |
|---|---|
| Product | `Baralho` |
| ConcreteProduct reutilizável | `BaralhoPadrao` |
| Creator contract / factory method | `BaralhoFactory.criarBaralho()` |
| ConcreteCreator | fábrica fornecida pelo jogo cliente |

Em Java, o papel de ConcreteCreator pode ser uma classe nomeada ou uma implementação da
interface funcional. Nos clientes completos, classes nomeadas são preferíveis para que
o mapeamento do padrão fique explícito no UML e na defesa.

### 4.3 Problema resolvido

O engine precisa iniciar com um baralho, mas não pode conhecer quantidade, tipo das
cartas ou implementação concreta. A Trinca fornece 104 cartas; o Blackjack, 52. Ambos
podem devolver `BaralhoPadrao`, mas com composições diferentes.

Instanciar `new BaralhoPadrao(...)` dentro do motor o acoplaria à composição de um
cliente. Fazer cada jogo reimplementar compra e embaralhamento duplicaria código.

### 4.4 Delimitação acadêmica

Não é Abstract Factory: há um produto principal (`Baralho`), e não uma família de
produtos relacionados. Também não se deve chamar qualquer construtor auxiliar de
Factory Method; a evidência é o método público de criação usado pelo engine.

## 5. Observer — eventos da partida

### 5.1 Intenção

Permitir que interessados sejam avisados de fatos sem que o objeto observado conheça
suas implementações concretas.

### 5.2 Participantes

| Papel GoF | Elemento do projeto |
|---|---|
| Subject | `MotorDePartida` |
| Observer | `PartidaListener` |
| Notification | `EventoDePartida` |
| Concrete notifications | eventos em `api.evento` e eventos dos clientes |

O motor agrega zero ou mais listeners, oferece adicionar/remover e publica eventos a
partir de uma cópia da lista. Um listener defeituoso é isolado e registrado; os demais
continuam recebendo notificações.

### 5.3 Problema resolvido

Sem Observer, console, log ou placar precisariam ser chamados diretamente pelo motor.
Isso aumentaria acoplamento, misturaria domínio e apresentação e dificultaria testes.

O cliente pode trocar console por GUI ou acrescentar telemetria sem editar o engine.

### 5.4 Relação com MVC

Observer ajuda a separar Model e View, mas a baseline não declara um MVC completo: não
existe ainda uma aplicação final com Controller/View definidos. MVC pode ser discutido
no relatório somente depois dos clientes completos, sem contar como GoF adicional.

## 6. Decorator — decisão de não usar ainda

Decorator seria apropriado se validações independentes precisassem ser empilhadas em
combinações diferentes, mantendo a mesma interface e delegando recursivamente.

A baseline não possui decoradores concretos. Strategy simples já atende os stubs. O
padrão só deve ser acrescentado se os jogos completos demonstrarem pelo menos três
validações combináveis e se a composição reduzir duplicação real.

Essa ausência é deliberada: declarar Decorator sem classes que realizem a estrutura do
padrão seria documentação falsa; criá-las sem uso seria overengineering.

## 7. Builder auxiliar — `PartidaConfig.Builder`

Configuração possui vários colaboradores opcionais e obrigatórios. O Builder evita um
construtor longo e posicional, valida invariantes no `build()` e produz objeto
imutável.

Builder não integra a contagem mínima porque não está entre os GoF selecionados do
material da disciplina para este trabalho.

## 8. Padrões considerados e rejeitados

| Padrão | Decisão |
|---|---|
| State | quatro estados com tabela de transição não justificam uma classe por estado |
| Chain of Responsibility | validações atuais não formam cadeia de tratadores |
| Iterator | coleções Java e visões imutáveis já resolvem a iteração |
| Adapter | `ControleEntradaSaida` implementa uma porta própria; não adapta API incompatível |
| Singleton | estado global prejudicaria isolamento dos testes |
| Abstract Factory | não há família de produtos |
| Null Object | a estratégia ausente falha explicitamente; não simula uma decisão silenciosa |

## 9. SOLID

### SRP — Single Responsibility Principle

- `GerenciadorDeTurnos`: ordem e rotação;
- `CicloDeVidaDaPartida`: estado corrente;
- `EstadoPartida`: transições permitidas;
- `BaralhoPadrao`: coleção do baralho;
- cada Strategy: um algoritmo variável;
- `MotorDePartida`: orquestração do fluxo.

### OCP — Open/Closed Principle

Novas cartas, baralhos, decisões, distribuições, regras, motivos, eventos e motores
concretos são adicionados por implementação/extensão. O engine não recebe condicionais
por nome de jogo.

### LSP — Liskov Substitution Principle

Subclasses de `MotorDePartida` não podem alterar o algoritmo público porque
`executar()` é final. Elas cumprem o contrato devolvendo uma diretiva válida e usando
somente o contexto protegido.

### ISP — Interface Segregation Principle

Visão, distribuição, decisão, validação, vitória e pontuação possuem contratos
distintos. Um cliente recebe apenas as capacidades necessárias naquela extensão.

### DIP — Dependency Inversion Principle

O engine depende de abstrações em `api`: `BaralhoFactory`, Strategies, listener e
contextos. Nenhuma classe de produção importa pacotes de Trinca ou Blackjack.

## 10. GRASP

### Alta Coesão

Responsabilidades mutáveis foram separadas do modelo público. Contextos agrupam apenas
operações relacionadas à fase em que são usados.

### Baixo Acoplamento

Clientes conhecem contratos e `engine.MotorDePartida`; internals são package-private.
Eventos evitam dependência do motor em apresentação.

### Especialista na Informação

| Informação | Especialista |
|---|---|
| cartas no baralho | `BaralhoPadrao` |
| transições legais | `EstadoPartida` |
| índice, sentido e pulos | `GerenciadorDeTurnos` |
| localização de cartas e mãos | `PartidaEmExecucao` |
| invariantes do resultado | `DesfechoDePartida` / `ResultadoDePartida` |
| algoritmo concreto de vitória | Strategy do jogo |

### Controlador

`MotorDePartida` recebe o evento de sistema “executar partida” e coordena os
colaboradores sem absorver suas regras internas.

### Creator

- `PartidaConfig.Builder` cria a configuração que conhece;
- `MotorDePartida` cria o agregado transitório da execução;
- `BaralhoFactory` cria o produto solicitado pelo motor.

## 11. Herança e composição

Herança é usada onde há um algoritmo estável com etapa variável:

```text
MotorDeTrinca ──|> MotorDePartida
MotorDeBlackjack ──|> MotorDePartida
```

Composição é usada para algoritmos substituíveis:

```text
PartidaConfig → distribuição / validação / vitória / pontuação
JogadorPadrao → estratégia de decisão opcional
MotorDePartida → listeners
```

Essa divisão evita tanto uma hierarquia de classes por combinação de regras quanto um
motor cheio de condicionais.

## 12. Pontos de extensão

| # | Hot-spot | Como o cliente estende |
|---|---|---|
| 1 | `Carta` | declara um tipo imutável |
| 2 | `BaralhoFactory` | fornece a composição |
| 3 | `EstrategiaDeDistribuicao` | define a distribuição inicial |
| 4 | `EstrategiaDeDecisao` | cria humano, bot ou dealer |
| 5 | `Jogada` / `EtapaDeTurno` | define ações e fases |
| 6 | `RegraDeValidacaoStrategy` | valida ações específicas |
| 7 | `RegraDeVitoriaStrategy` | reconhece encerramento |
| 8 | `RegraDePontuacaoStrategy` | calcula placar |
| 9 | eventos/listener | declara fatos e reações |
| 10 | `executarTurno` | implementa a mecânica concreta |

## 13. Evidências mecânicas

```bash
./mvnw clean verify
./mvnw javadoc:javadoc
```

A baseline integrada executa 103 testes. `ClientesStubTest` verifica a direção das
dependências. Testes específicos cobrem encapsulamento, transições, turnos, resultados,
regras, decisões, I/O e falha de observador.

## 14. Elementos obrigatórios no UML

- `MotorDePartida` abstrato e herança dos clientes;
- `executar()` final, operação primitiva e três hooks;
- realização das interfaces Strategy;
- fábrica criando `Baralho`;
- agregação de listeners;
- composição do estado interno com baralho e mãos;
- multiplicidades de jogadores, cartas, mãos e listeners;
- separação visual entre `api`, `engine` e clientes;
- estereótipos dos quatro padrões implementados.

## 15. Roteiro curto para a defesa

| Pergunta | Resposta central |
|---|---|
| Por que é framework? | Inversão de Controle no Template Method final |
| Como entra um jogo novo? | implementa os hot-spots sem editar o engine |
| Por que não uma classe de regra única? | ISP e combinações independentes |
| Por que jogador não tem subclasses humano/bot? | Strategy por composição |
| Como o console não contamina o motor? | porta de I/O + Observer |
| Onde está o baixo acoplamento? | direção `cliente → engine → api` e internals fechados |
| Por que não Decorator? | necessidade combinatória ainda não comprovada |
| Isso é Abstract Factory? | não; há um único produto variável, `Baralho` |

---

Referências locais: enunciado em `docs/proposta/AtividadeProposta.pdf` e materiais das
aulas de Observer, Strategy, Decorator, Template Method, Factory Method, SOLID e GRASP.
