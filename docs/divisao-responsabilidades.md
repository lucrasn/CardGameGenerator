# Divisão de Responsabilidades — Projeto Final MAP (CPT01091)

**Equipe (5 integrantes):** Lucas · Allan · Raffael · Lívia · Júlio
**Jogo cliente principal:** Trinca
**Jogo-prova de extensibilidade (defesa):** Blackjack mínimo
**Status deste documento:** trilhas **definidas** — ver alocação na seção 3.

---

## 0. O que o enunciado cobra (fonte: `PROJETOS/AtividadeProposta.pdf`)

Resumo dos itens que a divisão precisa cobrir, para servir de checklist:

| # | Requisito obrigatório (p. 3 do PDF) |
|---|---|
| 1 | API pública claramente definida |
| 2 | Pelo menos **cinco** pontos de extensão |
| 3 | Separação entre código da solução e código das aplicações clientes |
| 4 | Pelo menos uma aplicação cliente (jogo) |
| 5 | Uso de interfaces e classes abstratas |
| 6 | Tratamento adequado de exceções |
| 7 | Encapsulamento das coleções internas |
| 8 | Testes automatizados |
| 9 | Javadoc da API pública |
| 10 | Diagrama de classes simplificado (especificação escrita) |
| 11 | Exemplos de utilização (na apresentação) |
| 12 | Justificativa das decisões de projeto (especificação escrita) |

Além disso (p. 2): **no mínimo 4 padrões GoF**, justificativa de **SOLID** e **GRASP**,
diagrama UML com multiplicidades, relatório de **até 8 páginas**, e
**todos os 5 integrantes apresentam** o design para a professora.

### ⚠ Dois pontos de atenção (um decidido, um pendente)

**(a) Segundo jogo — DECIDIDO.** A p. 2 diz: *"criar pelo menos uma aplicação cliente
[...] e mostrar na defesa do projeto que é possível criar outro jogo utilizando a
mesma biblioteca. Os jogos devem possuir diferenças suficientes para demonstrar
extensibilidade."* A equipe optou por **Trinca completa + Blackjack mínimo** como
jogo-prova. O Blackjack não precisa de polimento de console: seu único papel é provar
em tela que o framework se estende **sem alterar uma linha do `core`**. A diferença
entre os dois é o argumento da defesa — Trinca é jogo de formação de conjunto entre
jogadores, Blackjack é jogo de acumulação contra limite fixo com dealer; eles exercitam
hot-spots diferentes (regra de vitória, regra de pontuação, condição de parada de turno).

**(b) Regras da Trinca.** As regras exatas (nº de cartas na mão, condição de vitória,
compra/descarte, empate) **não estão no PDF** e precisam ser escritas pela equipe
antes da Fase 1 — elas definem quais hot-spots o framework precisa expor. Enquanto o
documento de regras não existir, a Trilha E fica bloqueada.

---

## 1. Princípio da divisão

O corte **não** é por "quem escreve mais classe". É por **fronteira de contrato**:
cada trilha é dona de um conjunto de tipos e das interfaces que expõe às outras.
Isso é o que permite trabalhar em paralelo sem conflito de merge e é também o que
vocês vão ter que defender oralmente ("qual sua responsabilidade nesta arquitetura?").

Três regras que valem para **todas** as trilhas, sem exceção:

1. **Quem escreve a classe escreve o teste dela.** Não existe "trilha de testes".
   Testes espalhados por dono é o que faz o requisito 8 sobreviver ao prazo.
2. **Quem escreve o tipo público escreve o Javadoc dele** (requisito 9).
3. **Quem é dono da trilha escreve a seção correspondente do relatório** — em
   rascunho, no `docs/`. Um integrante (ver Trilha E) apenas costura, uniformiza e
   corta para caber nas 8 páginas.

---

## 2. As cinco trilhas

### Trilha A — Motor de Partida e Ciclo de Vida

**Dono do fluxo de execução do framework: a Inversão de Controle mora aqui.**

Arquivos: `core/MotorDePartida`, `core/GerenciadorDeTurnos`, `core/EstadoPartida`
(hoje `EstadoPartid` — corrigir o typo), e a classe/builder de configuração da partida.

Responsabilidades:

- Implementar o laço da partida como **Template Method**: a sequência
  (preparar → distribuir → laço de turnos → apurar vencedor → encerrar) é fixa e
  fechada; os passos variáveis são chamadas às abstrações da API.
