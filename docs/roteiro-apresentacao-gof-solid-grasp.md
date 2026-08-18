# Roteiro de apresentação — GoF, SOLID, GRASP e pontos de extensão

**Equipe:** Lucas · Júlio · Allan · Lívia · Raffael  
**Framework:** baseline reutilizável da `main`  
**Clientes de prova:** Trinca (`jogo/trinca`) e Blackjack (`jogo/blackjack`)  
**Objetivo deste documento:** organizar a defesa oral; não deve ser lido palavra por
palavra durante a apresentação.

---

## 1. Mensagem central da defesa

O projeto não é “um jogo com várias classes genéricas”. É um framework que controla o
que se repete entre jogos e deixa explícito o que cada jogo precisa fornecer.

```text
Trinca / Blackjack ─────> cardgame.api
          │
          └─────────────> cardgame.engine.MotorDePartida

cardgame.engine ────────> cardgame.api
```

A prova mais forte de extensibilidade é concreta:

- a Trinca possui de dois a cinco humanos, nove cartas por pessoa, compra, descarte,
  reciclagem e vitória por combinações;
- o Blackjack possui uma pessoa e uma casa automatizada, duas cartas iniciais,
  informação oculta, repetição de turno, Ás flexível, limite 21 e empate;
- os dois clientes foram criados sem colocar `Trinca`, `Blackjack`, `Dealer`, limite
  21, pilha de descarte ou combinações dentro do framework;
- ambos dependem apenas da API pública e de `engine.MotorDePartida`.

Frase curta para abrir a apresentação:

> O framework fixa o ciclo seguro de uma partida e protege as variações de carta,
> baralho, distribuição, decisão, regras, eventos e turnos atrás de pontos de extensão.

---

## 2. Não misturar GoF, SOLID, GRASP e ponto de extensão

| Conceito | Pergunta que responde | Como provar no projeto |
|---|---|---|
| Padrão GoF | “Qual estrutura recorrente resolve este problema de projeto?” | mostrar participantes e colaboração em runtime |
| SOLID | “O design suporta mudança sem acoplamento ou contratos frágeis?” | mostrar direção das dependências e razão para mudar |
| GRASP | “Para qual objeto atribuímos cada responsabilidade?” | mostrar quem possui a informação e quem coordena |
| Ponto de extensão | “Onde um novo jogo adiciona comportamento sem editar o núcleo?” | comparar o mesmo contrato na Trinca e no Blackjack |

Exemplo da diferença:

- `EstrategiaDeDecisao` com implementações substituíveis é **Strategy GoF**;
- depender da interface, e não de uma decisão humana concreta, ajuda a cumprir **DIP**;
- atribuir a escolha variável a objetos polimórficos aplica **Polimorfismo GRASP**;
- criar `DecisaoHumanaTrincaConsole` ou `EstrategiaCasaBlackjack` é usar um **ponto de
  extensão**.

Uma mesma decisão pode ser evidência em categorias diferentes, mas a justificativa
deve mudar conforme a categoria.

---

## 3. Frozen-spots e hot-spots

### 3.1 Componentes reutilizáveis — frozen-spots

São partes que o cliente usa, mas não controla diretamente:

| Componente | Responsabilidade fixa |
|---|---|
| `MotorDePartida.executar()` | ordenar preparação, distribuição, turnos, avaliação, pontuação e encerramento |
| `CicloDeVidaDaPartida` | permitir somente transições válidas de estado |
| `GerenciadorDeTurnos` | avançar, repetir, inverter e pular participantes |
| `PartidaEmExecucao` | conservar baralho, mãos e identidades sem duplicar cartas |
| `BaralhoPadrao` | encapsular compra, topo, base e embaralhamento |
| `MaoDeCartasPadrao` | encapsular a coleção de uma mão |
| `JogadorPadrao` | compor identidade e Strategy de decisão |
| `ResultadoDePartida` | representar vencedores, placar e motivo de forma imutável |
| eventos padrão | informar os marcos comuns do ciclo de vida |

`executar()` é `final`, e todos os colaboradores do pacote `engine`, exceto
`MotorDePartida`, são package-private. Isso impede que um cliente pule etapas ou
controle turnos por fora do Template Method.

