# Levantamento de Padrões de Projeto — CardGame Framework

**Disciplina:** Métodos Avançados de Programação (CPT01091) — UEPB
**Documento-base para:** relatório (seções "padrões utilizados", "GRASP e SOLID",
"pontos de extensão") e defesa oral.
**Fonte única de verdade sobre padrões neste repositório.** Se `.claudecode.md` ou
`docs/divisao-responsabilidades.md` divergirem daqui, este documento prevalece.

---

## 0. Regra de escopo — leia antes de propor qualquer padrão

O enunciado (p. 2) diz:

> *"A solução deverá utilizar pelo menos **quatro padrões GoF dentre os estudados na
> disciplina**. Além desses, a equipe poderá usar outros padrões não apresentados,
> sempre tomando cuidado com engenharia excessiva (que conhecemos nas aulas como
> overengineering)."*

Isso tem uma consequência dura: **os quatro padrões que contam para o mínimo têm que
sair da lista do que a professora deu em aula.** Padrão de fora só entra como *extra*,
e ainda assim sob risco de ser lido como overengineering.

### Padrões GoF efetivamente estudados (`MATERIAL_DA_AULA/`)

| Aula | Arquivo | Padrão |
|---|---|---|
| 03.1 | `03.1_GoFObserver.pdf` (23 p.) | **Observer** |
| 04.1 | `04.1_GoFStrategy.pdf` (37 p.) | **Strategy** |
| 05.1 | `05.1_GofDecorator.pdf` (38 p.) | **Decorator** |
| 06.2 | `06.2_GofTemplateMethod.pdf` (32 p.) | **Template Method** |
| 07.1 | `07.1_GofFactoryMethod.pdf` (30 p.) | **Factory Method** |

Complementares (não são GoF, mas são cobrados no relatório):

| Aula | Arquivo | Conteúdo |
|---|---|---|
| 02.1 | `02.1_MVC.pdf` | MVC como **composição** de padrões |
| 08.2 / 09.2 | `PrincipiosSOLIDSRPI.pdf` / `SRPIi.pdf` | SOLID (SRP, OCP, LSP, ISP, DIP) |
| 10.2 | `10.2_GRASPCoesão.pdf` (36 p.) | GRASP — Alta Coesão |
| 11.2 | `11.2_GRASPAcoplamento.pdf` (18 p.) | GRASP — Baixo Acoplamento |
| 12.2 | `12.2_GRASPExpert.pdf` (24 p.) | GRASP — Especialista na Informação |

**O universo estudado tem exatamente 5 padrões GoF. A proposta abaixo usa os 5** —
4 como obrigatórios e 1 (Decorator) como condicional, com critério explícito de
descarte na seção 3.

> ⚠️ **Correção em relação aos documentos anteriores.** `docs/divisao-responsabilidades.md`
> mencionava State, Chain of Responsibility, Iterator e Adapter como "opcionais".
> Nenhum deles foi dado em aula. Continuam permitidos como extras pelo enunciado, mas
> **não contam para o mínimo de quatro** e passaram para a tabela de rejeitados
> (seção 4), que é uma seção que joga a favor da nota.

---

## 1. Visão geral — o mapa fechado

| # | Padrão | Onde na arquitetura | Trilha | Status |
|---|---|---|---|---|
| 1 | **Template Method** | `MotorDePartida` | A — Lucas | obrigatório |
| 2 | **Strategy** | `Regra*Strategy` + estratégia de decisão do jogador | C — Allan / D — Lívia | obrigatório |
| 3 | **Factory Method** | `BaralhoFactory` | B — Júlio | obrigatório |
| 4 | **Observer** | `PartidaListener` | D — Lívia | obrigatório |
| 5 | **Decorator** | composição de `RegraDeValidacaoStrategy` | D — Lívia | condicional |

**Mínimo do enunciado: 4. Entregamos 4 garantidos + 1 condicional.** Nenhum padrão
fora desta tabela entra sem discussão no grupo.

