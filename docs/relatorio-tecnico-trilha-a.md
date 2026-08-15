# Relatório técnico da Trilha A — motor e ciclo de vida

**Projeto:** CardGame Framework  
**Disciplina:** Métodos Avançados de Programação — UEPB  
**Responsável pela trilha:** Lucas  
**Baseline analisada:** `main`, em 15/08/2026  
**Escopo:** motor, ciclo de vida, turnos, estado interno, contextos e resultados

## 1. Resumo executivo

A Trilha A implementa o fluxo reutilizável de uma partida de cartas sem codificar
regras de Trinca, Blackjack ou qualquer outro cliente dentro do framework. O
algoritmo comum fica em `MotorDePartida<C>`, enquanto o jogo concreto fornece apenas
os passos que realmente variam. O resultado é um runtime apropriado para uma
**família de jogos de cartas baseados em turnos**, e não uma promessa literal de
suportar todo jogo concebível sem extensões futuras.

A solução está bem orientada quanto a baixo acoplamento, alta coesão e inversão de
controle. O motor depende dos contratos genéricos de `cardgame.api`; seus
colaboradores mutáveis permanecem internos a `cardgame.engine`; e o cliente não
consegue avançar turnos, trocar o estado ou finalizar a partida fora do fluxo
controlado.

O escopo ainda não representa o framework final da equipe. As Strategies de regras e
o Observer da Trilha D continuam pendentes, e a `main` ainda não contém os clientes
concretos Trinca e Blackjack. Portanto, a conclusão defensável é que a Trilha A
fornece uma base genérica e testada, cuja extensibilidade completa ainda deve ser
demonstrada pelos dois jogos.

## 2. Posição na arquitetura

A direção de dependência adotada é:

```text
Trinca / Blackjack ─────> cardgame.api
          │
          └─────────────> cardgame.engine.MotorDePartida

cardgame.engine ────────> cardgame.api
```

Somente `MotorDePartida` é público no pacote `engine`. Os demais tipos têm
visibilidade de pacote e, por isso, não podem se tornar dependências acidentais dos
jogos clientes.

| Componente reutilizável | Responsabilidade |
|---|---|
| `MotorDePartida<C>` | controlar o algoritmo completo e a inversão de controle |
| `PartidaConfig<C>` | reunir e validar participantes, fábrica, distribuição e primeiro jogador |
| `EstadoPartida` / `CicloDeVidaDaPartida` | definir e aplicar transições legais |
| `GerenciadorDeTurnos` | manter ordem, sentido, repetição e pulos para N jogadores |
| `PartidaEmExecucao<C>` | armazenar o agregado mutável sem expor sua representação |
| `VisaoDaPartida<C>` | oferecer leitura sem autoridade de mutação |
| `ContextoDePartida<C>` | oferecer somente as mutações de cartas permitidas |
| `ResultadoDoTurno` | comunicar uma diretiva ao controlador sem manipular turnos diretamente |
| `DesfechoDePartida` / `ResultadoDePartida` | representar valores finais imutáveis |

## 3. Fluxo de execução

O método `MotorDePartida.executar()` é `public final` e representa o Template
Method. Uma instância aceita exatamente uma execução:

```text
CONFIGURADA
    │
    ▼
PREPARANDO
    ├─ criar e embaralhar o baralho
    ├─ criar mãos, turnos e estado interno
    ├─ preparar(contexto)
    ├─ executar a Strategy de distribuição
    └─ aposDistribuir(contexto)
    │
    ▼
EM_ANDAMENTO
    ├─ avaliar um possível desfecho inicial
    └─ repetir:
         executarTurno(contexto)
         avaliarDesfecho(visão)
         aplicar repetição, inversão, pulo ou avanço
    │
    ▼
FINALIZADA
    ├─ validar vencedores e placar
    ├─ criar ResultadoDePartida
    └─ aoEncerrar(visão, resultado)
```

