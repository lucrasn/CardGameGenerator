# Arquitetura do CardGame Framework

**Documento canônico da arquitetura.**

**Baseline:** `main`, após integração das Trilhas A, B, C e D em 16/08/2026.

**Validação:** 137 testes, zero falhas e zero erros.

**Documentação complementar:**

- [Manual do cliente](manual-do-cliente.md): guia prático da API, dos pontos de
  extensão e das limitações atuais do framework.

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
```

Regras mecânicas:

1. `api` não importa `engine` nem jogos;
2. `engine` não importa Trinca ou Blackjack;
3. clientes podem estender `MotorDePartida`, mas não acessar seus colaboradores;
4. estado mutável só é alcançado por contextos públicos estreitos;
5. um tipo pertencente a outra trilha não é alterado para “fazer o motor compilar”.

## 3. Organização física honesta

```text
br.edu.uepb.map.cardgame.api
  contratos e valores públicos
  api.evento     seis eventos padrão da partida
  api.excecao    hierarquia de exceções de domínio
  api.estrategia estratégias de decisão reutilizáveis

br.edu.uepb.map.cardgame.engine
  MotorDePartida<C>                         public
  GerenciadorDeTurnos                      package-private
  SentidoDeRotacao                         package-private
  CicloDeVidaDaPartida                     package-private
  PartidaEmExecucao<C>                     package-private
  ContextoDeDistribuicaoInterno<C>         package-private
```

`GerenciadorDeTurnos` fica em `engine`, junto de quem o usa. Isso não é acoplamento
indevido: ele é parte da implementação do motor. Deixá-lo sem `public` impede que o
jogo cliente transforme um detalhe interno em dependência.

São exatamente dois pacotes de produção. Uma única classe pública em `engine` é o que
torna o frozen-spot inviolável: o jogo cliente não consegue instanciar o gerenciador de
turnos nem forçar uma transição de estado, ainda que saiba que eles existem.

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

As exceções de domínio, as três Strategies de regra e o protocolo de eventos estão
implementados:

| Contrato | Operação |
|---|---|
| `RegraDeValidacaoStrategy<C>` | `validar(ContextoDeValidacao<C>)` |
| `RegraDeVitoriaStrategy<C>` | `avaliar(VisaoDaPartida<C>)` |
| `RegraDePontuacaoStrategy<C>` | `calcular(VisaoDaPartida<C>, DesfechoDePartida)` |
| `PartidaListener` | `aoOcorrer(EventoDePartida)` |

`EventoDePartida` é uma interface de extensão; `api.evento` traz seis records imutáveis
prontos: `PartidaIniciada`, `CartasDistribuidas`, `TurnoIniciado`, `TurnoEncerrado`,
`JogadaRejeitada` e `PartidaFinalizada`. Subclasses publicam eventos específicos do jogo
pelo método protegido e final `MotorDePartida.publicarEvento`, reutilizando a mesma
ordem e o mesmo isolamento de falhas dos listeners. Strategy de regras e Observer,
portanto, são padrões demonstráveis em runtime nesta baseline, e não arquitetura
planejada.

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
    ├─ avaliar desfecho inicial pela regra de vitória
    └─ repetir:
         executarTurno(contexto)
         regraDeVitoria.avaliar(visão)
         aplicar ResultadoDoTurno
    │
    ▼
FINALIZADA
    ├─ validar vencedores e placar
    ├─ criar ResultadoDePartida
    └─ aoEncerrar(visão, resultado)
```

Operação primitiva obrigatória — a única:

- `executarTurno(ContextoDePartida<C>)`.

Hooks opcionais:

- `preparar`;
- `aposDistribuir`;
- `aoEncerrar`.

Apoio oferecido ao jogo:

- `validarJogada(VisaoDaPartida<C>, Jogada)`, `final`, que delega à Strategy configurada.
- `publicarEvento(EventoDePartida)`, `final`, que entrega eventos próprios aos
  observers cadastrados no motor.

Vitória e pontuação **deixaram de ser hooks** e passaram a Strategies de `PartidaConfig`.
A decisão eliminou a duplicidade que a versão anterior deste documento previa: existe
uma única fonte de variação para cada decisão, e trocar a condição de vitória não exige
mais criar uma subclasse de motor.

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
| 7 | `RegraDeValidacaoStrategy<C>` | disponível |
| 8 | `RegraDeVitoriaStrategy<C>` | disponível |
| 9 | `RegraDePontuacaoStrategy<C>` | disponível |
| 10 | `PartidaListener` | disponível |
| 11 | `EventoDePartida` próprio do jogo | disponível |

São onze extensões utilizáveis, todas com contrato definido e exercitado por teste. O
requisito da atividade pede no mínimo cinco.

## 9. Padrões, SOLID e GRASP

### Implementados e verificáveis

- **Template Method:** `MotorDePartida.executar()` fixa o algoritmo e chama os hooks;
- **Factory Method:** `BaralhoFactory.criar()` decide a composição;
- **Strategy:** distribuição, decisão, validação, vitória e pontuação são objetos
  substituíveis;
- **Observer:** o motor cadastra ouvintes, publica seis eventos padrão e recebe eventos
  próprios das subclasses, isolando falhas de cada ouvinte;
- **Builder:** construção validada de `PartidaConfig` (apoio, fora da contagem GoF).

Quatro padrões GoF distintos dentre os estudados em sala, o que atende ao mínimo
exigido pela atividade.

### Ainda em avaliação

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

1. implementar Trinca usando apenas `api` e `engine.MotorDePartida`;
2. implementar Blackjack com diferenças suficientes para provar extensibilidade;
3. zerar os sete avisos residuais de Javadoc nos contratos de cartas e distribuição;
4. decidir sobre Decorator segundo o critério objetivo já acordado.

As fronteiras não dependem mais de inspeção por `grep`.
`FronteirasArquiteturaisTest` analisa o bytecode com ArchUnit e falha o build se a
API conhecer o engine ou clientes, se o engine conhecer jogos concretos, ou se outro
tipo público surgir no pacote do runtime.

## 11. Critério de “caminho certo”

A arquitetura está no caminho certo quando um segundo jogo exige novas classes de
cliente, não novos `if (jogo == ...)` no framework. Trinca e Blackjack serão a prova:
se ambos usam o mesmo ciclo, fábrica, distribuição e contexto sem importar internals,
os frozen-spots e hot-spots estão bem posicionados. Se um deles precisar editar o
engine, a abstração correspondente deve ser revista — sem generalizar antecipadamente
uma regra que só existe em um jogo.