---

## 2. Catálogo detalhado

Cada ficha segue a mesma estrutura, para virar seção do relatório com corte mínimo:
*intenção (na definição da aula) → participantes (na nomenclatura da aula) →
mapeamento para nossas classes → problema concreto → justificativa → alternativa
rejeitada → o que aparece no diagrama.*

---

### 2.1 Template Method — `MotorDePartida`

**Intenção (slide 06.2, "Definição oficial do GoF"):** reutilizar a estrutura de um
algoritmo, permitindo que subclasses redefinam etapas específicas sem alterar a
sequência. O slide reforça: *"a superclasse controla a sequência do algoritmo; as
subclasses implementam etapas específicas"* e *"é a superclasse que chama os métodos
da subclasse"*.

**Participantes (nomenclatura da aula 06.2):**

| Participante (slide) | Nossa classe |
|---|---|
| `AbstractTemplate` | `api.MotorDePartida` (abstrata) |
| Template Method (marcado `final`) | `MotorDePartida.executar()` |
| Operação primitiva (abstrata, obrigatória) | `executarTurno(ContextoDePartida)` |
| Hooks / métodos gancho (com implementação padrão) | `preparar(ContextoDePartida)`, `aoEncerrar(ContextoDePartida, ResultadoDePartida)` |
| `ImplementationA`, `ImplementationB` | `MotorDeTrinca`, `MotorDeBlackjack` |

**Problema concreto.** Trinca e Blackjack compartilham a mesma espinha dorsal —
preparar mesa → distribuir cartas → laço de turnos → apurar vencedor → encerrar. O que
muda é *quantas* cartas se distribui, *o que* acontece no turno e *quando* a partida
acaba. Sem o padrão, cada jogo reescreveria o laço inteiro, e a ordem dos passos
poderia divergir entre eles — exatamente o cenário `ProcessadorPDF` / `ProcessadorExcel`
do slide, em que os dois métodos `processar()` são idênticos exceto por uma linha.

**Justificativa para a defesa.** É o padrão que materializa a **Inversão de Controle**,
que é o que faz esta solução ser um *framework* e não uma *biblioteca*. O slide 06.2
diz literalmente que o padrão se aplica *"em frameworks e bibliotecas"* e que
*"o framework ou a superclasse controla o fluxo"*. Marcar `executar()` como `final` é
a garantia mecânica de que nenhum jogo cliente pode subverter a ordem da partida —
é o **frozen-spot** da arquitetura.

**Fronteira pública.** `MotorDePartida` pertence à API pública como a abstração de
extensão do framework; `MotorDeTrinca` e `MotorDeBlackjack` ficam nos pacotes dos
jogos clientes e a estendem. As operações primitivas recebem apenas um
`ContextoDePartida` público e controlado. Assim, a subclasse não importa
`BaralhoBase`, `MaoDeCartas` ou `Mesa` de `core`, preservando a separação exigida no
enunciado sem esconder o Template Method atrás de uma fachada.

**Alternativa rejeitada.** Strategy para o fluxo inteiro. Descartada porque a sequência
da partida **não** deve variar em tempo de execução nem ser substituível pelo cliente;
o slide 06.2 é explícito: use Strategy *"quando o comportamento precisa mudar em tempo
de execução"*, o que não é o caso da espinha dorsal da partida. Fixar a sequência é
requisito, não limitação.

**Desvantagem que precisamos admitir (slide 06.2).** O padrão gera *"forte dependência
de herança"* e *"alto acoplamento estrutural"*: `MotorDeTrinca` depende do contrato
protegido de `MotorDePartida`. Mitigação: há uma única operação primitiva obrigatória
e nenhum atributo protegido; todo acesso ao estado ocorre por `ContextoDePartida`.

