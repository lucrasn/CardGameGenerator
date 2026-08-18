# Trilha C — jogadores e estratégias de decisão

**Responsável:** Allan Guilherme da S. Vieira

**Status:** parte reutilizável da Trilha C concluída. A API está pronta para ser
validada pelos clientes concretos; Trinca e Blackjack pertencem à Trilha E e ainda
não estão implementados na `main`.

Este texto reúne a justificativa técnica da Trilha C e um roteiro para sua defesa na
apresentação. As decisões seguem `divisao-responsabilidades.md`,
`padroes-de-projeto.md`. As especificações dos clientes Trinca e Blackjack são
mantidas nas branches `jogo/trinca` e `jogo/blackjack`, respectivamente.

## 1. Responsabilidade e resultado

A Trilha C separa **quem participa** de **como decide**:

- `Jogador` define identidade estável, nome e estratégia atual;
- `JogadorPadrao` implementa essa identidade por composição;
- `EstrategiaDeDecisao` define o algoritmo substituível de escolha;
- `ContextoDeDecisao` entrega à estratégia uma visão mínima e somente leitura;
- `Jogada` e `EtapaDeTurno` permitem ações e fases próprias de cada jogo;
- `EntradaSaida` isola a interação humana;
- estratégias humana, aleatória e gulosa são implementações reutilizáveis.

Mão, pontuação, descarte, regra de vitória e fluxo da partida não foram colocados no
jogador. Esses dados mudam de um jogo para outro e pertencem ao agregado mantido pelo
motor ou ao cliente concreto.

## 2. Mapeamento dos packages

O mapeamento atual está correto:

| Package | Tipos da Trilha C | Justificativa |
|---|---|---|
| `cardgame.api` | `Jogador`, `JogadorPadrao`, `EstrategiaDeDecisao`, `ContextoDeDecisao`, `ContextoDeDecisaoPadrao`, `Jogada`, `EtapaDeTurno`, `EntradaSaida` | contratos e componentes públicos que um cliente precisa importar ou instanciar |
| `cardgame.api.estrategia` | `DecisaoHumanaConsole`, `DecisaoAleatoria`, `DecisaoGulosa` | estratégias públicas prontas, agrupadas por responsabilidade |
| `cardgame.api.io` | `ControleEntradaSaida` | implementação pública da porta de terminal |
| `cardgame.engine` | nenhum tipo da Trilha C | contém o runtime e seus frozen-spots; o motor não deve conhecer decisões de jogos concretos |
| `cardgame.api.evento` | nenhum tipo da Trilha C | eventos pertencem à Trilha D; a decisão não os publica |

Uma API pública não precisa ser formada apenas por interfaces. Classes finais e
records reutilizáveis também pertencem à API quando o cliente deve construí-los. Por
isso `ContextoDeDecisaoPadrao` foi retirado de `core`: mantê-lo ali obrigaria Trinca e
Blackjack a importar um detalhe interno.

As subpastas `api.estrategia` e `api.io` continuam fazendo parte da superfície
pública; elas somente organizam implementações por assunto.

## 3. Strategy de decisão

O padrão **Strategy** encapsula uma família de algoritmos, permite substituí-los e
faz com que variem independentemente de quem os utiliza.

| Papel no desenho | Tipo |
|---|---|
| Strategy | `EstrategiaDeDecisao` |
| ConcreteStrategies | `DecisaoHumanaConsole`, `DecisaoAleatoria`, `DecisaoGulosa` e estratégias criadas pelos jogos |
| objeto que mantém a Strategy | `JogadorPadrao` |
| cliente que dispara a decisão | futuro `MotorDeTrinca` ou `MotorDeBlackjack`, no hook `executarTurno` |
| entrada da Strategy | `ContextoDeDecisao` |
| resultado | `Jogada` |

`ContextoDeDecisao` é o nome do objeto de entrada da decisão; ele não deve ser
confundido com o participante chamado *Context* na descrição clássica do GoF. No
desenho deste projeto, `JogadorPadrao` guarda a referência intercambiável e o motor do
jogo cliente realiza a chamada.

Exemplo de substituição em tempo de execução:

```java
JogadorPadrao participante = new JogadorPadrao(
        "Ana",
        new DecisaoHumanaConsole(new ControleEntradaSaida())
);

participante.alterarEstrategiaDeDecisao(new DecisaoAleatoria());
```

O `UUID` e o nome permanecem os mesmos. Somente a política muda. Isso evita classes
como `JogadorHumano`, `JogadorBot` e `Dealer`, cuja combinação de perfis faria a
hierarquia crescer e impediria a troca dinâmica.

## 4. Ações, etapas e contexto abertos

`Jogada` e `EtapaDeTurno` são interfaces marcadoras de propósito. Não existe uma
operação universal que faça sentido para comprar, descartar, pedir e parar. Colocar
essas ações no framework criaria condicionais por jogo e violaria OCP.

Também não há duplicação entre `EtapaDeTurno` e `GerenciadorDeTurnos`:

- a etapa descreve **em qual fase interna** do turno o jogador está, como compra ou
  descarte;
- o gerenciador interno do engine controla **de quem é a vez**, o sentido e os pulos.

O contexto base contém a etapa e um snapshot imutável das ações legais. Um cliente
que precise de mais informação pode criar uma subinterface pública mais específica,
sem revelar o `ContextoDePartida` mutável:

```java
interface ContextoTrinca extends ContextoDeDecisao {
    List<CartaTrinca> maoDoJogador();
    Optional<CartaTrinca> topoDoDescarte();
}
```

Uma estratégia humana normalmente precisa apenas das ações já filtradas. Um bot pode
usar o contexto especializado para avaliar a mão. A estratégia recebe somente dados
que aquele participante tem permissão para enxergar.

## 5. Decisão humana e isolamento do console

`DecisaoHumanaConsole` depende de `EntradaSaida`, não de `System.in` e `System.out`.
`ControleEntradaSaida` é a implementação para terminal e aceita `Reader`/`Writer`
injetados, o que torna os testes determinísticos.

As descrições em texto existem apenas nessa fronteira. A estratégia devolve a mesma
instância tipada de `Jogada` que recebeu no contexto; o motor nunca interpreta uma
`String` como comando de domínio.

Essa porta demonstra **DIP** e testabilidade. Ela não é apresentada como Adapter GoF,
porque não há duas interfaces incompatíveis sendo convertidas.

## 6. Como montar a Trinca com o framework

A implementação abaixo pertence ao pacote cliente, por exemplo
`br.edu.uepb.map.trinca`, e não à Trilha C. Ela é o principal cenário de apresentação.

### 6.1 Tipos do cliente

O cliente cria:

- `CartaTrinca implements Carta`, com `UUID`, valor e naipe;
- `BaralhoTrincaFactory`, que cria 104 cartas sem curingas;
- `MotorDeTrinca extends MotorDePartida<CartaTrinca>`;
- regra para validar trincas e sequências;
- estado próprio para a pilha de descarte;
- ações e etapas tipadas:

```java
enum OrigemCompra { MONTE, DESCARTE }

record Comprar(OrigemCompra origem) implements Jogada {}
record Descartar(UUID cartaId) implements Jogada {}

enum EtapaTrinca implements EtapaDeTurno {
    COMPRA,
    DESCARTE
}
```

O descarte fica no cliente porque é regra da Trinca, não uma característica comum a
todos os jogos de carta.

### 6.2 Configuração

Os dois participantes humanos reutilizam `JogadorPadrao` e
`DecisaoHumanaConsole`. A configuração usa a fábrica própria e a distribuição pronta
de nove cartas:

```java
PartidaConfig<CartaTrinca> configuracao = PartidaConfig
        .<CartaTrinca>builder()
        .jogadores(List.of(jogador1, jogador2))
        .baralhoFactory(new BaralhoTrincaFactory())
        .distribuicao(new DistribuicaoAlternada<>(9))
        .primeiroJogador(0)
        .build();

ResultadoDePartida resultado = new MotorDeTrinca(configuracao).executar();
```

Essa é a Inversão de Controle do framework: a aplicação chama `executar()` uma vez e
o Template Method chama os hooks do jogo.

### 6.3 Execução do turno

O fluxo do `MotorDeTrinca` seria:

1. em `aposDistribuir`, retirar uma carta do monte e iniciar o descarte;
2. em `executarTurno`, obter `contexto.jogadorAtual()`;
3. construir um `ContextoTrinca` na etapa `COMPRA`, com `Comprar(MONTE)` e, quando
   possível, `Comprar(DESCARTE)`;
4. chamar `jogador.estrategiaDeDecisao().decidir(contextoDeCompra)` e aplicar a origem
   escolhida;
5. construir outro contexto na etapa `DESCARTE`, com uma ação `Descartar` para cada
   carta da mão;
6. chamar a mesma Strategy, validar o identificador antes da mutação e mover a carta
   escolhida para o topo do descarte;
7. devolver `ResultadoDoTurno.avancar()` após o ciclo comprar–descartar completo;
8. implementar `RegraDeVitoriaStrategy` verificando se as nove cartas restantes formam
   combinações ou se não há carta para comprar/reciclar;
9. implementar `RegraDePontuacaoStrategy` atribuindo 1 ao vencedor e 0 aos demais, e
   registrar as duas em `PartidaConfig`.

As ações oferecidas devem ser produzidas a partir do estado válido. Assim, a decisão
escolhe entre possibilidades legais e o cliente valida toda referência antes de
alterar a mão ou o descarte.

Para trocar humano por bot, basta fornecer outra `EstrategiaDeDecisao` ao mesmo
`JogadorPadrao`; o motor, a carta e as regras da Trinca não mudam.

## 7. Blackjack como segunda prova

Blackjack reutiliza os mesmos contratos com regras diferentes:

```java
enum AcaoBlackjack implements Jogada { PEDIR, PARAR }
enum EtapaBlackjack implements EtapaDeTurno { DECISAO }

interface ContextoBlackjack extends ContextoDeDecisao {
    int totalDaMao();
}
```

