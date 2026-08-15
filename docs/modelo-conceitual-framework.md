# Modelo conceitual — CardGame Framework

**Status:** base arquitetural para discussão; não é ainda uma especificação de
assinaturas Java.

## 1. Propósito e regra de decisão

O framework fornece infraestrutura reutilizável para partidas de cartas. Trinca e
Blackjack são aplicações clientes que a validam; nenhuma característica particular de
um deles define, sozinha, um conceito do framework.

Antes de incluir uma abstração ou operação pública, a equipe deve responder:

1. ela representa um conceito presente em mais de um jogo de cartas?
2. um jogo novo precisará conhecê-la para usar ou estender o framework?
3. sua responsabilidade pode ser expressa sem termos como descarte, dealer,
   combinação, aposta, naipe ou valor?

Se a resposta for negativa, a responsabilidade pertence ao pacote do jogo cliente.

## 2. Abstrações mínimas

| Abstração | Responsabilidade | Dados/operações esperados | Não deve conhecer |
|---|---|---|---|
| `Carta` | Representar uma carta individual, inclusive quando há cartas visualmente iguais. | Identidade estável. Atributos específicos pertencem às cartas concretas. | Naipes, valores, regras, mão, turno ou vitória. |
| `Baralho` | Guardar e fornecer uma sequência de cartas. | Quantidade, vazio, comprar, adicionar e embaralhar; coleções sempre encapsuladas. | Jogadores, regras de jogo, descarte ou placar. |
| `MaoDeCartas` | Agrupar cartas controladas por uma partida. | Consultar, adicionar e remover cartas sem expor a coleção mutável. | Regras de combinação, valor da mão e decisão do jogador. |
| `Jogador` | Representar a identidade de um participante. | Identificador, nome e, quando aplicável, estratégia de decisão por composição. | Regras concretas, console, dealer ou estado mutável de mesa. |
| `Partida` | Representar o ciclo de vida e a configuração de uma execução. | Jogadores, estado, turnos, baralho, resultado e eventos por contratos controlados. | Regras ou ações de um jogo específico. |
| `RegraDoJogo` | Nome conceitual para comportamentos que determinam um jogo. | É decomposta em validação, vitória e pontuação para evitar interface ampla. | Estruturas internas mutáveis do `core`. |

`Carta` deve ter identidade estável, mas não expor `naipe`, `valor`, `cor` ou
`símbolo` no contrato geral: esses atributos não existem em todos os jogos.

## 3. Responsabilidades de infraestrutura

O framework contém mecanismos comuns, não regras de mesa:

- `MotorDePartida` coordena o ciclo de vida da partida.
- `GerenciadorDeTurnos` controla o jogador atual e a rotação.
- `EstadoPartida` restringe transições do ciclo de vida.
- `ResultadoDePartida` expõe o desfecho como valor imutável.
- contextos entregam somente snapshots e operações controladas às extensões.
- eventos notificam observadores sem acoplar o motor ao console.
- exceções de domínio comunicam falhas previsíveis sem expor detalhes internos.

O framework **não** define uma pilha de descarte, mão do dealer, apostas,
combinações, cartas francesas ou a regra de reciclagem de descarte. Esses elementos
ficam nos jogos clientes até que o uso por jogos independentes justifique uma nova
abstração reutilizável.

## 4. Variações e pontos de extensão