**No diagrama:** `MotorDePartida` em *itálico* (abstrata), com `executar()` sublinhado
como `final` e as operações primitivas em itálico; `MotorDeTrinca` e `MotorDeBlackjack`
ligadas por **herança** (triângulo vazado).

---

### 2.2 Strategy — regras do jogo e decisão do jogador

**Intenção (slide 04.1, "Definição oficial do GoF"):** *"Define uma família de
algoritmos, encapsula cada um deles e os torna intercambiáveis."*

**Participantes (nomenclatura da aula 04.1):**

| Participante (slide) | Nossas classes |
|---|---|
| `Context` | `MotorDePartida` (para as regras) · `Jogador` (para a decisão) |
| `Strategy` (Estratégia Abstrata) | `RegraDeValidacaoStrategy`, `RegraDePontuacaoStrategy`, `RegraDeVitoriaStrategy`, `EstrategiaDeDecisao` |
| `ConcreteStrategy` | `ValidacaoTrinca`, `PontuacaoBlackjack`, `VitoriaPorTrincaFormada`, `DecisaoAleatoria`, `DecisaoGulosa`, `DecisaoHumanaConsole` |

**Problema concreto.** O enunciado (p. 1) exige que jogos diferentes tenham
*"diferentes regras"*, *"diferentes formas de vencer uma partida"* e *"diferentes
estratégias de tomada de decisão dos jogadores"*. A solução ingênua seria
`if (jogo == TRINCA) ... else if (jogo == BLACKJACK) ...` dentro do motor — o
anti-padrão que o slide 04.1 abre denunciando (*"Muitos if/else · Difícil manutenção ·
Toda mudança quebra algo"*).

**Justificativa para a defesa.** Duas frases do slide 04.1 são o núcleo do argumento:

- *"O segredo do Strategy é COMPOSIÇÃO e não herança. O objeto recebe um comportamento
  (ele não tem um comportamento)."* — por isso `Jogador` **tem uma** `EstrategiaDeDecisao`
  em vez de `JogadorAgressivo extends Jogador`. Um mesmo jogador pode trocar de
  estratégia em tempo de execução.
- *"A classe principal depende da abstração e não das implementações concretas."* — é a
  formulação que a aula dá para o **DIP**, e é o que permite que o `core` não conheça
  nenhuma regra concreta.

**Por que três interfaces de regra e não uma.** Separar *validar*, *pontuar* e *vencer*
é aplicação direta do **ISP** (aula 08.2/09.2): o Blackjack precisa de pontuação
sofisticada e vitória trivial; a Trinca é o inverso. Uma interface `RegraDoJogo` única
obrigaria cada jogo a implementar métodos que não usa.

**Alternativa rejeitada.** `enum` com comportamento (`TipoDeRegra.TRINCA.validar(...)`).
Funciona, mas fecha o ponto de extensão: um jogo novo exigiria **editar o enum**, dentro
do framework — violação frontal do OCP e do requisito 3 (separação framework/cliente).

**No diagrama:** `MotorDePartida` com **associação direcionada** (seta simples) para
cada interface de regra, multiplicidade `1`; as concretas ligadas por **realização**
(linha tracejada com triângulo vazado). As concretas ficam do lado do pacote do jogo,
não do `core` — isso precisa ficar visualmente evidente no diagrama.

---

### 2.3 Factory Method — `BaralhoFactory`

**Intenção (slide 07.1, "Definição oficial do GoF"):** *"Define uma interface para
criar um objeto, mas deixa as subclasses decidirem qual classe instanciar."*

**Participantes (nomenclatura da aula 07.1 — em inglês, como no slide):**

| Participante (slide) | Nossas classes |
|---|---|
| `Product` | `Baralho` |
| `ConcreteProduct` | `BaralhoPadrao` (recebe a composição de cartas definida por cada fábrica) |
| `Creator` | `BaralhoFactory` |
| `ConcreteCreator` | `BaralhoFrancesFactory` (52), `BaralhoDeTrincaFactory` |

**Problema concreto.** O motor precisa de um baralho, mas **não pode saber** qual
composição ele possui. Blackjack usa 52 cartas francesas; a Trinca usa dois baralhos
franceses. Cada fábrica cria um `BaralhoPadrao` com sua própria coleção de cartas. O slide 07.1
enuncia o problema em termos que servem literalmente ao nosso caso: *"o problema não é
criar personagens, é **decidir qual criar**. Quem deveria ter essa responsabilidade?"*

**Justificativa para a defesa.** É a resposta ao *"Provê ganchos para subclasses"* do
slide 07.1 — a criação do baralho é o primeiro hot-spot que um jogo novo precisa
preencher, e é o que permite ao `core` trabalhar apenas contra a interface `Baralho`
(*"o código só lida com a interface Produto"*, slide 07.1).

**Distinção que provavelmente será cobrada na defesa.** Não é Abstract Factory. Abstract
Factory criaria *famílias* de produtos relacionados (carta + baralho + mesa temática);
aqui há **um** produto variável, criado por **um** método fábrica. Chamar de Abstract
Factory seria overengineering nominal — e o enunciado penaliza isso.

**Alternativa rejeitada.** `new BaralhoPadrao(...)` direto no motor. Acopla o `core` à
composição de cartas de um jogo concreto e quebra o requisito 3. Também rejeitado:
fazer cada jogo reimplementar o baralho, o que duplicaria embaralhamento, compra e
encapsulamento de coleção. O `BaralhoPadrao` é componente reutilizável público; a
fábrica concreta varia somente as cartas que o compõem.

**No diagrama:** `BaralhoFactory` como interface com `criarBaralho(): Baralho`;
`MotorDePartida` associado a ela com multiplicidade `1`; `Baralho` em **composição**
(losango cheio) com `Carta`, multiplicidade `1 -- 0..*`.

---

### 2.4 Observer — `PartidaListener`

**Intenção (slide 03.1):** *"O padrão Observer permite que objetos interessados sejam
avisados da mudança de estado"* de outro objeto.

**Participantes (nomenclatura da aula 03.1 — o slide usa os termos em inglês):**

| Participante (slide) | Nossas classes |
|---|---|
| `Subject` | `MotorDePartida` |
| `Observer` | `PartidaListener` |
| Observadores concretos | `ConsoleView`, `PlacarListener`, `LogDePartidaListener` |

**Problema concreto.** O enunciado (p. 1) exige *"diferentes eventos durante a partida"*.
Sem Observer, o `MotorDePartida` chamaria `System.out.println` diretamente — e aí o
framework fica acoplado ao console, o motor vira intestável (não dá para asserir sobre
saída padrão com JUnit) e o requisito de I/O em console vira parte do `core`.

**Justificativa para a defesa.** A aula 03.1 constrói o padrão a partir do problema de
acoplamento do `GeradorDeNotaFiscal`, e a conclusão dos slides é a nossa: *"o Subject
está acoplado apenas à classe base Observer"* e *"o cliente configura o número e o tipo
de observadores"*. Traduzindo para o projeto: **o motor não sabe que existe console.**
Ele anuncia "carta jogada"; quem quiser que escute. É o que permite trocar console por
GUI sem tocar no `core` — e é o argumento mais forte de *baixo acoplamento* (GRASP,
aula 11.2) que temos.

**Ligação com MVC (aula 02.1) — vale ponto no relatório.** A aula 02.1 apresenta MVC
como *composição* de padrões, e é exatamente o que temos: o `core` é o **Model**, o
`ConsoleView` é a **View** registrada como Observer, e `MotorDePartida` acumula o papel
de **Controller**. Citar essa leitura mostra que a arquitetura não é um amontoado de
padrões isolados.

**Alternativa rejeitada.** Passar o `ConsoleView` por parâmetro ao motor. Resolve a
dependência de compilação, mas fixa **um** observador; o slide 03.1 destaca justamente
que o cliente configura *o número* de observadores.

**No diagrama:** `MotorDePartida ◇—— PartidaListener` com **agregação** (losango vazado,
porque os listeners existem independentemente do motor) e multiplicidade `1 -- 0..*`.
Métodos `adicionarListener()` / `removerListener()` visíveis no `MotorDePartida`.

---

### 2.5 Decorator — composição de regras de validação *(condicional)*

**Intenção (slide 05.1):** *"O padrão Decorator adiciona responsabilidades extras a
objetos dinamicamente"*, usando composição e evitando *"explosão de subclasses"*.

**Participantes (nomenclatura da aula 05.1 — mista, `BaseDecorator` em inglês e
`ConcreteDecorator` traduzido como "Decorador" nos exemplos):**

| Participante (slide) | Nossas classes |
|---|---|
| `Component` | `RegraDeValidacaoStrategy` |
| `ConcreteComponent` | `ValidacaoBase` (aceita qualquer jogada legal do baralho) |
| `BaseDecorator` | `ValidacaoDecorator` (guarda referência a outra `RegraDeValidacaoStrategy` e delega) |
| `ConcreteDecorator` | `NaoUltrapassarLimiteDecorator`, `CartaPertenceAMaoDecorator`, `RespeitaTurnoDecorator` |

**Problema concreto.** As validações de jogada se acumulam por combinação. O Blackjack
precisa de "a carta veio do baralho" + "o jogador não estourou 21" + "o jogador não
declarou parada". A Trinca precisa de "a carta está na mão" + regra própria de descarte.
Modelar isso por herança produz `ValidacaoComLimiteEComTurno`, `ValidacaoComLimiteSemTurno`…
— literalmente a lista `EmailComLogECriptografia` do slide 05.1, cuja legenda é:
*"quando usamos herança para adicionar funcionalidades, cada combinação vira uma classe"*.

**Justificativa para a defesa.** É o único ponto da arquitetura com **explosão
combinatória real**, que é a condição de uso do padrão. A *composição recursiva* descrita
no slide permite montar a regra de cada jogo como um encadeamento declarativo:

```java
new RespeitaTurnoDecorator(
    new CartaPertenceAMaoDecorator(
        new ValidacaoBase()));
```

**Por que é condicional — e este critério é o que nos protege de overengineering.**
O próprio slide 05.1 lista como desvantagem *"mais objetos, mais indireção e mais
estrutura complexa"*. Então a regra de decisão é objetiva:

> **Só usamos Decorator se, ao escrever as regras da Trinca e do Blackjack, houver pelo
> menos 3 validações independentes que se combinem de formas diferentes entre os dois
> jogos.** Se cada jogo tiver 1 ou 2 validações fixas, um `ConcreteStrategy` por jogo
> resolve, e o Decorator sai — sobram 4 padrões, que é o mínimo exigido.

Essa decisão só pode ser tomada **depois** que Raffael entregar o documento de regras da
Trinca (Fase 0). Levar essa condicional explicitada para o relatório é melhor do que
levar o padrão aplicado sem necessidade: demonstra o critério de parada que a aula cobra.

**Uso alternativo, caso a validação não sustente o padrão:** decorar `PartidaListener`
(`ListenerComLogDecorator` sobre `ConsoleView`). É honesto, mas mais fraco — a
combinação de listeners já é resolvida pelo próprio Observer, que aceita N observadores.
Aplicar Decorator ali seria redundante. **Preferir cortar o padrão a forçá-lo.**

**No diagrama (se entrar):** `ValidacaoDecorator` implementa `RegraDeValidacaoStrategy`
**e** agrega uma `RegraDeValidacaoStrategy` — a seta que volta para a própria abstração
é a assinatura visual do padrão e precisa estar desenhada com multiplicidade `1`.

---

## 3. Padrões auxiliares, considerados e rejeitados

Esta seção **entra no relatório**. O enunciado penaliza overengineering explicitamente;
por isso registramos tanto o Builder auxiliar adotado quanto os padrões descartados.

| Padrão | Onde caberia | Decisão |
|---|---|---|
| **State** | `EstadoPartida` como hierarquia de classes | Não foi dado em aula. Nossos 4 estados têm transições lineares e nenhum comportamento próprio — um `enum` com validação de transição resolve. State aqui troca 1 enum por 4 classes sem ganho. |
| **Chain of Responsibility** | encadeamento de validações | Não foi dado em aula, e resolve o mesmo problema do Decorator (que **foi** dado). Entre dois padrões equivalentes, usar o da ementa. |
| **Iterator** | percorrer `MaoDeCartas` | Java já entrega via `Iterable`. Implementar à mão é reinventar a biblioteca padrão. |
| **Adapter** | camada de console | Não há interface incompatível a adaptar — o console é código nosso, escrito já no formato certo. |
| **Singleton** | `BaralhoFactory` | Não foi dado em aula, introduz estado global e quebra o isolamento dos testes. Rejeição fácil de defender. |
| **Abstract Factory** | criação de baralho | Há um único produto variável, não uma família. Ver 2.3. |
| **Builder** | `PartidaConfig` | **Adotado como decisão auxiliar:** a configuração tem jogadores, fábrica de baralho, distribuição, três regras, primeiro jogador e listeners. Evita construtor longo e posicional. Não conta entre os quatro padrões da disciplina porque não foi estudado. |

---

## 4. Pontos de extensão × padrão (requisito 2: mínimo 5)

Cada ponto de extensão existe **por causa de** um padrão. Esta tabela é a resposta
direta à exigência de *"identificar explicitamente quais elementos representam
componentes reutilizáveis e quais representam pontos de extensão"* (p. 2 do enunciado).

| # | Ponto de extensão (hot-spot) | Padrão que o sustenta | O que o jogo cliente faz |
|---|---|---|---|
| 1 | `Carta` | Factory Method | define seu tipo de carta |
| 2 | `BaralhoFactory` | Factory Method | define a composição do baralho |
| 3 | `EstrategiaDeDistribuicao` | Strategy | define quantas cartas e em que ordem |
| 4 | `EstrategiaDeDecisao` | Strategy | define bots e o jogador humano |
| 5 | `RegraDeValidacaoStrategy` | Strategy (+ Decorator) | define validações do jogo |
| 6 | `RegraDeVitoriaStrategy` | Strategy | define a condição de vitória |
| 7 | `RegraDePontuacaoStrategy` | Strategy | define o cálculo de pontos |
| 8 | `Jogada` / `EtapaDeTurno` | Strategy | define ações e fases próprias do jogo |
| 9 | `EventoDePartida` / `PartidaListener` | Observer | define eventos e reações (console, placar, log) |
| 10 | `MotorDePartida.executarTurno()` | Template Method | especializa o turno do jogo |

**Componentes reutilizáveis (frozen-spots):** `MotorDePartida.executar()`,
`GerenciadorDeTurnos`, `EstadoPartida`, `BaralhoPadrao`, `MaoDeCartasPadrao`,
`JogadorPadrao`, mecanismo de notificação e hierarquia de exceções.

---

## 5. GRASP — como justificar (aulas 10.2, 11.2, 12.2)

A aula 12.2 abre com a pergunta que organiza esta seção: *"onde esse método deve ficar?"*

| Princípio GRASP | Onde aplicamos | Argumento |
|---|---|---|
| **Especialista na Informação** (12.2) | `RegraDePontuacaoStrategy.calcular(ContextoDePontuacao)`, não `MotorDePartida.pontuarMao(...)` | a regra concreta conhece o algoritmo de pontuação; a mão apenas encapsula cartas. Isso evita acoplar um componente reutilizável às regras de um jogo. |
| **Alta Coesão** (10.2) | separação `MotorDePartida` / `GerenciadorDeTurnos` | *"cada classe faz poucas coisas"*. Fluxo da partida e ordem de turnos são responsabilidades distintas; juntá-las criaria a classe-Deus típica. |
| **Baixo Acoplamento** (11.2) | `core` depende só de interfaces da `api` | a aula mede acoplamento por *"quanto uma classe conhece outra / quanto cria objetos"*: o `core` **não instancia** nenhuma classe de jogo — quem instancia é a `BaralhoFactory` injetada. |
| **Polimorfismo** | despacho das regras | substitui `if/else` por tipo — ver slide 04.1. |
| **Variações Protegidas** | interfaces da `api` como fronteira | a variação (o jogo) fica atrás de uma interface estável; o slide 03.1 chama isso de *"a interface será estável por natureza"*. |
| **Criador** | `BaralhoFactory` cria `BaralhoPadrao`; `ContextoDePartida` cria e associa mãos | quem possui os dados necessários coordena a criação, sem expor coleções. |

**Alerta de coerência para a defesa.** Existe uma tensão real entre o Template Method
(que a aula 06.2 admite gerar *"alto acoplamento estrutural"* por herança) e o princípio
de Baixo Acoplamento da aula 11.2. **Não escondam isso** — é provável que seja
perguntado. A resposta: aceitamos acoplamento por herança **apenas** no eixo do fluxo da
partida, onde a sequência precisa ser imutável; todos os outros eixos de variação usam
composição via Strategy. É uma escolha consciente de onde pagar o custo.

---

## 6. SOLID — como justificar (aulas 08.2 e 09.2)

| Princípio | Onde | Argumento |
|---|---|---|
| **SRP** | `MotorDePartida` orquestra, `GerenciadorDeTurnos` ordena, `Regra*` decide, `ConsoleView` exibe | quatro motivos de mudança, quatro classes |
| **OCP** | os 7 pontos de extensão da seção 4 | a aula 09.2 dedica ~8 slides ao OCP — é o princípio central. *"Uma nova funcionalidade deve ser adicionada com mudanças mínimas no código existente."* Um jogo novo = **zero** linhas alteradas no `core`. |
| **LSP** | `MotorDeTrinca` e `MotorDeBlackjack` substituem `MotorDePartida` | nenhuma subclasse fortalece pré-condição nem lança exceção não prevista pelo contrato |
| **ISP** | três interfaces de regra separadas em vez de uma `RegraDoJogo` | jogo nenhum implementa método que não usa |
| **DIP** | `core` → interfaces da `api`; concretas injetadas pelo cliente | o slide 04.1 formula assim: *"a classe principal depende da abstração e não das implementações concretas"* |

**Prova mecânica do OCP para a apresentação.** Rodar ao vivo:

```bash
grep -rn "import br.edu.uepb.map.\(trinca\|blackjack\)" src/main/java/br/edu/uepb/map/cardgame/
```

Saída vazia = o framework não conhece nenhum jogo. É a demonstração mais curta e mais
convincente que temos, e cobre de uma vez o requisito 3 e o OCP.

---

## 7. Guia do diagrama de classes (para Raffael — Trilha E)

O enunciado exige **multiplicidades** nominalmente; é item de checagem.

**Notação por tipo de relacionamento:**

| Relacionamento | Notação | Onde usar |
|---|---|---|
| Herança | linha cheia, triângulo vazado | `MotorDeTrinca → MotorDePartida` |
| Realização (implementação) | linha **tracejada**, triângulo vazado | `ValidacaoTrinca ⇢ RegraDeValidacaoStrategy` |
| Composição (o todo destrói a parte) | losango **cheio** | `Baralho ◆— Carta`, `ContextoDePartida ◆— MaoDeCartas` |
| Agregação (parte sobrevive ao todo) | losango **vazado** | `MotorDePartida ◇— PartidaListener`, `Mesa ◇— Carta` |
| Associação direcionada | seta simples | `MotorDePartida → RegraDeVitoriaStrategy` |

**Multiplicidades a desenhar:**

```
MotorDePartida    1  ——  2..*  Jogador          (abre para N, como o enunciado pede)
MotorDePartida    1  ——  1     GerenciadorDeTurnos
MotorDePartida    1  ——  1     Baralho
MotorDePartida    1  ——  1     EstadoPartida
MotorDePartida    1  ——  0..*  PartidaListener
MotorDePartida    1  ——  1     RegraDeValidacaoStrategy
MotorDePartida    1  ——  1     RegraDePontuacaoStrategy
MotorDePartida    1  ——  1     RegraDeVitoriaStrategy
Jogador           1  ——  1..*  MaoDeCartas      (Blackjack pode dividir a mão)
Jogador           1  ——  1     EstrategiaDeDecisao
MaoDeCartas       1  ——  0..*  Carta
Baralho           1  ——  0..*  Carta
Mesa              1  ——  0..*  Carta
```

**Convenções visuais:**

- Classes abstratas e métodos abstratos em *itálico*; interfaces com `«interface»`.
- **Separe visualmente os pacotes:** `api` e `core` de um lado, `trinca` e `blackjack`
  do outro. **Todas as setas entre os blocos devem apontar do jogo para o framework,
  nunca o contrário.** Um diagrama em que isso é óbvio já defende o requisito 3 sozinho.
- Marque cada hot-spot da seção 4 com um destaque (cor ou estereótipo `«extensão»`) —
  o enunciado pede identificação explícita, e o diagrama é o melhor lugar.
- Para os recortes do relatório (o enunciado sugere justificar *"utilizando trechos do
  diagrama"*), gere 5 sub-diagramas, um por padrão. Cabe melhor em 8 páginas do que um
  diagrama gigante ilegível.

---

## 8. Roteiro da defesa — perguntas prováveis

Todos os cinco apresentam. Cada um deve conseguir responder pelo próprio padrão **e**
pelo do colega (ver seção 6 de `divisao-responsabilidades.md`).

| Pergunta esperada | Onde está a resposta |
|---|---|
| "Por que Template Method e não Strategy no motor?" | 2.1, alternativa rejeitada |
| "Isso não é Abstract Factory?" | 2.3, distinção |
| "Por que três interfaces de regra e não uma?" | 2.2, ISP |
| "Onde vocês usaram herança e onde usaram composição, e por quê?" | 5, alerta de coerência |
| "Mostre onde eu adiciono um jogo novo sem tocar no framework" | 4 + `grep` da seção 6 |
| "Vocês não exageraram nos padrões?" | 3, tabela de rejeitados + critério do Decorator em 2.5 |
| "Qual padrão vocês quase usaram e desistiram?" | 3 (State e Chain of Responsibility) |

---

## 9. Pendências que travam este documento

1. **Regras do Blackjack.** A equipe escolheu a versão completa, mas ainda precisa
   congelar a regra de mesa para fechar validações e decidir o Decorator (2.5).
2. **Assinaturas da API.** Elas serão derivadas de
   `modelo-conceitual-framework.md` e aprovadas por A-D antes de o UML ou novos
   imports se consolidarem.
3. **Decidir se o MVC (aula 02.1) entra no relatório** como leitura arquitetural
   (ver 2.4). Custo: meio parágrafo. Retorno: mostra composição de padrões, que é a tese
   daquela aula.

---

*Fontes: `PROJETOS/AtividadeProposta.pdf` (pp. 1–3) e `MATERIAL_DA_AULA/` — aulas 02.1,
03.1, 04.1, 05.1, 06.2, 07.1, 08.2, 09.2, 10.2, 11.2 e 12.2. As citações entre aspas são
transcrições dos slides. Documento redigido em 15/08/2026.*