- Ordem de turnos, inversão de sentido, pular jogador, e a política de expansão
  para N jogadores (o PDF pede 2 jogadores **com abertura para mais**).
- Máquina de estados da partida e as transições legais entre eles.
- Garantir que o `core` **não importa nada** de pacote de jogo concreto — essa é a
  prova mecânica do requisito 3.

Padrões que esta trilha justifica no relatório: **Template Method** (obrigatório),
**State** ou **Builder** (opcionais — avaliar contra *overengineering*; o PDF
penaliza excesso explicitamente).

Entrega de defesa: "por que o framework chama o jogo e não o contrário".

---

### Trilha B — Domínio de Cartas, Baralho e Mesa

**Dono dos dados do jogo e do encapsulamento das coleções (requisito 7).**

Arquivos: `api/Carta`, `api/BaralhoFactory`, `core/BaralhoBase`, `core/MaoDeCartas`,
`core/Mesa`.

Responsabilidades:

- Modelar `Carta` como abstração aberta o suficiente para carta francesa (naipe +
  valor), carta de Uno (cor + símbolo) e carta de atributos (Super Trunfo) — sem
  que o `core` saiba de nenhuma delas.
- **Criação de baralho como ponto de extensão** (`BaralhoFactory`): baralho de 52,
  de 40 (truco), com/sem coringa. Padrão **Factory Method** / **Abstract Factory**.
- Embaralhamento e **formas diferentes de distribuir cartas** como estratégia
  substituível (o PDF cita isso explicitamente na p. 1).
- **Encapsulamento rigoroso:** `MaoDeCartas`, `BaralhoBase` e `Mesa` nunca devolvem
  a `List` interna. Cópia defensiva ou `Collections.unmodifiableList`. Este é um
  requisito literal do enunciado e um item fácil de perder na correção.

Padrões: **Factory Method/Abstract Factory**, opcionalmente **Iterator** e
**Prototype** (avaliar necessidade real).

Entrega de defesa: "mostre que eu não consigo corromper a mão de um jogador por fora".

---

### Trilha C — Jogadores e Estratégias de Decisão

**Dono da variação de comportamento do jogador (humano vs. automatizado).**

Arquivos: `api/Jogador`, mais `JogadorHumano`, `JogadorAutomatizado`, a interface de
estratégia de decisão, e a camada de entrada/saída de console.

Responsabilidades:

- Separar **identidade do jogador** (nome, mão, pontuação) de **como ele decide** —
  se as duas coisas ficarem na mesma classe, o requisito de "diferentes estratégias
  de tomada de decisão" (p. 1) vira herança em vez de composição, e a defesa fica
  ruim. Padrão **Strategy**.
- Implementar pelo menos duas estratégias automatizadas com comportamento
  distinguível (ex.: aleatória e gulosa), para o relatório ter o que comparar.
- Isolar o console atrás de uma abstração de I/O — sem isso, **nenhum teste
  automatizado de jogador é possível** (bloqueia o requisito 8 da Trilha E também).
- Modelar a "jogada" / ação do jogador como tipo próprio, em vez de trafegar
  `String` do console pelo motor.

Padrões: **Strategy** (obrigatório), opcionalmente **Null Object** para jogador
inativo e **Adapter** na camada de console.

Entrega de defesa: "troque o jogador humano por um bot sem recompilar o motor".

---

### Trilha D — Regras, Exceções e Eventos

**Dono das três estratégias de regra e do tratamento de erro do framework.**

Arquivos: `api/RegraDeValidacaoStrategy`, `api/RegraDePontuacaoStrategy`,
`api/RegraDeVitoriaStrategy`, `api/PartidaListener`, e o pacote de exceções de
domínio (a criar).

Responsabilidades:

- Definir os três contratos de regra e garantir que sejam **independentes entre si**
  (validar ≠ pontuar ≠ vencer). Padrão **Strategy**; avaliar **Chain of
  Responsibility** se a Trinca exigir validações compostas.
- Hierarquia de exceções de domínio (`JogadaInvalidaException`,
  `BaralhoVazioException`, `EstadoDePartidaInvalidoException`) e a política:
  o que é exceção verificada, o que é não-verificada, o que o motor captura e o que
  propaga. **Requisito 6 inteiro é desta trilha.**
- **Observer** via `PartidaListener` para os "diferentes eventos durante a partida"
  (p. 1): carta jogada, turno encerrado, partida finalizada. É o que permite que o
  console seja um observador e não um acoplamento dentro do motor.

