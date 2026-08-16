# Padrões de projeto, SOLID e GRASP

**Baseline analisada:** `main`, 16/08/2026.

**Regra deste catálogo:** “implementado” exige participantes, colaboração em runtime e
testes; uma interface vazia não prova um padrão.

## 1. Resumo

| Padrão | Participantes atuais | Estado |
|---|---|---|
| Template Method | `MotorDePartida.executar()` + hooks | implementado |
| Factory Method | `BaralhoFactory.criar()` | implementado |
| Strategy | distribuição e decisão | implementado |
| Strategy | validação, vitória e pontuação | implementado |
| Observer | `PartidaListener` + `api.evento` | implementado |
| Decorator | composição de validações | condicional, não implementado |
| Builder | `PartidaConfig.Builder<C>` | implementado como apoio |

O requisito de quatro padrões GoF estudados em sala está cumprido: Template Method,
Factory Method, Strategy e Observer. Builder não entra nessa contagem, por não fazer
parte do conjunto cobrado pela disciplina.

## 2. Template Method

### Intenção

Definir o esqueleto de um algoritmo e permitir que subclasses variem passos sem mudar
a ordem global.

### Participantes

| Papel GoF | Tipo |
|---|---|
| AbstractClass | `engine.MotorDePartida<C>` |
| template method | `executar()` (`public final`) |
| primitive operation | `executarTurno` — a única obrigatória |
| hooks | `preparar`, `aposDistribuir`, `aoEncerrar` |
| ConcreteClass | futuros motores de Trinca e Blackjack |

### Evidência

O motor fixa:

```text
validar estado → preparar → distribuir → executar turnos
→ avaliar → pontuar → finalizar
```

Subclasses não podem sobrescrever `executar()`. Os testes verificam a ordem, o estado
final, a impossibilidade de segunda execução e a aplicação de diretivas.

### Consequência

Há acoplamento por herança, inerente ao padrão. A superfície protegida é pequena e não
há atributos protegidos; o estado entra por contexto.

## 3. Factory Method

### Intenção

Adiar a decisão sobre o objeto criado para um ponto de extensão.

### Participantes

| Papel | Tipo |
|---|---|
| creator contract | `BaralhoFactory<C>` |
| factory method | `criar()` |
| product | `Baralho<C>` |
| concrete product reutilizável | `BaralhoPadrao<C>` |
| client | `MotorDePartida<C>` |

O engine precisa de um baralho, mas não conhece carta francesa, coringa ou quantidade.
Cada fábrica entrega uma composição nova e independente.

Não se trata de Abstract Factory: existe uma família de produto principal, o baralho.

## 4. Strategy

### 4.1 Distribuição

| Papel | Tipo |
|---|---|
| Strategy | `EstrategiaDeDistribuicao<C>` |
| Context port | `ContextoDeDistribuicao<C>` |
| ConcreteStrategy | `DistribuicaoAlternada<C>` |

A estratégia decide quantidade e ordem sem receber baralho/mãos mutáveis. Trocar o
algoritmo não altera o motor.

### 4.2 Decisão

| Papel | Tipo |
|---|---|
| Strategy | `EstrategiaDeDecisao` |
| Context | `ContextoDeDecisao` |
| ConcreteStrategies | humana, aleatória e gulosa |
| cliente da Strategy | `JogadorPadrao` por composição |

Humano, bot e dealer podem compartilhar identidade e variar comportamento.

### 4.3 Regras do jogo

| Papel | Tipos |
|---|---|
| Strategy | `RegraDeValidacaoStrategy<C>`, `RegraDeVitoriaStrategy<C>`, `RegraDePontuacaoStrategy<C>` |
| Context port | `ContextoDeValidacao<C>` e `VisaoDaPartida<C>` |
| cliente da Strategy | `MotorDePartida<C>`, via `PartidaConfig<C>` |

As três são obrigatórias na configuração e recebem apenas leitura. Antes elas eram
hooks protegidos, o que obrigava a criar subclasse para trocar a condição de vitória;
como Strategy, a troca é de objeto. O motor continua validando o retorno: desfecho não
nulo, vencedor participante e placar cobrindo exatamente os participantes.

## 5. Observer

### Intenção

Notificar console, interface gráfica ou telemetria sem acoplá-los ao motor.

| Papel | Tipo |
|---|---|
| Subject | `MotorDePartida<C>` |
| Observer | `PartidaListener` |
| Notification | `EventoDePartida` e os seis records de `api.evento` |

### Decisões tomadas na implementação

| Questão | Decisão |
|---|---|
| duplicidade | por identidade (`==`), não por `equals` |
| ordem | cadastro, documentada no contrato |
| iteração | sobre `List.copyOf(listeners)` |
| falha de um ouvinte | isolada em `try/catch` de `RuntimeException` |
| conteúdo dos eventos | records imutáveis, sem mão de jogador nem ordem do baralho |