### 3.2 Pontos de extensão — hot-spots

| Ponto de extensão | O jogo concreto fornece |
|---|---|
| `Carta` | seus atributos e semântica de carta |
| `BaralhoFactory<C>` | a composição de um baralho novo |
| `EstrategiaDeDistribuicao<C>` | quantidade e ordem da distribuição |
| `Jogada` e `EtapaDeTurno` | o vocabulário de ações e fases |
| `ContextoDeDecisao` | a informação pública autorizada para decidir |
| `EstrategiaDeDecisao` | comportamento humano ou automatizado |
| `RegraDeValidacaoStrategy<C>` | o que constitui uma ação legal |
| `RegraDeVitoriaStrategy<C>` | quando a partida acaba e quem venceu |
| `RegraDePontuacaoStrategy<C>` | como o desfecho vira placar |
| `MotivoDeEncerramento` | vocabulário próprio de desfechos |
| `MotorDePartida<C>` | o passo variável de um turno |
| `EventoDePartida` | fatos específicos publicados pelo jogo |
| hooks opcionais | preparação antes/depois da distribuição e reação ao encerramento |

O requisito de pelo menos cinco pontos de extensão é superado sem contar variações
artificiais.

---

## 4. Padrões GoF implementados

Os quatro padrões usados para cumprir o requisito são **Template Method, Factory
Method, Strategy e Observer**. Builder existe como apoio, mas não entra na contagem dos
quatro padrões cobrados.

### 4.1 Mapa rápido

| GoF | Problema resolvido | Participantes no framework | Evidência nos clientes | Defesa principal |
|---|---|---|---|---|
| Template Method | conservar a ordem global permitindo passos variáveis | `MotorDePartida.executar()`, hooks e `executarTurno()` | `MotorTrinca` e `MotorBlackjack` | Lucas |
| Factory Method | criar baralhos sem o engine conhecer a composição | `BaralhoFactory.criar()` e `Baralho<C>` | `BaralhoTrincaFactory` e `BaralhoBlackjackFactory` | Júlio |
| Strategy | substituir algoritmos sem editar o consumidor | interfaces de distribuição, decisão e regras | decisões humanas/casa e regras dos dois jogos | Júlio, Allan e Lívia |
| Observer | reagir a eventos sem acoplar console ao motor | `PartidaListener`, `EventoDePartida`, cadastro/publicação | eventos padrão na Trinca e eventos próprios do Blackjack | Lívia |

### 4.2 Template Method

Participantes:

| Papel do padrão | Implementação |
|---|---|
| Abstract Class | `MotorDePartida<C>` |
| Template Method | `executar()`, público e `final` |
| operação primitiva | `executarTurno(ContextoDePartida<C>)` |
| hooks opcionais | `preparar`, `aposDistribuir`, `aoEncerrar` |
| Concrete Classes | `MotorTrinca` e `MotorBlackjack` |

Fluxo fixo:

```text
criar baralho → preparar → distribuir → executar turno
→ avaliar vitória → aplicar diretiva → pontuar → finalizar
```

Variações concretas:

- Trinca: comprar do monte/descarte, descartar e devolver `avancar()`;
- Blackjack: `PEDIR` compra e devolve `repetir()`; `PARAR` devolve `avancar()`;
- nenhum motor concreto chama “próximo jogador” nem finaliza a partida diretamente.

Por que usar: a ordem contém invariantes críticas e não deve ser copiada em cada jogo.

### 4.3 Factory Method

Participantes:

| Papel do padrão | Implementação |
|---|---|
| contrato criador | `BaralhoFactory<C>` |
| método fábrica | `criar()` |
| produto | `Baralho<C>` |
| produto concreto reutilizável | `BaralhoPadrao<C>` |
| consumidor | `MotorDePartida<C>` |

Variações concretas:

- `BaralhoTrincaFactory` cria cartas `CartaTrinca`;
- `BaralhoBlackjackFactory` cria cartas `CartaBlackjack`;
- ambos usam 52 cartas francesas hoje, mas a semântica de valor pertence ao cliente;
- o engine apenas solicita um `Baralho<C>` e o embaralha.

Por que usar: o motor precisa do produto, mas não deve conhecer valores, naipes,
curingas ou quantidade.

### 4.4 Strategy