- a fábrica cria 52 cartas e a distribuição entrega duas por participante;
- `PEDIR` compra uma carta e pode devolver `ResultadoDoTurno.repetir()`;
- `PARAR` registra o fim das decisões e avança;
- a casa é um `JogadorPadrao` com Strategy: total menor que 17 pede, total igual ou
  maior que 17 para;
- o desfecho compara os totais, estouro e empate;
- não existe `Dealer` no framework.

Trinca usa compra por origem e descarte; Blackjack usa pedir e parar. Se ambos
funcionarem sem editar `MotorDePartida`, a separação entre frozen-spots e hot-spots
está comprovada.

## 8. Frozen-spots, hot-spots e componentes prontos

Frozen-spots relevantes:

- o algoritmo final de `MotorDePartida.executar()`;
- a rotação encapsulada em `GerenciadorDeTurnos`;
- as invariantes públicas de identidade, contexto imutável e ação tipada.

Hot-spots da Trilha C:

- `EstrategiaDeDecisao`, para novas políticas;
- `Jogada`, para ações de cada jogo;
- `EtapaDeTurno`, para fases de cada turno;
- `ContextoDeDecisao`, por subinterfaces seguras;
- `EntradaSaida`, para terminal, GUI ou dublê de teste.

Componentes prontos para reuso:

- `JogadorPadrao`;
- `ContextoDeDecisaoPadrao`;
- `DecisaoHumanaConsole`;
- `DecisaoAleatoria`;
- `DecisaoGulosa`;
- `ControleEntradaSaida`.

A assinatura da API só deve ser declarada definitivamente congelada depois que os
dois clientes da Trilha E compilarem e executarem usando apenas `api` e
`engine.MotorDePartida`.

## 9. Decisões segundo SOLID e GRASP

| Princípio | Decisão de modelagem |
|---|---|
| SRP / Alta Coesão | identidade, escolha, visão da decisão e I/O possuem donos diferentes |
| OCP | novos jogos acrescentam ações, etapas, contextos e Strategies sem editar jogador ou engine |
| LSP | qualquer Strategy que respeite o contrato pode substituir outra sem mudar o cliente |
| ISP | a decisão recebe uma visão estreita, não todo o estado mutável da partida |
| DIP | jogador e decisão dependem de abstrações públicas; console depende da porta `EntradaSaida` |
| Baixo Acoplamento | a Trilha C não importa `core`, `engine`, Trinca ou Blackjack |
| Polimorfismo | a escolha humana, aleatória, gulosa ou específica do jogo usa o mesmo contrato |
| Especialista na Informação | a Strategy conhece a política de escolha; o cliente conhece e cria as ações legais do jogo |
| Indireção | `ContextoDeDecisao` medeia o acesso da Strategy às informações da partida |
| Variações Protegidas | a variação humano/bot/casa fica protegida atrás de `EstrategiaDeDecisao` |

A composição foi preferida à herança porque “humano”, “bot” e “casa” são
comportamentos substituíveis, não identidades diferentes. `EtapaDeTurno` também não
implementa State: seus tipos apenas identificam fases e não encapsulam comportamento
polimórfico próprio.

## 10. Testes e Javadoc

Os testes da Trilha C cobrem:

- identidade, normalização do nome e troca real de Strategy sem troca de `UUID`;
- decisões aleatória e gulosa, incluindo empate e fonte aleatória controlada;
- contexto imutável, cópia defensiva, nulos e possibilidade de especialização;
- decisão humana convertendo opções textuais de volta às ações tipadas;
- repetição de entrada até uma opção válida, fim de entrada e falha de leitura;
- ausência de ações, índices inválidos e colaboradores nulos;
- um consumidor em package externo criando ações e fases de Trinca e Blackjack;
- a política da casa do Blackjack usando um contexto especializado;
- compra por monte/descarte e descarte por `UUID` na Trinca.

Não é necessário duplicar nos testes unitários da Trilha C as regras completas dos
jogos. Distribuição, formação de combinações, reciclagem do descarte, Ás, estouro e
desfecho são testes de aceitação dos clientes da Trilha E.

Na baseline integrada atual, a suíte executa **137 testes**, sem falhas ou erros. O
Javadoc dos tipos públicos da Trilha C inclui finalidade, contratos, parâmetros,
retornos, exceções relevantes, autoria e versão. A geração estrita também não
produz avisos.

## 11. Limite da entrega

A parte de Allan no framework está concluída. Permanecem fora desta entrega:

- implementação de `MotorDeTrinca`, `MotorDeBlackjack` e seus tipos — Trilha E;
- testes de aceitação completos dos dois clientes — Trilha E;
- UML e relatório consolidados — Trilha E.

Para resumir na apresentação: **o jogador preserva identidade; a Strategy varia a
decisão; o contexto limita o que pode ser visto; a jogada mantém o domínio tipado; o
cliente define as regras; e o engine conserva o fluxo.**