Padrões: **Strategy** (obrigatório), **Observer** (obrigatório), opcionalmente
**Chain of Responsibility**.

Entrega de defesa: "onde eu ponho uma regra nova sem tocar em classe existente" —
esta é a resposta direta ao Open-Closed Principle.

---

### Trilha E — Aplicação Cliente (Trinca), UML e Relatório

**Dono do produto final e da prova de que o framework serve para alguma coisa.**

Arquivos: pacote `br.edu.uepb.map.trinca` (separado do framework — requisito 3),
o jogo-prova mínimo, `docs/especificacao_arquitetural.md`, e o diagrama UML.

Responsabilidades:

- Escrever o **documento de regras da Trinca** (ver ponto de atenção (b) acima) —
  isso é a **primeira entrega do projeto inteiro**, porque as Trilhas B, C e D
  dependem dele para saber quais hot-spots expor.
- Implementar a Trinca **usando apenas a API pública**. Se em algum momento
  precisar mexer no `core` para o jogo funcionar, isso é um **defeito de projeto**
  a ser reportado ao dono da trilha correspondente — não contornado. Esta trilha é,
  na prática, o **teste de aceitação vivo da arquitetura**.
- Implementar o jogo-prova mínimo para a defesa.
- **Diagrama UML** com classes, interfaces, associação/herança/implementação/
  composição/agregação **e multiplicidades** (o PDF pede multiplicidades
  nominalmente — é item de checklist da correção).
- Consolidar o relatório de 8 páginas a partir dos rascunhos das outras trilhas.

**Por que a carga faz sentido:** nas primeiras semanas esta trilha está *bloqueada*
para código (o framework ainda não existe) — é exatamente o tempo em que ela produz
regras, UML e esqueleto do relatório. Depois inverte: quando as outras trilhas
fecham, esta vira a mais pesada em implementação. A carga total empata; ela só está
distribuída em ordem invertida no tempo.

**Risco desta trilha:** é a única que concentra dois entregáveis distintos (código +
documento). Válvula de escape acordada: se na Fase 3 ficar pesada, o **relatório**
migra para a Trilha A (Lucas), que tem folga na fase final porque o motor congela
cedo — mas o **UML não migra**, ele precisa ficar com quem monta os jogos.

---

## 3. Tabela de alocação (definida)

| Trilha | Integrante | Pacotes de que é dono |
|---|---|---|
| A — Motor e Ciclo de Vida | **Lucas** | `core` (motor, turnos, estado) |
| B — Cartas, Baralho, Mesa | **Júlio** | `api/Carta`, `api/BaralhoFactory`, `core` (baralho, mão, mesa) |
| C — Jogadores e Estratégias | **Allan** | `api/Jogador`, jogadores, I/O de console |
| D — Regras, Exceções, Eventos | **Lívia** | `api/Regra*`, `api/PartidaListener`, exceções |
| E — Jogo, UML e Relatório | **Raffael** | `br.edu.uepb.map.trinca`, `br.edu.uepb.map.blackjack`, `docs/` |

---

## 4. Cobertura dos requisitos por trilha

Nenhum requisito pode ficar sem dono. Confira contra a tabela da seção 0:

| Req. | Descrição | Dono |
|---|---|---|
| 1 | API pública definida | A (fronteira) + B, C, D (conteúdo) |
| 2 | ≥ 5 pontos de extensão | B (2) + C (1) + D (3) — ver seção 5 |
| 3 | Separação framework/cliente | A garante no `core`; E prova ao consumir |
| 4 | Aplicação cliente | E |
| 5 | Interfaces e classes abstratas | todas |
| 6 | Tratamento de exceções | **D** |
| 7 | Encapsulamento de coleções | **B** |
| 8 | Testes automatizados | cada trilha testa o que escreve; E faz o de integração |
| 9 | Javadoc da API pública | cada trilha documenta o que escreve |
| 10 | Diagrama de classes | **E** |
| 11 | Exemplos de utilização | **E** |
| 12 | Justificativa das decisões | rascunho por trilha, consolidação em E |

---

## 5. Padrões GoF e pontos de extensão (mapa anti-duplicação)

O PDF exige **≥ 4 padrões** e **≥ 5 pontos de extensão**. Este mapa existe para
ninguém aplicar o mesmo padrão em dois lugares por acidente — e para ninguém
enfiar padrão a mais só para "encher", o que o enunciado penaliza como
*overengineering*.