Strategy aparece em três eixos independentes:

| Eixo | Interface | Implementações ou usos |
|---|---|---|
| distribuição | `EstrategiaDeDistribuicao<C>` | `DistribuicaoAlternada`: nove cartas na Trinca e duas no Blackjack |
| decisão | `EstrategiaDeDecisao` | humanos da Trinca, humano do Blackjack e `EstrategiaCasaBlackjack` |
| regras | validação, vitória e pontuação | três regras concretas para cada jogo |

Por que não usar um único `if (tipoDoJogo)`:

- os algoritmos mudam por motivos diferentes;
- uma decisão humana pode ser trocada por bot sem trocar o jogador;
- uma regra de vitória pode ser testada isoladamente;
- o engine permanece fechado para alterações de jogos concretos.

### 4.5 Observer

Participantes:

| Papel do padrão | Implementação |
|---|---|
| Subject | `MotorDePartida<C>` |
| Observer | `PartidaListener` |
| notificações | `EventoDePartida`, eventos padrão e eventos dos clientes |

Evidências:

- o motor publica início/fim de partida e turnos, distribuição e rejeições;
- o console da Trinca observa `JogadaRejeitada` sem entrar no engine;
- o Blackjack publica `MaoDaCasaReveladaBlackjack`, `CartaPedidaBlackjack` e
  `ParticipanteParouBlackjack` pelo mesmo mecanismo;
- falha de um listener é isolada e não interrompe a partida;
- eventos são imutáveis e a carta fechada só é publicada quando deixa de ser secreta.

Por que usar: console, histórico ou outra interface podem reagir sem virar dependência
do motor.

### 4.6 Builder — apoio, não um dos quatro exigidos

`PartidaConfig.Builder<C>` evita um construtor posicional longo e valida todos os
colaboradores antes da execução. É correto chamá-lo de Builder, mas a equipe deve dizer
explicitamente que a contagem obrigatória já é satisfeita pelos quatro padrões acima.

### 4.7 Padrões que não devem ser reivindicados

| Padrão | Decisão |
|---|---|
| State | não usado: os estados restringem transições, mas não têm comportamento polimórfico próprio |
| Decorator | não usado: ainda não há combinações diferentes de validações reutilizáveis que o justifiquem |
| Singleton | rejeitado: impediria partidas independentes e atrapalharia os testes |
| Abstract Factory | não usado: o ponto atual cria um produto principal, `Baralho<C>` |

Isso demonstra cuidado contra overengineering.

---

## 5. Princípios SOLID

### 5.1 Matriz de evidências

| Princípio | Evidência no framework | Prova com Trinca e Blackjack | Quem enfatiza |
|---|---|---|---|
| SRP | ciclo, turnos, estado, agregado, decisão e regras estão em tipos separados | `CombinacoesTrinca` calcula combinações; `PontuacaoDaMaoBlackjack` calcula o Ás; motores apenas orquestram o turno concreto | todos, com exemplo da própria trilha |
| OCP | interfaces públicas recebem novas implementações | dois clientes novos foram adicionados sem condicionais no engine | Raffael |
| LSP | `executar()` final conserva o contrato das subclasses; Strategies respeitam os mesmos retornos | `MotorTrinca` e `MotorBlackjack` terminam pelo mesmo Template Method | Lucas |
| ISP | contextos separam leitura, mutação controlada, distribuição e decisão | decisão humana do Blackjack não recebe a carta fechada; regra de vitória não recebe mutadores | Júlio, Allan e Lívia |
| DIP | engine depende das abstrações de `api`, não dos clientes | não há import de Trinca/Blackjack em `api` ou `engine`; ArchUnit falha o build se aparecer | Lucas e Raffael |

### 5.2 SRP — Princípio da Responsabilidade Única

Cada classe possui um motivo principal para mudar:

- `MotorDePartida`: mudança no fluxo comum;
- `GerenciadorDeTurnos`: mudança na rotação;
- `CicloDeVidaDaPartida`: mudança nas transições;
- `BaralhoPadrao`: mudança no armazenamento do baralho;
- `JogadorPadrao`: mudança na identidade/composição da decisão;
- Strategies de regra: mudança na política correspondente;
- `CombinacoesTrinca`: mudança na definição de combinações;
- `PontuacaoDaMaoBlackjack`: mudança no cálculo do Ás e do total;
- apresentadores/telas: mudança visual do console.