| Ponto de extensão | Variação atendida | Contrato público candidato |
|---|---|---|
| Tipo de carta | atributos e igualdade de cada baralho | `Carta` |
| Composição do baralho | 52 cartas, dois baralhos, cartas especiais etc. | `BaralhoFactory` |
| Distribuição inicial | quantidade, ordem e destinatários das cartas | `EstrategiaDeDistribuicao` |
| Decisão do jogador | humano, bot, dealer ou outra automação | `EstrategiaDeDecisao` |
| Ações e etapas | operações válidas em cada turno | `Jogada` e `EtapaDeTurno` abertas |
| Validação | pré-condições de ações de um jogo | `RegraDeValidacaoStrategy` |
| Vitória/encerramento | critério de fim e vencedores | `RegraDeVitoriaStrategy` |
| Pontuação | cálculo de placar, inclusive estratégia neutra | `RegraDePontuacaoStrategy` |
| Eventos | fatos observáveis e reações externas | `EventoDePartida` e `PartidaListener` |
| Turno concreto | interpretação de ações do jogo | gancho protegido de `MotorDePartida` |

Esses contratos são candidatos; suas assinaturas só devem ser definidas depois de os
dois jogos-prova demonstrarem que conseguem usá-los sem importar `core`.

## 5. Fluxo comum e fluxo específico

O fluxo comum da partida é:

```text
configurar → preparar → distribuir → executar turnos → avaliar encerramento → finalizar
```

`MotorDePartida.executar()` controla essa sequência e permanece final. O turno não é
presumido como "comprar e descartar": cada jogo o implementa ou especializa com suas
próprias ações. Assim, Trinca pode controlar compra, descarte e reciclagem, enquanto
Blackjack controla pedir carta, parar e o turno do dealer.

Um `ContextoDePartida` público, caso seja necessário para o gancho de turno, deve
conter apenas capacidade genérica: consultar estado, jogadores e mãos; comprar do
baralho; mover uma carta para ou de uma mão; avançar turno; encerrar partida e
publicar evento. Ele não terá `cartasPublicas`, `publicarCarta`, `recolherCarta` nem
`devolverAoBaralho`, pois esses nomes codificam a mecânica de descarte da Trinca.

## 6. Fronteira entre API e core

### API pública

Contém os contratos que um autor de jogo precisa conhecer: cartas, baralho, mão,
jogador, estratégias, jogadas/etapas, eventos, exceções, configuração, motor abstrato,
estado, resultado e os contextos indispensáveis às extensões. Implementações padrão
como `BaralhoPadrao`, `MaoDeCartasPadrao` e `JogadorPadrao` podem ser públicas se forem
utilizáveis sem conhecer detalhes internos.

### Core interno

Contém o estado mutável da partida, implementação de contextos, ordem de turnos,
armazenamento das mãos, publicação de eventos e detalhes de execução. Clientes não
importam `core`; `core` nunca importa um pacote de jogo.

### Jogos clientes

Contêm cartas concretas, regras, jogadas, fases, estratégias concretas, interface de
console e qualquer estado de mesa próprio. Exemplos: `PilhaDeDescarte` e combinações
na Trinca; `MaoDoDealer` e valor de Ás no Blackjack.

## 7. Cenários de validação

Os dois jogos não são modelos para a API; eles são testes de aceitação arquitetural:

| Cenário | Deve demonstrar |
|---|---|
| Trinca | dois baralhos, distribuição alternada, compra de monte ou descarte, combinações e reciclagem sem alterar o `core`. |
| Blackjack básico | baralho simples, duas cartas iniciais, pedir/parar, Ás como 1 ou 11, dealer compra até 17 e limite 21 sem alterar o `core`. |

Blackjack básico não inclui apostas, seguro, *split*, *double down* ou outras regras
avançadas. Essas funcionalidades aumentam o escopo, mas não são necessárias para
provar reuso.

## 8. Critério de congelamento da API

A API só poderá ser congelada quando:

1. cada contrato tiver responsabilidade e fronteira de pacote definidas;
2. ao menos cinco pontos de extensão estiverem justificados por variações reais;
3. existirem pequenos stubs de Trinca e Blackjack que compilem apenas contra `api`;
4. não houver referência a descarte, dealer, combinação ou aposta no `core`;
5. coleções internas forem protegidas por cópias defensivas ou visões imutáveis;
6. os quatro padrões GoF forem associados a problemas reais, e não a uma contagem.