**Padrões (4 obrigatórios já cobertos):**

| Padrão | Onde | Trilha | Problema que resolve |
|---|---|---|---|
| Template Method | `MotorDePartida` | A | fluxo da partida é invariante, os passos variam por jogo |
| Factory Method | `BaralhoFactory` | B | criar baralhos de composições diferentes sem o motor saber qual |
| Strategy | regras de validação/pontuação/vitória e decisão do jogador | C, D | variar algoritmo em tempo de execução, sem herança |
| Observer | `PartidaListener` | D | notificar eventos sem acoplar o motor ao console |

*Opcionais, só se a Trinca exigir de fato:* State (A), Chain of Responsibility (D),
Iterator (B), Adapter (C).

**Pontos de extensão (5 obrigatórios, 6 mapeados):**

1. `Carta` — novos tipos de carta (B)
2. `BaralhoFactory` — novas composições de baralho (B)
3. Estratégia de distribuição — novas formas de distribuir (B)
4. Estratégia de decisão do jogador — novos bots e jogador humano (C)
5. `RegraDeValidacao` / `RegraDePontuacao` / `RegraDeVitoria` — novas regras (D)
6. `PartidaListener` — novos observadores de evento (D)

---

## 6. Faseamento e dependências

| Fase | O quê | Quem | Bloqueia |
|---|---|---|---|
| 0 | Regras da Trinca escritas + assinaturas das interfaces congeladas em reunião | **todos, juntos, em uma sessão** | tudo |
| 1 | Implementação paralela de A, B, C, D | A, B, C, D | Fase 2 |
| 2 | Integração: primeira partida rodando end-to-end | A + E | Fase 3 |
| 3 | Trinca completa + jogo-prova + testes de integração | E | Fase 4 |
| 4 | UML final, relatório consolidado, ensaio da defesa | **todos** | — |

**A Fase 0 é inegociável e presencial.** Se as assinaturas das interfaces não forem
acordadas antes de alguém escrever código, as quatro trilhas divergem e a Fase 2 vira
uma semana de merge. É o modo mais comum de este tipo de projeto falhar.

**Ensaio da defesa:** o PDF exige que os **cinco** apresentem. Reservem uma sessão na
Fase 4 em que cada um explica a trilha **do colega** — se todo mundo consegue,
a arquitetura está coesa; se não consegue, ela está fragmentada e a professora vai
perceber na hora.

---

## 7. Débitos técnicos a resolver na Fase 0

Encontrados no estado atual do repositório:

1. **`pom.xml` sem JUnit 5.** Não há dependência de teste nem `maven-surefire-plugin`.
   O requisito 8 está literalmente impossível hoje. Primeira tarefa da Fase 0.
2. **`pom.xml` declara `maven.compiler.source/target = 26`, o `README.md` diz
   "Java 17+".** Divergência real: os cinco precisam compilar na mesma versão, ou
   quem tiver JDK mais antigo não builda. Decidam uma e alinhem os dois arquivos.
3. **`core/EstadoPartid.java` — nome truncado**, deveria ser `EstadoPartida`.
   Renomear antes de qualquer import apontar para ele.
4. **`docs/especificacao_arquitetural.md` está vazio** (0 bytes).
5. **Nenhuma classe de exceção existe ainda** — o pacote da Trilha D é criado do zero.
6. **`src/test/` não existe.** Criar a árvore junto com o item 1.

---

## 8. Convenções de trabalho sugeridas

- **Branch por trilha:** `trilha/a-motor`, `trilha/b-baralho`, etc. `main` sempre
  compilando.
- **Ninguém commita direto em `main`.** PR com pelo menos um revisor de outra trilha
  — é isso que faz cada um conhecer o código do colega antes da defesa (ver seção 6).
- **Quem quebra o build conserta o build**, antes de continuar a própria tarefa.
- **Mudança em interface pública da API exige aviso ao grupo**, porque quebra as
  outras trilhas em silêncio.
- Commits seguindo o padrão já usado no repositório (`feat(escopo): ...`,
  `docs(escopo): ...`, `chore(escopo): ...`).

---

*Documento gerado a partir de `PROJETOS/AtividadeProposta.pdf` e do estado do
repositório em 15/08/2026. As regras da Trinca e a distribuição nominal das trilhas
ainda não foram definidas pela equipe.*