Não dizer “cada classe faz uma única operação”. SRP trata de **razão para mudar**.

### 5.3 OCP — Aberto para extensão, fechado para modificação

Esta é a principal prova empírica do trabalho:

- Trinca adiciona cartas, ações, regras, mesa e motor no pacote do cliente;
- Blackjack adiciona outras cartas, ações, regras, eventos e motor;
- nenhum dos dois exige editar condicionais no `MotorDePartida`;
- `MotivoDeEncerramento` ser interface permite `MotivoBlackjack` sem ampliar um enum
  central a cada jogo.

### 5.4 LSP — Substituição de Liskov

Os motores concretos preservam o contrato da classe abstrata:

- não sobrescrevem `executar()`;
- validam antes de mutar;
- devolvem `ResultadoDoTurno` não nulo;
- não indicam vencedor externo à partida;
- produzem placar para exatamente todos os participantes.

O `final` do Template Method reduz o espaço em que uma subclasse poderia quebrar as
invariantes.

### 5.5 ISP — Segregação de Interfaces

O projeto não entrega um “contexto universal” a todos:

- `VisaoDaPartida` permite somente consulta;
- `ContextoDePartida` acrescenta mutações controladas para o motor concreto;
- `ContextoDeDistribuicao` permite apenas entregar a próxima carta;
- `ContextoDeValidacao` reúne jogada e visão sem mutadores;
- `ContextoDeDecisao` informa somente o necessário para escolher.

No Blackjack, a própria Strategy humana recebe uma lista que não contém a carta
fechada da casa. O sigilo é estrutural, não uma convenção da tela.

### 5.6 DIP — Inversão de Dependência

O fluxo de dependência é:

```text
engine → contratos de api ← implementações dos clientes
```

O motor depende de `BaralhoFactory`, Strategies, contextos e valores abstratos. Ele não
depende de `CartaTrinca`, `EstrategiaCasaBlackjack` ou regras concretas. Os clientes
fornecem essas colaborações por `PartidaConfig`.

---

## 6. Princípios GRASP

| GRASP | Responsabilidade atribuída | Evidência concreta |
|---|---|---|
| Controller | coordenar o caso de uso “executar partida” | `MotorDePartida` recebe a execução e coordena colaboradores |
| Information Expert | comportamento fica com quem possui a informação necessária | turnos no `GerenciadorDeTurnos`; combinações em `CombinacoesTrinca`; total em `PontuacaoDaMaoBlackjack` |
| Creator | criar objetos que agrega ou configura | motor cria a execução; factories criam baralhos; aplicações montam jogadores/configuração |
| Low Coupling | reduzir conhecimento entre módulos | clientes veem `api` e somente o motor público; internals são package-private |
| High Cohesion | manter responsabilidades relacionadas juntas | `api`, `engine`, Trinca e Blackjack possuem fronteiras próprias |
| Polymorphism | tratar variações pelo mesmo contrato | cartas, factories, distribuição, decisão, regras, motivos e motores |
| Indirection | inserir um mediador para evitar acesso direto | contextos mediam mãos/baralho; Observer medeia motor e console |
| Protected Variations | estabilizar pontos sujeitos a mudança | interfaces cercam baralho, decisões, regras, eventos e motivos |
| Pure Fabrication | criar um serviço técnico sem forçá-lo em entidade de domínio | `ControleEntradaSaida`, `TelaTerminal` e `ApresentadorBlackjackConsole` isolam I/O/formatação |

### 6.1 Como explicar os GRASP mais cobrados

**Controller:** `MotorDePartida` coordena; ele não precisa calcular pontos, decidir a
jogada ou saber o valor de uma carta.

**Information Expert:** o especialista não é sempre o motor. O total do Blackjack fica
com `PontuacaoDaMaoBlackjack`, pois esse tipo conhece as cartas e o tratamento dos ases.
A partição da Trinca fica com `CombinacoesTrinca`, não com `Jogador`.

**Low Coupling e High Cohesion:** são avaliados juntos. Extrair classes sem reduzir
dependências não basta. Aqui os packages têm direções verificadas por ArchUnit e cada
grupo concentra responsabilidades relacionadas.

