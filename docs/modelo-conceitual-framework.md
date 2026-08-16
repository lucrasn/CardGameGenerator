# Modelo conceitual do framework

**Status:** refletido na baseline atual da `main`. Conceitos ainda sem contrato
executável são identificados como pendentes.

## 1. Critério de generalização

O framework modela conceitos reutilizáveis entre jogos de cartas diferentes. Ele não
modela regras específicas de um jogo nem tenta prever todas as mecânicas possíveis.

Pergunta usada para promover um conceito ao framework:

> Trinca e Blackjack precisam dele com o mesmo significado e o mesmo ciclo de vida?

Se a resposta for não, o conceito fica no cliente. Por isso “pilha de descarte”,
“dealer”, “bater”, “pedir carta”, “formar trinca” e “limite 21” não pertencem ao
modelo genérico.

## 2. Vocabulário estável

| Conceito | Responsabilidade genérica | Não conhece |
|---|---|---|
| Carta | possuir identidade estável | naipe, cor, valor ou efeito |
| Baralho | ordenar, embaralhar, comprar e recolocar cartas | regra de vitória |
| Mão | armazenar cartas de um participante | combinação vencedora |
| Jogador | identidade e estratégia de decisão | fluxo e pontuação da partida |
| Distribuição | entregar cartas iniciais por uma porta controlada | implementação das mãos |
| Partida | reunir participantes, baralho, mãos, estado e turnos | jogo concreto |
| Turno | dar a vez e aplicar uma diretiva de rotação | ação específica do jogo |
| Desfecho | indicar vencedores e motivo | como a condição foi detectada |
| Resultado | congelar vencedores, placar e motivo | estado mutável da execução |

## 3. Identidade

`Carta` e `Jogador` possuem `UUID`. A identidade lógica é usada para impedir que duas
instâncias representando o mesmo participante ou a mesma carta entrem duplicadas no
agregado.

O framework não depende de `equals` definido por implementações externas. Esse limite
reduz o acoplamento entre as trilhas.

## 4. Agregado da partida

Uma execução contém:

```text
PartidaEmExecucao<C>
 ├─ 1 Baralho<C>
 ├─ 2..* Jogador
 ├─ 1 mão principal por Jogador
 ├─ 1 GerenciadorDeTurnos
 └─ 1 CicloDeVidaDaPartida
```

`PartidaEmExecucao` é interna. O jogo recebe duas interfaces:

- `VisaoDaPartida<C>` para consulta;
- `ContextoDePartida<C>` para mutações permitidas durante hooks do motor.

Uma mão pública da Trilha B possui mutadores, então a visão não a devolve diretamente.
Ela entrega uma `List<C>` imutável, preservando o princípio de menor privilégio.

## 5. Ciclo de vida

```text
CONFIGURADA → PREPARANDO → EM_ANDAMENTO → FINALIZADA
```

- **CONFIGURADA:** colaboradores validados, execução ainda não iniciada;
- **PREPARANDO:** baralho, mãos, distribuição e zonas específicas são montados;
- **EM_ANDAMENTO:** turnos e avaliação podem ocorrer;
- **FINALIZADA:** resultado existe e nenhuma carta pode ser alterada.

O enum `EstadoPartida` conhece transições legais. O ciclo interno mantém o valor
corrente e comunica violações por exceção de domínio.

## 6. Configuração e execução

`PartidaConfig<C>` descreve os dados necessários para começar:

- participantes;
- fábrica de baralho;
- estratégia de distribuição;
- primeiro jogador.

`MotorDePartida<C>` interpreta essa configuração. Ele é simultaneamente:

- **Controlador GRASP**, porque recebe o evento sistêmico “executar partida”;
- **Template Method**, porque fixa a ordem e chama passos variáveis;
- **Creator**, porque cria o agregado transitório da execução.

O jogo não chama “próximo turno” nem “finalizar”. Ele devolve `ResultadoDoTurno` e o
framework conserva o controle.

## 7. Turno

Um turno genérico não é uma única `Jogada`. Jogos podem ter várias etapas e decisões.
O contrato genérico exige apenas que o hook termine com uma diretiva:

- avanço normal;
- repetição do participante;
- inversão de rotação;
- pulo de participantes.

A semântica da ação fica no cliente. A mecânica de ordem fica no engine.

## 8. Encerramento

O encerramento tem duas fases conceituais:

1. `DesfechoDePartida`: vencedores e motivo;
2. `ResultadoDePartida`: desfecho mais placar definitivo.

Separar esses valores evita que detecção de vitória e cálculo de pontuação sejam
confundidos. `MotivoDeEncerramento` é aberto para vocabulários de jogos novos.

Na baseline atual, detecção e pontuação são Strategies independentes e obrigatórias
em `PartidaConfig`. O motor consulta a regra de vitória após a distribuição e ao fim
de cada turno; quando há desfecho, consulta a regra de pontuação antes de produzir o
resultado imutável.

## 9. Decisão do jogador

`Jogador` representa identidade. `EstrategiaDeDecisao` representa comportamento.

```text
Jogador ──compõe──> EstrategiaDeDecisao
                         │
                         ├─ humana/console
                         ├─ aleatória
                         └─ gulosa
```

Essa composição permite trocar comportamento sem trocar a identidade nem criar uma
hierarquia de subclasses por perfil.

## 10. Distribuição

`EstrategiaDeDistribuicao<C>` recebe `ContextoDeDistribuicao<C>`, que oferece apenas
os participantes, a quantidade disponível e a entrega da próxima carta. Essa porta
mantém a estratégia independente de `BaralhoPadrao`, `MaoDeCartasPadrao` e do engine.

## 11. Hot-spots e frozen-spots

Hot-spots disponíveis:

- implementação de carta;
- fábrica de baralho;
- estratégia de distribuição;
- estratégia de decisão;
- subclasse de motor;
- motivo de encerramento.
- regras de validação, vitória e pontuação;
- eventos e observadores.

Frozen-spots:

- sequência de `MotorDePartida.executar()`;
- máquina de estados;
- criação do agregado;
- aplicação das diretivas de turno;
- validações estruturais de vencedores e placar;
- encerramento e bloqueio de mutações.

## 12. Teste do modelo

Trinca e Blackjack devem compartilhar frozen-spots, mas variar hot-spots. Se uma
regra dos dois for idêntica, ela pode sugerir uma nova abstração reutilizável. Se for
diferente, deve permanecer em cada cliente ou em uma Strategy. Essa comparação é o
mecanismo de validação do modelo, não uma justificativa para codificar os dois jogos
dentro do framework.