O jogo escolhe o que acontece em seu turno, mas não controla a sequência global.
Isso é Inversão de Controle: o cliente chama `executar()` uma vez e passa a ser
chamado pelo framework nos pontos de extensão protegidos.

Os hooks obrigatórios atuais são `executarTurno` e `avaliarDesfecho`. `preparar`,
`aposDistribuir`, `calcularPontuacao` e `aoEncerrar` possuem comportamento padrão e
podem ser sobrescritos quando o jogo precisar. `MotivoDeEncerramento` também é um
contrato aberto para motivos específicos. Assim, a Trilha A oferece mais de cinco
pontos reais de extensão sem condicionais do tipo `if (jogo == TRINCA)`.

## 4. Principais decisões de projeto

### 4.1 `engine` público por uma única fachada de extensão

`GerenciadorDeTurnos`, `SentidoDeRotacao`, `CicloDeVidaDaPartida`,
`PartidaEmExecucao` e `ContextoDeDistribuicaoInterno` ficam junto do motor, mas sem
`public`. Esse acoplamento é interno e esperado: todos colaboram na mesma execução.
A decisão evita chamar de API algo que o cliente não deve instanciar nem manipular e
elimina uma dependência artificial `api → core`.

### 4.2 Contextos com autoridades diferentes

`VisaoDaPartida<C>` expõe snapshots de leitura. `ContextoDePartida<C>` estende essa
visão com um conjunto limitado de operações sobre cartas. Já
`ContextoDeDistribuicao<C>` é ainda menor e permite apenas consultar jogadores,
contar cartas e entregar a próxima carta. Essa separação aplica Interface Segregation
e o princípio de menor autoridade: cada colaborador recebe somente o que precisa.

### 4.3 Diretiva de turno em vez de acesso ao gerenciador

O jogo concreto devolve `ResultadoDoTurno`, um valor declarativo que pode indicar
avanço, repetição, inversão ou pulo. Apenas o engine aplica a diretiva. Desse modo,
a regra decide **quando** um efeito ocorre e o gerenciador decide **como** alterar a
ordem, mantendo responsabilidades coesas.

### 4.4 Estado como `enum`, sem aplicar State artificialmente

`EstadoPartida` conhece o grafo de transições, enquanto `CicloDeVidaDaPartida`
mantém o valor corrente e rejeita transições ilegais. O padrão State não foi usado
porque os quatro estados não possuem comportamentos polimórficos próprios. Criar
quatro classes vazias apenas para cumprir um padrão aumentaria a complexidade sem
resolver um problema existente.

### 4.5 Valores e coleções imutáveis nas fronteiras

Configuração, vencedores, placar, listas de jogadores, mãos consultadas e cartas do
baralho são copiados defensivamente. O estado mutável existe somente durante a
execução e não vaza pela API. Isso impede que uma Strategy altere uma coleção sem
passar pelas invariantes do framework.

### 4.6 Generalização guiada por mecanismos, não por jogos

O parâmetro `C extends Carta` permite cartas diferentes sem impor naipe, cor, valor
ou pontuação. Dealer, apostas, descarte, formação de trincas e limite de 21 continuam
no cliente. Um mecanismo só deve migrar para o framework depois de demonstrar reuso
em jogos com diferenças suficientes.

## 5. Padrões, SOLID e GRASP

### Padrões de projeto

- **Template Method:** `MotorDePartida.executar()` fixa a ordem e chama operações
  primitivas e hooks protegidos. Este é o padrão principal da Trilha A.
- **Builder:** `PartidaConfig.Builder<C>` torna legível a montagem de uma
  configuração com vários colaboradores e centraliza sua validação. Ele é um
  apoio de construção e não deve substituir um dos quatro padrões GoF cobrados se
  não estiver no conjunto estudado em sala.
- **Factory Method e Strategy:** pertencem principalmente às Trilhas B/C, mas o
  motor colabora corretamente com `BaralhoFactory<C>` e
  `EstrategiaDeDistribuicao<C>` em vez de depender de implementações concretas.