**Protected Variations:** carta, distribuição, decisão e regras são os eixos que mais
mudam entre jogos. Por isso ficam atrás de contratos públicos estáveis.

---

## 7. Comparação dos pontos de extensão nos dois clientes

| Contrato/ponto | Trinca — `jogo/trinca` | Blackjack — `jogo/blackjack` | Responsável pela trilha |
|---|---|---|---|
| `Carta` | `CartaTrinca`, `Valor`, `Naipe` | `CartaBlackjack`, `ValorBlackjack`, `NaipeBlackjack` | Júlio / Trilha B |
| `BaralhoFactory` | `BaralhoTrincaFactory` | `BaralhoBlackjackFactory` | Júlio / Trilha B |
| distribuição | `DistribuicaoAlternada<>(9)` | `DistribuicaoAlternada<>(2)` | Júlio / Trilha B |
| `Jogada` | comprar do monte/descarte e descartar | `AcaoBlackjack.PEDIR` e `PARAR` | Allan / Trilha C |
| `EtapaDeTurno` | compra e descarte | decisão de pedir/parar | Allan / Trilha C |
| contexto de decisão | mão, descarte e última compra | própria mão e parcela pública da casa | Allan / Trilha C |
| decisão humana | `DecisaoHumanaTrincaConsole` | `DecisaoHumanaBlackjackConsole` | Allan / Trilha C |
| decisão automática | não é necessária no cliente principal | `EstrategiaCasaBlackjack` | Allan / Trilha C |
| validação | tamanhos 9/10, topo e posse da carta | ação tipada, total menor que 21 e baralho disponível | Lívia / Trilha D |
| vitória | mão de nove cartas totalmente combinada | natural, estouro, maior total ou empate | Lívia / Trilha D |
| pontuação | um ponto para quem formar a mão | um ponto para vencedor e zero no empate | Lívia / Trilha D |
| motivo | `MotivoPadrao.VITORIA` | `MotivoBlackjack` com motivos específicos | Lívia e Lucas / Trilhas D/A |
| eventos | observa eventos padrão, como rejeição | publica revelação, compra e parada próprias | Lívia / Trilha D |
| motor concreto | compra + descarte e sempre avança | pedir repete; parar avança | Lucas / Trilha A |
| estado específico | `MesaTrinca` mantém descarte | `MesaBlackjack` mantém papéis, paradas e última compra | Raffael / Trilha E |
| aplicação | 2–5 humanos, privacidade entre turnos | pessoa contra casa automatizada | Raffael / Trilha E |

Observação importante de autoria: Raffael é o responsável oficial pela integração dos
clientes da Trilha E. Os demais integrantes apresentam **como os contratos que
implementaram no framework são usados pelos clientes**. Essa divisão demonstra trabalho
integrado sem apagar a propriedade de cada trilha.

---

## 8. Divisão da apresentação por integrante

### 8.1 Raffael — problema, arquitetura e integração dos clientes

#### O que implementou/possui

- Trilha E: clientes, integração, UML, relatório e testes de aceitação;
- especificação e requisitos da Trinca;
- montagem das aplicações concretas em packages separados.

#### O que mostrar

1. diagrama de dependências `cliente → api/engine`;
2. diferença entre componentes reutilizáveis e pontos de extensão;
3. `PartidaConfig` de um cliente, mostrando todas as colaborações montadas;
4. packages separados e branches `jogo/trinca` e `jogo/blackjack`;
5. comparação funcional entre os dois jogos e demonstração no console.

#### Conceitos que deve relacionar

- OCP: o segundo cliente foi adicionado sem alterar o engine;
- Low Coupling e High Cohesion;
- Creator: aplicação monta participantes e motor;
- separação obrigatória entre framework e aplicações clientes.

#### Fala sugerida

> Nossa aplicação cliente não acessa estado interno nem controla a ordem da partida.
> Ela escolhe implementações para os hot-spots públicos e chama `executar()` uma vez.
> Trinca e Blackjack usam a mesma biblioteca, mas possuem fluxos, decisões e regras
> suficientemente diferentes para provar que o framework não está codificado para um
> único jogo.

---

