# Arquitetura do CardGame Framework

**Documento canônico da arquitetura.**

**Baseline:** `main`, após integração seletiva da Trilha A em 15/08/2026.

**Validação:** 105 testes, zero falhas e zero erros.

## 1. Objetivo e limite da promessa

O framework pretende apoiar uma família ampla de jogos de cartas, não garantir
literalmente todo jogo concebível. Ele generaliza mecanismos que reaparecem em jogos
independentes:

- identidade de cartas;
- criação, armazenamento e distribuição;
- participantes e tomada de decisão;
- ciclo de vida da partida;
- ordem e efeitos de turno;
- estado, desfecho e placar.

Regras como “formar uma trinca”, “atingir 21”, pilha de descarte, dealer, apostas ou
fases específicas pertencem aos clientes. Uma abstração só deve subir ao framework
quando houver reuso demonstrado por pelo menos dois jogos diferentes.

Essa fronteira evita dois extremos: um motor codificado para Trinca/Blackjack e um
“framework universal” composto por abstrações vazias sem comportamento verificável.

## 2. Direção das dependências

```text
Trinca / Blackjack ─────> cardgame.api
          │
          └─────────────> cardgame.engine.MotorDePartida

cardgame.engine ────────> cardgame.api
auxiliares em core ─────> cardgame.api
```

Regras mecânicas:

1. `api` não importa `engine`, `core` nem jogos;
2. `engine` não importa Trinca ou Blackjack;
3. clientes podem estender `MotorDePartida`, mas não acessar seus colaboradores;
4. estado mutável só é alcançado por contextos públicos estreitos;
5. um tipo pertencente a outra trilha não é alterado para “fazer o motor compilar”.

## 3. Organização física honesta

```text
br.edu.uepb.map.cardgame.api
  contratos e valores públicos

br.edu.uepb.map.cardgame.engine
  MotorDePartida<C>                         public
  GerenciadorDeTurnos                      package-private
  SentidoDeRotacao                         package-private
  CicloDeVidaDaPartida                     package-private
  PartidaEmExecucao<C>                     package-private
  ContextoDeDistribuicaoInterno<C>         package-private

br.edu.uepb.map.cardgame.core
  auxiliares e placeholders legados de outras trilhas
```

`GerenciadorDeTurnos` fica em `engine`, junto de quem o usa. Isso não é acoplamento
indevido: ele é parte da implementação do motor. Deixá-lo sem `public` impede que o
jogo cliente transforme um detalhe interno em dependência.

O pacote `core` ainda existe na baseline porque contém artefatos das Trilhas B/C e
placeholders antigos. A Trilha A não depende dele. Portanto, a afirmação correta hoje
é “não existe `core` da Trilha A”, e não “não existe pacote `core` no projeto”.

## 4. Contratos implementados

### 4.1 Cartas e distribuição — Trilha B

| Tipo | Papel |
|---|---|
| `Carta` | identidade estável, sem impor naipe/valor/cor |
| `Baralho<C>` | sequência encapsulada, compra, topo/base e embaralhamento |
| `BaralhoPadrao<C>` | implementação reutilizável |
| `BaralhoFactory<C>` | cria uma composição nova por partida |
| `MaoDeCartas<C>` | operações controladas de mão |
| `MaoDeCartasPadrao<C>` | implementação encapsulada |
| `EstrategiaDeDistribuicao<C>` | algoritmo substituível |
| `ContextoDeDistribuicao<C>` | porta mínima usada pela distribuição |

O contrato real é genérico. O motor chama `BaralhoFactory.criar()`,
`Baralho.quantidade()` e `ContextoDeDistribuicao.entregarProximaCarta()`; não mantém
uma API paralela com nomes diferentes.

### 4.2 Jogadores e decisão — Trilha C

`Jogador` contém identidade e uma `EstrategiaDeDecisao` por composição. Mãos e placar
pertencem à partida, não ao jogador. Isso permite que a mesma identidade participe de
jogos com modelos de mão diferentes sem herança por “jogador humano”, “bot” ou
“dealer”.

### 4.3 Ciclo de vida — Trilha A

Tipos públicos:

- `EstadoPartida`;
- `PartidaConfig<C>`;
- `VisaoDaPartida<C>`;
- `ContextoDePartida<C>`;
- `ResultadoDoTurno`;
- `DesfechoDePartida` e `ResultadoDePartida`;
- `MotivoDeEncerramento` e `MotivoPadrao`;
- `engine.MotorDePartida<C>`.

`VisaoDaPartida.maoDe()` devolve `List<C>` imutável. A implementação mutável de
`MaoDeCartas<C>` fica dentro de `PartidaEmExecucao` e não vaza para regras ou clientes.

### 4.4 Regras, eventos e exceções — Trilha D

As exceções de domínio estão implementadas. As interfaces abaixo continuam vazias na
`main` e devem permanecer assim até a Trilha D aprovar suas assinaturas:

- `RegraDeValidacaoStrategy`;
- `RegraDeVitoriaStrategy`;
- `RegraDePontuacaoStrategy`;
- `PartidaListener`.

`EventoDePartida` e eventos concretos ainda não existem. Portanto, Strategy de regras
e Observer são arquitetura planejada, não padrões já demonstráveis na baseline.

## 5. Template Method do motor

`MotorDePartida.executar()` é `public final` e pode ser chamado uma única vez:

```text
CONFIGURADA
    │
    ▼
PREPARANDO
    ├─ criar e embaralhar baralho
    ├─ preparar(contexto)
    ├─ distribuir(contexto estreito)
    └─ aposDistribuir(contexto)
    │
    ▼
EM_ANDAMENTO
    ├─ avaliar desfecho inicial
    └─ repetir:
         executarTurno(contexto)
         avaliarDesfecho(visão)
         aplicar ResultadoDoTurno
    │
    ▼
FINALIZADA
    ├─ validar vencedores e placar
    ├─ criar ResultadoDePartida
    └─ aoEncerrar(visão, resultado)
```