- **Observer e Strategy de regras:** ainda são planejados. Interfaces vazias não
  constituem evidência de um padrão em runtime.

### SOLID

| Princípio | Evidência na Trilha A |
|---|---|
| SRP | fluxo, ciclo, turnos, agregado e valores finais têm responsabilidades distintas |
| OCP | novos jogos especializam hooks e contratos sem editar o algoritmo final |
| LSP | subclasses preservam o fluxo porque `executar()` não pode ser sobrescrito |
| ISP | visão, contexto mutável e contexto de distribuição são portas separadas |
| DIP | `engine` depende de contratos de `api`, não de Trinca ou Blackjack |

### GRASP

| Princípio | Evidência na Trilha A |
|---|---|
| Controlador | `MotorDePartida` recebe e coordena o caso de uso executar partida |
| Especialista | estado conhece transições; gerenciador conhece a rotação |
| Creator | o motor cria o agregado transitório e seus colaboradores de execução |
| Alta Coesão | decisão de regra não foi misturada com mecânica de turno |
| Baixo Acoplamento | internos sem `public` e comunicação por interfaces e valores |
| Polimorfismo | tipo de carta, motor e motivo de encerramento permanecem abertos |
| Indireção | contextos mediam o acesso ao agregado interno |
| Variações Protegidas | regras variáveis ficam atrás de hooks ou contratos |

## 6. Invariantes, exceções e robustez

A implementação valida, entre outros pontos:

- pelo menos dois jogadores e identidades lógicas únicas;
- índice inicial dentro da lista;
- fábrica, distribuição, baralho e retornos dos hooks não nulos;
- transições apenas em `CONFIGURADA → PREPARANDO → EM_ANDAMENTO → FINALIZADA`;
- identidade de carta única entre baralho e mãos;
- vencedor pertencente à partida;
- placar com exatamente todos os participantes e sem valores nulos;
- impossibilidade de mutação depois da finalização;
- impossibilidade de executar a mesma instância duas vezes.

`JogadaInvalidaException` representa uma falha recuperável: o mesmo participante
recebe nova tentativa, sem avanço do turno. O limite interno de 100 recusas evita um
laço infinito provocado por um cliente defeituoso. A implementação do hook deve
validar antes de mutar, pois o engine não realiza rollback de alterações feitas pelo
jogo concreto.

## 7. Evidência automatizada

Foi executado `./mvnw clean test`, eliminando resultados antigos antes da medição:

| Escopo | Testes | Falhas | Erros | Ignorados |
|---|---:|---:|---:|---:|
| Trilha A | 68 | 0 | 0 | 0 |
| Projeto completo | 105 | 0 | 0 | 0 |

Os testes da trilha cobrem o grafo de estados, configuração, valores finais,
diretivas, N participantes, inversão e pulos, unicidade de cartas, sequência do
Template Method, repetição de jogada inválida, encerramento inicial, validação de
vencedores/placar e bloqueio de segunda execução.

## 8. Auditoria de Javadoc

A API pública e os hooks protegidos da Trilha A foram verificados com `javadoc`
usando `-Xdoclint:all -Werror`. O resultado foi **zero avisos e zero erros**. Foram
explicitados parâmetros, retornos, exceções, estados válidos durante cada hook,
imutabilidade, execução única e ausência de rollback. Os dois colaboradores internos
que ainda tinham documentação parcial também receberam contratos de construção e
operação.

A auditoria estrita do **repositório inteiro**, porém, ainda encontra 21 avisos fora
da Trilha A:

| Origem | Avisos | Situação principal |
|---|---:|---|
| contratos/implementações da Trilha B | 7 | descrição principal ausente em alguns métodos |
| tipos da Trilha C | 4 | construtores e componentes de `record` incompletos |
| placeholders da Trilha D | 4 | tipos públicos sem comentário de classe |
| placeholders legados em `core` | 6 | classes e construtores padrão sem documentação |