### 8.2 Lucas — motor, ciclo de vida e Template Method

#### O que implementou/possui

- `MotorDePartida`, `PartidaConfig`, contextos de partida e visão;
- máquina de estados, agregado em execução e gerenciamento interno de turnos;
- resultado, desfecho, motivos comuns e diretivas de turno.

#### O que mostrar nos clientes

| Trinca | Blackjack |
|---|---|
| `MotorTrinca` implementa compra e descarte | `MotorBlackjack` implementa pedir e parar |
| um turno válido termina com `avancar()` | pedir usa `repetir()`; parar usa `avancar()` |
| `aposDistribuir` cria o topo do descarte | avaliação inicial detecta Blackjack natural |

#### Conceitos que deve relacionar

- Template Method e Inversão de Controle;
- LSP: subclasses não podem substituir `executar()`;
- Controller GRASP;
- SRP entre motor, ciclo de vida, turnos e agregado;
- DIP: engine consome os contratos da API.

#### Fala sugerida

> O cliente não chama preparar, distribuir, avaliar ou avançar turnos. Ele chama
> `executar()` e o framework chama seus pontos de extensão. Essa inversão de controle é
> o centro do framework. A Trinca e o Blackjack só descrevem o que acontece em um
> turno; o motor continua responsável pela sequência e pelas invariantes.

#### Pergunta provável

**Por que `GerenciadorDeTurnos` não é público?** Porque o jogo escolhe uma diretiva,
mas somente o controlador interno deve aplicá-la. Expor o gerenciador permitiria burlar
o Template Method.

---

### 8.3 Júlio — cartas, baralho, mãos e distribuição

#### O que implementou/possui

- `Carta`, `Baralho`, `BaralhoPadrao` e `BaralhoFactory`;
- `MaoDeCartas` e implementação padrão;
- contexto e Strategy de distribuição;
- encapsulamento, identidade UUID e snapshots imutáveis.

#### O que mostrar nos clientes

| Trinca | Blackjack |
|---|---|
| `CartaTrinca` possui valor/naipe para combinações | `CartaBlackjack` fornece valores usados no total 21 |
| factory cria o baralho do cliente | outra factory cria cartas de outro tipo genérico |
| distribuição alternada de nove | distribuição alternada de duas |

Mesmo usando hoje a mesma composição francesa de 52 cartas, os tipos não foram
colocados no framework: outros jogos podem usar cores, efeitos, curingas ou cartas sem
naipe.

#### Conceitos que deve relacionar

- Factory Method;
- Strategy de distribuição;
- ISP: distribuição recebe apenas `ContextoDeDistribuicao`;
- Information Expert e Protected Variations;
- encapsulamento de coleções e conservação de identidades.

#### Fala sugerida

> O framework sabe que uma carta tem identidade, mas não impõe valor ou naipe. A
> factory do cliente decide a composição e a Strategy decide a distribuição. Assim, o
> motor manipula `C` genericamente e não contém nenhuma condição específica para
> baralho francês.

#### Pergunta provável

**Por que `Carta` exige UUID?** Para conservar a identidade física da carta e impedir
que duas cartas visualmente iguais sejam tratadas como o mesmo objeto no baralho e nas
mãos.

---

### 8.4 Allan — jogadores, ações e Strategies de decisão

#### O que implementou/possui

- `Jogador` e `JogadorPadrao`;
- `Jogada`, `EtapaDeTurno`, `ContextoDeDecisao` e Strategy de decisão;
- decisões reutilizáveis e abstração `EntradaSaida`;
- composição entre identidade e comportamento.

#### O que mostrar nos clientes

| Trinca | Blackjack |
|---|---|
| 2–5 jogadores usam decisão humana | a pessoa usa decisão humana e a casa usa Strategy automática |
| ações: comprar e descartar | ações: `PEDIR` e `PARAR` |
| contexto informa mão e descarte | contexto esconde estruturalmente a carta fechada |
| etapas separadas de compra/descarte | etapa única de decisão |

#### Conceitos que deve relacionar

- Strategy de decisão;
- composição em lugar de subclasses `JogadorHumano`/`Dealer`;
- OCP e Polimorfismo GRASP;
- ISP e DIP por meio de `ContextoDeDecisao` e `EntradaSaida`;
- testabilidade pela injeção da porta de I/O.