A cópia da lista permite que um ouvinte se descadastre dentro do próprio callback sem
provocar `ConcurrentModificationException`. O isolamento impede que um console com
defeito derrube a partida. Há teste para os dois casos.

## 6. Decorator — uso condicional

Decorator só será adotado se validações independentes precisarem ser combinadas em
ordens diferentes entre jogos. Criar wrappers para uma única validação seria
overengineering.

Critério objetivo:

- pelo menos três validações reutilizáveis;
- combinações distintas entre Trinca e Blackjack;
- cada wrapper mantém o mesmo contrato de validação;
- teste comprova composição sem editar as validações existentes.

Sem esse cenário, adicionar Decorator seria engenharia excessiva — e o quarto padrão
GoF já está garantido pelo Observer.

## 7. Builder como apoio

`PartidaConfig.Builder<C>` melhora legibilidade e centraliza validações de um objeto
com vários colaboradores. Ele resolve construção, não comportamento do jogo.

Evidências:

- configuração imutável;
- lista copiada defensivamente;
- obrigatoriedade de fábrica e distribuição;
- validação de participantes e índice inicial;
- tipo genérico preservado do builder ao motor.

## 8. Padrões avaliados e rejeitados

| Padrão | Decisão |
|---|---|
| State | rejeitado: quatro estados sem comportamento polimórfico próprio |
| Singleton | rejeitado: impediria partidas independentes e dificultaria testes |
| Abstract Factory | rejeitado: a criação atual possui um produto principal |
| Iterator | desnecessário: snapshots de `List` atendem à navegação |
| Chain of Responsibility | só considerar se validações precisarem parar na primeira falha |

Registrar rejeições evita que a equipe adicione padrões apenas para aumentar a
contagem.

## 9. SOLID

### SRP

- `MotorDePartida`: orquestra;
- `GerenciadorDeTurnos`: calcula a vez;
- `CicloDeVidaDaPartida`: mantém o estado;
- `PartidaEmExecucao`: guarda o agregado e aplica operações controladas;
- `ResultadoDePartida`: representa valor final.

### OCP

Novas cartas, fábricas, distribuições, decisões, motivos e subclasses de motor são
adicionadas sem condicionais por tipo de jogo.

### LSP

Uma subclasse preserva o fluxo porque `executar()` é final. Ela pode variar apenas os
hooks documentados e deve devolver valores que respeitem as invariantes.

### ISP

`ContextoDeDistribuicao` oferece três operações. `VisaoDaPartida` separa leitura de
`ContextoDePartida`, que permite mutações limitadas. A Strategy não recebe o objeto
interno completo.

### DIP

O engine depende de `Baralho`, `BaralhoFactory`, `EstrategiaDeDistribuicao`,
`ContextoDePartida` e valores em `api`, não de implementações de Trinca/Blackjack.

## 10. GRASP

| Princípio | Evidência |
|---|---|
| Controlador | `MotorDePartida` coordena o caso de uso executar partida |
| Especialista | estado conhece transições; turnos conhecem rotação |
| Creator | motor cria o agregado transitório da execução |
| Alta Coesão | fluxo, cartas, decisão, estado e turnos estão separados |
| Baixo Acoplamento | clientes veem interfaces; internals ficam sem `public` |
| Polimorfismo | cartas, fábrica, distribuição, decisão, motivo e motor variam por tipo |
| Indireção | contextos mediam distribuição e acesso ao estado |
| Variações Protegidas | tipos de carta e algoritmos variáveis ficam atrás de interfaces |

## 11. Mapa para a defesa

| Pergunta | Evidência curta |
|---|---|
| Onde está a IoC? | o cliente chama `executar()` uma vez e o motor chama seus hooks |
| Por que turnos não são públicos? | o cliente escolhe a diretiva, mas o engine aplica a ordem |
| Como muda o baralho? | nova `BaralhoFactory<C>` |
| Como muda a distribuição? | nova `EstrategiaDeDistribuicao<C>` |
| Como troca humano por bot? | nova `EstrategiaDeDecisao` |
| Observer já existe? | sim; o motor publica seis eventos e isola falhas de ouvintes |
| Por que não State? | estados só restringem transições, sem comportamento próprio |

## 12. Evidência automatizada

A baseline executa **133 testes**. Há testes para ciclo, turnos, Template Method,
invariantes, encapsulamento, Factory, distribuição, decisão, as três Strategies de
regra e a publicação de eventos. Faltam apenas os testes de aceitação dos dois clientes
concretos, que ainda não existem na `main`.