Assim, o requisito de Javadoc está atendido pela Trilha A, mas ainda não pode ser
declarado concluído para toda a API pública. Esses arquivos devem ser corrigidos por
seus responsáveis sem preencher unilateralmente as interfaces ainda vazias.

## 9. Revisão do diagrama de classes

O arquivo `diagrama-classes.puml` foi comparado com as assinaturas compiladas e com a
visibilidade real dos tipos. A revisão corrigiu:

- a falsa composição simultânea do ciclo de vida por motor e partida; o motor é o
  proprietário e `PartidaEmExecucao` apenas consulta o mesmo objeto;
- a dependência da fábrica para o produto `Baralho`;
- assinaturas, tipos de parâmetro, operações estáticas e overloads relevantes;
- classes `final`, tipos package-private e caminhos reais de subpacotes;
- implementações de distribuição, decisão e entrada/saída presentes na `main`;
- relações com adaptadores transitórios de `core`;
- notas que separam recursos implementados de placeholders pendentes.

As multiplicidades centrais agora mostram configuração com dois ou mais
participantes, uma mão por participante e um único gerenciador/ciclo por execução. O
arquivo usa o layout Java ELK embutido e foi validado e renderizado pelo PlantUML
1.2024.8 sem depender de uma instalação externa do Graphviz.

O diagrama é deliberadamente simplificado: ele registra contratos e relações
arquiteturais importantes, sem listar todo construtor, método herdado ou detalhe
privado. Essa escolha é coerente com o entregável pedido pela atividade.

## 10. Limitações e próximos passos

1. Definir e integrar as Strategies de validação, vitória e pontuação da Trilha D.
2. Implementar Observer com eventos imutáveis, ordem de notificação e isolamento de
   falhas documentados e testados.
3. Decidir se as Strategies de regra substituem ou adaptam os hooks provisórios, sem
   manter duas fontes concorrentes para a mesma decisão.
4. Integrar Trinca e Blackjack como clientes que importem somente `api` e
   `engine.MotorDePartida`.
5. Adicionar testes arquiteturais automatizados para impedir dependências proibidas.
6. Concluir o Javadoc estrito dos tipos públicos pertencentes às outras trilhas.
7. Manter uma instância de motor por partida; o runtime atual é síncrono e não
   thread-safe.
8. Em caso de falha inesperada propagada por um hook, descartar a instância: ela
   conserva o estado alcançado e não é reiniciável.

## 11. Matriz dos requisitos obrigatórios

| Requisito da atividade | Situação observada |
|---|---|
| API pública definida | atendido na baseline atual |
| pelo menos cinco pontos de extensão | atendido pela composição de contratos e hooks |
| solução separada dos clientes | atendido arquiteturalmente |
| pelo menos um jogo cliente | pendente na `main` |
| interfaces e classes abstratas | atendido |
| exceções adequadas | atendido no escopo implementado |
| coleções encapsuladas | atendido |
| testes automatizados | atendido: 105 testes verdes |
| Javadoc da API pública | Trilha A atendida; repositório completo pendente |
| diagrama simplificado | atendido após a revisão |
| exemplos de utilização | pendentes com os clientes concretos |
| justificativa das decisões | atendida nos documentos arquiteturais e neste relatório |

## 12. Conclusão

A decisão central da Trilha A está correta: o framework controla o ciclo e o jogo
concreto fornece variações por contratos estreitos. O pacote `engine` representa
honestamente o runtime, e seus detalhes permanecem inacessíveis aos clientes. A
combinação de Template Method, configuração imutável, contextos segregados e
diretivas de turno produz uma base coesa e extensível sem antecipar regras de Trinca
ou Blackjack.

O próximo marco arquitetural não é adicionar mais abstrações ao motor, mas provar os
pontos existentes: integrar as regras da Trilha D e implementar dois clientes com
mecânicas suficientemente diferentes. Se ambos reutilizarem o mesmo ciclo sem exigir
condicionais por jogo no framework, a arquitetura terá demonstrado o objetivo da
atividade.