#### Fala sugerida

> Humano, bot e casa não são novas subclasses de jogador. `JogadorPadrao` mantém a
> identidade e recebe uma Strategy substituível. A diferença entre a Trinca e o
> Blackjack está nas ações e nos contextos concretos, não no contrato base. No
> Blackjack, nem a Strategy humana consegue acessar a carta fechada.

#### Pergunta provável

**Por que não existe `Dealer` no framework?** Porque “casa” é um papel específico do
Blackjack. O mecanismo reutilizável é participante com Strategy automatizada.

---

### 8.5 Lívia — regras, exceções e Observer

#### O que implementou/possui

- Strategies de validação, vitória e pontuação;
- contexto somente leitura de validação;
- protocolo `EventoDePartida`/`PartidaListener` e eventos padrão;
- exceções de domínio.

#### O que mostrar nos clientes

| Trinca | Blackjack |
|---|---|
| valida compra/descarte e tamanhos 9/10 | valida pedir/parar, total e disponibilidade |
| vence por partição em combinações | vence por natural, estouro ou maior total |
| não possui empate na variante adotada | possui empate explícito |
| usa eventos padrão/rejeição | adiciona três eventos específicos |

#### Conceitos que deve relacionar

- Strategy de regras;
- Observer;
- SRP entre validar, reconhecer desfecho e pontuar;
- ISP: regras recebem visão, não operações mutáveis;
- OCP por `MotivoDeEncerramento` e `EventoDePartida` abertos.

#### Fala sugerida

> Validação, vitória e pontuação variam independentemente, por isso são três
> Strategies, não um objeto de regras monolítico. O Observer também é extensível: o
> framework publica eventos comuns e o Blackjack acrescenta fatos próprios usando a
> mesma infraestrutura protegida do motor.

#### Pergunta provável

**Por que vitória não é mais um hook do motor?** Como Strategy, a regra pode ser
substituída e testada sem criar outra subclasse do motor. O Template Method continua
orquestrando quando ela será consultada.

---

## 9. Ordem sugerida para uma apresentação de 15 minutos

| Tempo | Pessoa | Conteúdo |
|---:|---|---|
| 0:00–1:30 | Raffael | problema, promessa honesta e arquitetura de packages |
| 1:30–4:00 | Lucas | Template Method, IoC, ciclo e diretivas comparadas |
| 4:00–6:00 | Júlio | carta genérica, Factory Method e distribuição 9 × 2 |
| 6:00–8:00 | Allan | Strategy de decisão, humano × casa e informação oculta |
| 8:00–10:00 | Lívia | Strategies de regras, exceções e Observer extensível |
| 10:00–11:30 | Raffael | SOLID/GRASP como síntese e tabela de pontos de extensão |
| 11:30–14:30 | equipe | demonstração curta de Trinca e Blackjack |
| 14:30–15:00 | Raffael | conclusão: segundo cliente sem alteração do framework |

Se houver mais tempo, cada integrante pode explicar um teste da própria trilha. Se o
tempo diminuir, preservem a comparação entre os dois clientes; ela é a evidência mais
forte do projeto.

---

## 10. Roteiro da demonstração

### 10.1 Preparação

- deixar as duas branches prontas em diretórios/terminais diferentes;
- testar antes da defesa com `./mvnw test`;
- confirmar JDK 26 e suporte a ANSI;
- aumentar a fonte do terminal;
- não depender de troca de branch ao vivo;
- deixar o diagrama de classes aberto.

### 10.2 Trinca

```bash
./mvnw compile && java -cp target/classes br.edu.uepb.map.trinca.AplicacaoTrinca
```

Mostrar rapidamente:

1. quantidade variável de participantes;
2. separação visual e privacidade dos turnos;
3. escolha entre monte e descarte;
4. ordenação da mão e agrupamento de combinações;
5. descarte e avanço normal do turno.

### 10.3 Blackjack

```bash
./mvnw compile && java -cp target/classes br.edu.uepb.map.blackjack.AplicacaoBlackjack
```

Mostrar rapidamente:

1. pessoa contra Strategy da casa;
2. carta fechada não revelada durante a decisão humana;
3. `PEDIR` repetindo o participante;
4. destaque da carta comprada;
5. revelação e execução automática da casa;
6. resultado, motivo e possibilidade de nova rodada.

### 10.4 O que dizer ao terminar

> Vimos duas aplicações com quantidade de jogadores, fluxo de turno, informação
> pública, regras de vitória e possibilidade de empate diferentes. O código comum não
> foi copiado entre elas e o engine não conhece nenhuma dessas regras.

---

## 11. Perguntas prováveis da professora

### “O framework realmente gera qualquer jogo de cartas?”

Não literalmente. O alvo são jogos com baralho compartilhado, participantes com mãos,
turnos controlados e uma condição de encerramento. Persistência, concorrência, entrada
de jogadores durante a partida e múltiplas mãos por jogador não fazem parte desta
versão. A promessa limitada é uma decisão arquitetural honesta.

### “Onde está a Inversão de Controle?”

O cliente chama `MotorDePartida.executar()` uma vez. Depois, o framework chama factory,
distribuição, hooks, motor concreto e regras na ordem definida pelo Template Method.

### “Qual é a prova de OCP?”

Trinca e Blackjack foram acrescentados em packages/branches de clientes sem modificar
o ciclo do engine. ArchUnit protege essa direção de dependências.

### “Strategy e Template Method não resolvem a mesma coisa?”

Não. Template Method varia passos por herança preservando um esqueleto. Strategy troca
algoritmos por composição. O motor concreto varia o turno; decisão, distribuição e
regras variam como objetos configurados.

### “Por que três Strategies de regras?”

Validar uma ação, reconhecer o fim e calcular o placar possuem razões diferentes para
mudar. Separá-las aumenta coesão e permite testes/substituições independentes.

### “Por que o engine está em outro pacote se usa a API?”

`api` define contratos estáveis; `engine` implementa o runtime. A dependência correta é
`engine → api`. O cliente consome a API e estende somente o ponto público do engine.

### “Como as coleções internas são protegidas?”

Baralho e mãos não são devolvidos como coleções mutáveis. Visões, resultados, eventos
e contextos usam snapshots imutáveis; mutações passam por portas controladas.

### “Por que não usaram State, Decorator ou Singleton?”

Porque não havia problema concreto que justificasse esses padrões. Quatro padrões GoF
já aparecem em runtime e possuem testes. Adicionar outros apenas para contagem seria
overengineering.

### “Quais são os cinco pontos de extensão obrigatórios?”

Podem ser citados, no mínimo: carta, factory de baralho, distribuição, decisão,
validação, vitória, pontuação e motor concreto. O projeto ainda abre ações, etapas,
contextos, motivos e eventos próprios.

---

## 12. Checklist final da equipe

- [ ] todos conseguem diferenciar GoF, SOLID e GRASP;
- [ ] cada pessoa sabe indicar classes da própria trilha no UML;
- [ ] cada pessoa compara seu ponto de extensão na Trinca e no Blackjack;
- [ ] a equipe chama apenas Template Method, Factory Method, Strategy e Observer de
      quatro GoF obrigatórios;
- [ ] Builder é apresentado somente como apoio;
- [ ] ninguém afirma que State ou Decorator já estão implementados;
- [ ] a demonstração usa duas branches/diretórios já preparados;
- [ ] `./mvnw test` e `./mvnw javadoc:javadoc` passam antes da defesa;
- [ ] a limitação do escopo é explicada sem prometer “qualquer jogo concebível”;
- [ ] a conclusão reforça que o Blackjack não exigiu mudanças no framework.

---

## 13. Resumo de uma frase por integrante

- **Raffael:** “Dois clientes diferentes foram montados apenas pelos hot-spots
  públicos, provando separação e OCP.”
- **Lucas:** “O Template Method controla o ciclo e os motores concretos variam somente
  o turno, devolvendo diretivas.”
- **Júlio:** “Cartas, composição do baralho e distribuição variam sem o engine conhecer
  valor, naipe ou quantidade.”
- **Allan:** “Identidade e decisão são compostas; a mesma abstração atende humanos e a
  casa automatizada com contextos seguros.”
- **Lívia:** “Regras independentes e eventos extensíveis variam por Strategy e Observer
  sem acoplar o motor aos jogos.”
