# Modelo conceitual — CardGame Framework

**Status:** consolidado e refletido na baseline da branch local `trilha/a-motor`;
integração do código à `main` pendente

**Nível:** responsabilidades e relações; assinaturas ficam na especificação Java

## 1. Propósito

O framework fornece conceitos reutilizáveis para partidas de cartas. Trinca e
Blackjack validam a arquitetura, mas nenhum deles define sozinho o domínio comum.

Uma responsabilidade pertence ao framework quando:

1. aparece em jogos diferentes;
2. pode ser descrita sem termos de um jogo específico;
3. precisa ser usada ou estendida por um cliente;
4. preserva baixo acoplamento e alta coesão.

Pilha de descarte, dealer, apostas, combinações, naipe e valor permanecem nos jogos.

## 2. Conceitos principais

| Conceito | Responsabilidade | Dados/operações conceituais | Não deve conhecer |
|---|---|---|---|
| Carta | representar uma carta individual | identidade estável e atributos definidos pelo cliente | mão, turno ou vitória |
| Baralho | guardar e fornecer cartas | comprar, adicionar, embaralhar e consultar quantidade | jogadores ou regras |
| Mão | expor as cartas controladas pela partida | consulta imutável; mutação mediada pelo engine | combinações ou valor |
| Jogador | identificar um participante e, opcionalmente, compor sua decisão | id, nome e Strategy | estado mutável da partida |
| Partida | representar uma execução configurada | ciclo de vida, participantes, baralho, turnos e resultado | regra concreta de um jogo |
| Jogada | representar uma ação tipada | dados imutáveis definidos pelo cliente | interpretação genérica obrigatória |
| Regra | variar validação, vitória e pontuação | recebe visões controladas e produz decisão/resultado | internals do engine |
| Evento | representar um fato ocorrido | payload imutável adequado ao observador | console ou GUI específicos |

`RegraDoJogo` é um nome conceitual, não uma interface monolítica. O código a decompõe
em três contratos menores para cumprir o ISP: validar, reconhecer o desfecho e calcular
o placar.

## 3. Participante, decisão e mão

`Jogador` é a identidade usada pelo engine. A estratégia de decisão é composição:
`JogadorPadrao` pode recebê-la e trocá-la, sem subclasses “humano”, “bot” ou “dealer”.
Também é possível criar um participante sem estratégia quando a jogada chega por outra
fronteira; tentar pedir decisão nesse estado falha explicitamente.

A mão não é propriedade mutável do objeto `Jogador`. Ela pertence à execução da
partida, pois o mesmo participante pode jogar partidas diferentes. A baseline mantém
uma mão principal por jogador e expõe apenas `MaoDeCartas` somente leitura.

## 4. Configuração e execução

`PartidaConfig` representa colaborações imutáveis:

- jogadores;
- fábrica de baralho;
- distribuição;
- validação;
- vitória;
- pontuação;
- primeiro jogador.

`MotorDePartida` interpreta a configuração. Uma execução cria um agregado interno
contendo baralho, mãos, ciclo de vida e gerenciador de turnos. Esse agregado não é
exposto ao cliente; ele é acessado por contextos públicos com capacidades limitadas.

## 5. Contextos e visões

| Contrato | Quem consome | Capacidade |
|---|---|---|
| `VisaoDaPartida` | vitória e pontuação | estado, jogadores, atual, mãos e quantidade no baralho |
| `ContextoDeDistribuicao` | Strategy de distribuição | comprar e entregar cartas durante a preparação |
| `ContextoDePartida` | motor concreto | mover cartas, validar jogada, embaralhar e publicar evento |
| `ContextoDeValidacao` | Strategy de validação | visão da partida + jogada pretendida |
| `ContextoDeDecisao` | Strategy humana/automática | etapa + lista imutável de ações permitidas |

O contexto de partida não permite avançar a vez ou finalizar diretamente. O jogo
solicita essas decisões por valores (`ResultadoDoTurno` e `DesfechoDePartida`), e o
engine conserva o controle.

## 6. Ciclo de vida

```text
CONFIGURADA → PREPARANDO → EM_ANDAMENTO → FINALIZADA
```

`EstadoPartida` é especialista na tabela de transições. `MotorDePartida.executar()`
coordena a sequência, e `GerenciadorDeTurnos` é especialista na rotação, no sentido e
nos pulos. Nenhum jogo cliente manipula esses colaboradores internos.

## 7. Resultado do turno e resultado final

`ResultadoDoTurno` é uma diretiva imutável:

- avançar normalmente;
- repetir o jogador atual;
- inverter o sentido;
- pular uma quantidade de participantes.

`DesfechoDePartida` é a conclusão preliminar da regra de vitória: vencedores e motivo.
Depois, a regra de pontuação calcula o placar e o engine cria `ResultadoDePartida`.

`MotivoDeEncerramento` é interface aberta. `MotivoPadrao` oferece vitória, empate e
esgotamento, mas um cliente pode declarar outro motivo sem editar o framework.

## 8. Variações e pontos de extensão

| Variação | Contrato |
|---|---|
| tipo de carta | `Carta` |
| composição do baralho | `BaralhoFactory` |
| distribuição | `EstrategiaDeDistribuicao` |
| tomada de decisão | `EstrategiaDeDecisao` |
| ações/fases | `Jogada`, `EtapaDeTurno` |
| validação | `RegraDeValidacaoStrategy` |
| vitória | `RegraDeVitoriaStrategy` |
| pontuação | `RegraDePontuacaoStrategy` |
| observação | `EventoDePartida`, `PartidaListener` |
| mecânica do turno | `MotorDePartida.executarTurno` |

## 9. Fronteira física

### API

Contratos, valores e implementações públicas reutilizáveis ficam em `cardgame.api` e
seus subpacotes.

### Engine

O runtime fica em `cardgame.engine`. `MotorDePartida` é público porque é o ponto de
extensão. Turnos, ciclo de vida, estado mutável e contexto concreto são package-private.

### Clientes

Trinca e Blackjack dependem de `api` e de `engine.MotorDePartida`. O framework nunca
depende deles.

## 10. Agregados e relações

- uma configuração possui ao menos dois jogadores;
- uma execução possui um baralho e uma mão principal para cada jogador;
- baralho e mãos contêm zero ou mais cartas;
- um motor agrega zero ou mais listeners;
- um jogador pode ter zero ou uma estratégia configurada;
- regras e distribuição são colaborações únicas da configuração;
- um resultado contém todos os participantes no placar e zero ou mais vencedores,
  conforme o motivo.

## 11. Critério para evoluir o modelo

Uma nova abstração só entra quando dois clientes independentes demonstrarem a mesma
lacuna. Primeiro escreve-se o cenário de cliente; depois o contrato mínimo; por fim,
atualizam-se testes, Javadoc, UML e especificação. Esse processo evita que o framework
seja enviesado para Trinca ou Blackjack.