Hooks obrigatórios enquanto as Strategies da Trilha D estão pendentes:

- `executarTurno(ContextoDePartida<C>)`;
- `avaliarDesfecho(VisaoDaPartida<C>)`.

Hooks opcionais:

- `preparar`;
- `aposDistribuir`;
- `calcularPontuacao` — zero para todos por padrão;
- `aoEncerrar`.

Quando as Strategies da Trilha D forem definidas, a equipe deverá escolher uma única
fonte de variação. Não se deve manter simultaneamente hooks e Strategies duplicando a
mesma decisão sem uma justificativa explícita.

## 6. Turnos

`ResultadoDoTurno` é uma diretiva declarativa. O jogo escolhe o efeito, mas apenas o
engine altera a ordem:

- `avancar()`;
- `repetir()`;
- `inverter()`;
- `pular(int)`.

`GerenciadorDeTurnos` usa aritmética modular e aceita N participantes. Identidades
duplicadas, índice inicial inválido e pulos negativos são recusados. O tipo é interno,
logo um cliente não consegue avançar a vez fora do Template Method.

Uma `JogadaInvalidaException` lançada por `executarTurno` repete o mesmo turno. Após
100 recusas consecutivas, o motor lança `IllegalStateException` para impedir um laço
infinito causado por um cliente defeituoso.

## 7. Estado e invariantes

Transições válidas:

```text
CONFIGURADA → PREPARANDO → EM_ANDAMENTO → FINALIZADA
```

Invariantes verificadas:

- ao menos dois jogadores;
- identidades de jogadores únicas;
- índice inicial dentro da lista;
- fábrica e distribuição não nulas;
- fábrica não devolve baralho nulo;
- cartas não aparecem simultaneamente em baralho/mãos;
- coleções públicas são snapshots imutáveis;
- vencedor pertence à partida;
- placar contém exatamente todos os participantes;
- não há mutação após `FINALIZADA`;
- segunda chamada de `executar()` é recusada.

`MotivoDeEncerramento` é interface aberta. `MotivoPadrao` cobre vitória, empate,
esgotamento e abandono, enquanto jogos podem declarar motivos próprios sem editar o
framework.

## 8. Pontos de extensão

| # | Ponto | Estado atual |
|---|---|---|
| 1 | novas implementações de `Carta` | disponível |
| 2 | `BaralhoFactory<C>` | disponível |
| 3 | `EstrategiaDeDistribuicao<C>` | disponível |
| 4 | `EstrategiaDeDecisao` | disponível |
| 5 | subclasse de `MotorDePartida<C>` | disponível |
| 6 | `MotivoDeEncerramento` específico | disponível |
| 7 | validação por Strategy | contrato pendente |
| 8 | vitória por Strategy | contrato pendente |
| 9 | pontuação por Strategy | contrato pendente |
| 10 | eventos/observadores | pendente |

Mesmo sem contar placeholders, há seis extensões utilizáveis. Para a defesa final,
os pontos 7–10 só devem ser apresentados como implementados depois que possuírem
operações, implementações concretas e testes.

## 9. Padrões, SOLID e GRASP

### Implementados e verificáveis

- **Template Method:** `MotorDePartida.executar()` fixa o algoritmo e chama hooks;
- **Factory Method:** `BaralhoFactory.criar()` decide a composição;
- **Strategy:** distribuição e decisão são objetos substituíveis;
- **Builder:** construção validada de `PartidaConfig` (apoio, fora da contagem GoF).

### Planejados

- **Strategy de regras:** interfaces ainda vazias;
- **Observer:** listener ainda vazio e eventos ausentes;
- **Decorator:** somente se houver composição real de três ou mais validações.

Evidências de SOLID/GRASP:

- **SRP / Alta Coesão:** ciclo, turnos, cartas, decisão e resultado têm donos distintos;
- **OCP / Polimorfismo:** cartas, fábrica, distribuição, decisão, motivo e motor variam
  sem condicionais por jogo;
- **LSP:** subclasses não substituem o algoritmo `final`;
- **ISP:** distribuição recebe uma porta estreita; visão somente leitura é separada
  do contexto mutável;
- **DIP / Baixo Acoplamento:** engine depende de contratos em `api`;
- **Especialista:** `EstadoPartida` conhece suas transições e
  `GerenciadorDeTurnos` conhece a rotação;
- **Controlador:** `MotorDePartida` recebe o evento sistêmico de executar a partida;
- **Creator:** o motor cria a execução transitória que agrega seus colaboradores.

## 10. O que ainda falta provar

1. Trilha D definir e testar regras e Observer;
2. integrar esses contratos sem duplicar os hooks atuais;
3. implementar Trinca usando apenas API/`MotorDePartida`;
4. implementar Blackjack com diferenças suficientes;
5. remover ou realocar placeholders legados de `core` pelos respectivos donos;
6. executar testes arquiteturais de dependência entre pacotes;
7. atualizar o UML final após congelar as assinaturas.

## 11. Critério de “caminho certo”

A arquitetura está no caminho certo quando um segundo jogo exige novas classes de
cliente, não novos `if (jogo == ...)` no framework. Trinca e Blackjack serão a prova:
se ambos usam o mesmo ciclo, fábrica, distribuição e contexto sem importar internals,
os frozen-spots e hot-spots estão bem posicionados. Se um deles precisar editar o
engine, a abstração correspondente deve ser revista — sem generalizar antecipadamente
uma regra que só existe em um jogo.
