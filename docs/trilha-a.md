# Trilha A — motor e ciclo de vida

**Responsável:** Lucas Nóbrega de Araújo

**Status:** parte reutilizável da Trilha A concluída. O engine já integra as
Strategies de validação, vitória e pontuação e está pronto para ser validado pelos
clientes concretos; Trinca e Blackjack pertencem à Trilha E e ainda não estão
implementados na `main`. A publicação dos eventos da Trilha D pelo motor já está
implementada: `MotorDePartida` cadastra observadores, publica os seis eventos padrão
ao longo do ciclo e permite que subclasses publiquem eventos específicos do jogo.

Este texto reúne a justificativa técnica da Trilha A e um roteiro para sua defesa na
apresentação. As decisões seguem `ARQUITETURA_FRAMEWORK_MAP.md`,
`divisao-responsabilidades.md`, `especificacao_arquitetural.md`,
`padroes-de-projeto.md` e `regras-blackjack-basico.md`, com as assinaturas conferidas
contra o código atual. A especificação da Trinca permanece na branch do cliente.

## 1. Responsabilidade e resultado

A Trilha A separa **o fluxo controlado pelo framework** das **regras fornecidas por
cada jogo**:

- `MotorDePartida<C>` controla a sequência completa e a Inversão de Controle;
- `PartidaConfig<C>` reúne e valida os colaboradores necessários à execução;
- `EstadoPartida` e `CicloDeVidaDaPartida` definem e aplicam as transições legais;
- `GerenciadorDeTurnos` controla participante atual, sentido e pulos;
- `PartidaEmExecucao<C>` mantém o agregado mutável sem expor sua representação;
- `VisaoDaPartida<C>` e `ContextoDePartida<C>` oferecem autoridades diferentes;
- `ResultadoDoTurno` comunica efeitos ao controlador sem alterar turnos diretamente;
- `DesfechoDePartida` e `ResultadoDePartida` representam valores finais imutáveis.

O motor não conhece trincas, sequências, limite de 21, dealer, pilha de descarte ou
ações como pedir e parar. Ele oferece mecanismos comuns a uma **família ampla de
jogos de cartas baseados em turnos**. Não é uma promessa literal de suportar todo
jogo concebível sem novos pontos de extensão.

A composição do baralho e sua distribuição pertencem à Trilha B; participantes e
decisão pertencem à Trilha C; regras, eventos e exceções pertencem à Trilha D. A
Trilha A consome esses contratos sem criar versões paralelas deles.

## 2. Mapeamento dos packages

O mapeamento atual é intencional:

| Package | Tipos da Trilha A | Justificativa |
|---|---|---|
| `cardgame.api` | `EstadoPartida`, `PartidaConfig`, `VisaoDaPartida`, `ContextoDePartida`, `ResultadoDoTurno`, `DesfechoDePartida`, `ResultadoDePartida`, `MotivoDeEncerramento`, `MotivoPadrao` | contratos e valores públicos usados para configurar, especializar e consultar uma partida |
| `cardgame.engine` | `MotorDePartida` | único tipo público do runtime e ponto de extensão por herança |
| `cardgame.engine` | `GerenciadorDeTurnos`, `SentidoDeRotacao`, `CicloDeVidaDaPartida`, `PartidaEmExecucao`, `ContextoDeDistribuicaoInterno` | colaboradores package-private que somente o motor deve manipular |
| `cardgame.api.evento` | nenhum tipo da Trilha A | eventos da Trilha D publicados pelo motor |

Colocar os colaboradores internos junto de `MotorDePartida` não cria uma dependência
arquitetural indevida: eles formam a implementação coesa do runtime. Deixá-los sem
`public` impede que Trinca ou Blackjack avancem turnos, alterem o ciclo ou acessem
mãos mutáveis fora do fluxo permitido.

Uma API pública também pode conter classes finais, records e enums. Esses tipos
ficam em `api` porque os clientes precisam construí-los ou recebê-los; os detalhes de
armazenamento permanecem em `engine`.

## 3. Template Method e Inversão de Controle

`MotorDePartida.executar()` é o **Template Method**. O método é `public final`, fixa
o algoritmo da partida e pode ser executado uma única vez.

| Papel no desenho | Operação ou tipo |
|---|---|
| Template Method | `MotorDePartida.executar()` |
| operação primitiva obrigatória | `executarTurno(ContextoDePartida<C>)` |
| hooks opcionais | `preparar`, `aposDistribuir` e `aoEncerrar` |
| validação oferecida à subclasse | `validarJogada(VisaoDaPartida<C>, Jogada)` |
| publicação oferecida à subclasse | `publicarEvento(EventoDePartida)` |
| variações por composição | fábrica, distribuição e Strategies de regras em `PartidaConfig` |
| infraestrutura fixa | ciclo de vida, agregado da execução e gerenciador de turnos |

O fluxo real é:

```text
CONFIGURADA
    │
    ▼
PREPARANDO
    ├─ criar o baralho pela Factory
    ├─ criar mãos, turnos e estado interno
    ├─ embaralhar o baralho
    ├─ preparar(contexto)
    ├─ executar a Strategy de distribuição
    └─ aposDistribuir(contexto)
    │
    ▼
EM_ANDAMENTO
    ├─ consultar a Strategy de vitória antes do primeiro turno
    └─ repetir:
         definir número e participante do turno
         executarTurno(contexto), repetindo se a jogada for inválida
         consultar a Strategy de vitória
         aplicar repetição, inversão, pulo ou avanço se a partida continuar
    │
    ▼
FINALIZADA
    ├─ calcular e validar o placar pela Strategy de pontuação
    ├─ criar ResultadoDePartida
    ├─ transicionar para FINALIZADA
    └─ aoEncerrar(visão, resultado)
```

O cliente chama `executar()` uma vez; depois disso, o framework chama o hook e as
Strategies na ordem definida. Isso é Inversão de Controle. O jogo decide o conteúdo
de uma jogada e suas regras, mas não substitui a sequência global.

A Strategy de validação não pode ser chamada automaticamente antes do hook porque o
engine ainda não conhece qual `Jogada` o cliente produzirá. Por isso o motor concreto
deve chamar o método final `validarJogada` antes de aplicar cada ação. Vitória e
pontuação, por outro lado, são chamadas diretamente pelo Template Method.

## 4. Ciclo de vida e diretivas de turno

O ciclo admite somente:

```text
CONFIGURADA → PREPARANDO → EM_ANDAMENTO → FINALIZADA
```

`EstadoPartida` conhece a tabela de sucessores legais; `CicloDeVidaDaPartida` mantém
o valor corrente e lança `EstadoDePartidaInvalidoException` quando uma operação tenta
violar o grafo. O padrão State não foi aplicado porque os estados não possuem
comportamentos polimórficos próprios. Quatro classes de estado acrescentariam
estrutura sem uma variação real de comportamento.

O jogo também não recebe o `GerenciadorDeTurnos`. Ao concluir uma jogada válida, ele
devolve um valor declarativo:

- `ResultadoDoTurno.avancar()` passa para o próximo participante;
- `ResultadoDoTurno.repetir()` conserva o participante atual;
- `ResultadoDoTurno.inverter()` muda o sentido antes de avançar;
- `ResultadoDoTurno.pular(n)` salta `n` participantes antes do próximo turno.

O gerenciador usa aritmética modular com `Math.floorMod`, portanto o mesmo algoritmo
funciona para dois ou mais jogadores e nos dois sentidos. Pulos negativos e a
combinação incoerente de repetição com pulo são recusados na construção da diretiva.

Se `executarTurno` lançar `JogadaInvalidaException`, o engine mantém o participante e
o número lógico do turno e oferece nova tentativa. Após 100 recusas consecutivas, uma
`IllegalStateException` encerra a tentativa para impedir um laço infinito causado
por um cliente defeituoso. Não há rollback: o motor concreto deve validar antes de
realizar mutações irreversíveis.

## 5. Contextos, estado interno e invariantes

Os contratos expõem diferentes níveis de autoridade:

- `VisaoDaPartida<C>` oferece somente estado, participantes, jogador atual, mãos,
  quantidade no baralho e número do turno, sempre por valores ou snapshots;
- `ContextoDePartida<C>` acrescenta compra, inclusão e remoção de cartas e
  reembaralhamento, mas não permite avançar turnos nem finalizar a partida;
- `ContextoDeDistribuicao<C>` é a porta ainda menor consumida pela Strategy da
  Trilha B;
- `ContextoDeValidacao<C>` reúne uma `Jogada` e uma visão somente leitura para a
  Strategy da Trilha D.

`ContextoDeDistribuicaoInterno` adapta `PartidaEmExecucao` à porta estreita de
distribuição. A Strategy pode entregar a próxima carta, mas não recebe o baralho nem
as mãos mutáveis.

O agregado interno mantém um baralho exclusivo, uma `MaoDeCartas` por identidade de
jogador, o gerenciador de turnos e o ciclo de vida. Entre as invariantes verificadas
estão:

- ao menos dois jogadores, sem identidades lógicas repetidas;
- primeiro jogador dentro da lista;
- Factory, distribuição e três Strategies de regras obrigatórias;
- Factory proibida de devolver baralho nulo;
- carta proibida de aparecer simultaneamente no baralho e nas mãos;
- vencedor obrigado a pertencer à partida;
- placar obrigado a conter exatamente todos os participantes;
- desfecho, resultado e coleções públicas copiados defensivamente;
- mutações de cartas permitidas apenas em preparação ou andamento;
- segunda chamada de `executar()` recusada.

Uma instância do motor representa uma execução síncrona, não é thread-safe e não
pode ser reiniciada depois de sucesso ou falha inesperada. Uma nova partida exige
uma nova instância.

## 6. Como montar a Trinca com o framework

A implementação abaixo pertence ao pacote cliente, por exemplo
`br.edu.uepb.map.trinca`, e não à Trilha A. Ela é o principal cenário de validação do
motor.

### 6.1 Tipos do cliente

O cliente cria:

- `CartaTrinca implements Carta`, com `UUID`, valor e naipe;
- `BaralhoTrincaFactory`, que cria 104 cartas sem curingas;
- `MotorDeTrinca extends MotorDePartida<CartaTrinca>`;
- ações tipadas de compra e descarte;
- estado próprio para a pilha de descarte;
- Strategies de validação, vitória e pontuação da Trinca.

A pilha de descarte e a regra de formar trincas ou sequências ficam no cliente porque
não são mecanismos universais de jogos de carta.

### 6.2 Configuração

A configuração injeta todos os colaboradores variáveis:

```java
PartidaConfig<CartaTrinca> configuracao = PartidaConfig
        .<CartaTrinca>builder()
        .jogadores(List.of(jogador1, jogador2))
        .baralhoFactory(new BaralhoTrincaFactory())
        .distribuicao(new DistribuicaoAlternada<>(9))
        .regraDeValidacao(new RegraDeValidacaoTrinca())
        .regraDeVitoria(new RegraDeVitoriaTrinca())
        .regraDePontuacao(new RegraDePontuacaoTrinca())
        .primeiroJogador(0)
        .build();

ResultadoDePartida resultado = new MotorDeTrinca(configuracao).executar();
```

O Builder evita um construtor posicional longo, concentra a validação da montagem e
deixa explícito qual colaboração resolve cada variação.

### 6.3 Execução do turno

O fluxo do futuro `MotorDeTrinca` pode ser:

1. em `aposDistribuir`, comprar uma carta e iniciar a pilha de descarte do cliente;
2. em `executarTurno`, consultar `contexto.jogadorAtual()`;
3. pedir à Strategy do jogador uma ação tipada de compra;
4. chamar `validarJogada(contexto, jogada)` antes de mover qualquer carta;
5. aplicar a compra pelo monte ou pela zona de descarte do cliente;
6. pedir, validar e aplicar uma ação tipada de descarte;
7. devolver `ResultadoDoTurno.avancar()` após o ciclo completo;
8. deixar a Strategy de vitória reconhecer as combinações da mão;
9. deixar a Strategy de pontuação calcular o placar do desfecho.

As zonas próprias do jogo podem ser mantidas por um pequeno estado de cliente
compartilhado apenas com seus colaboradores. Elas não devem ser acrescentadas ao
framework enquanto não houver reuso demonstrado em outro jogo.

## 7. Blackjack como segunda prova

Blackjack reutiliza o mesmo ciclo com decisões e regras diferentes:

- a Factory cria 52 cartas e a distribuição entrega duas por participante;
- `PEDIR` compra uma carta e pode devolver `ResultadoDoTurno.repetir()`;
- `PARAR` registra a decisão no estado próprio do cliente e avança;
- a casa pode ser um `JogadorPadrao` cuja Strategy pede até 16 e para a partir de 17;
- a Strategy de validação aceita apenas ações legais no estado observado;
- a Strategy de vitória considera participantes parados, estouro e esgotamento;
- a Strategy de pontuação compara os totais e produz uma entrada para cada jogador.

Informações específicas, como participantes que já pararam ou o valor flexível do
Ás, pertencem aos tipos do cliente. Quando mais de um colaborador do Blackjack
precisar desses dados, o cliente pode injetar neles o mesmo estado específico sem
expor os internos do engine.

Trinca usa compra por origem, descarte e formação de combinações; Blackjack usa pedir,
parar, estouro e política da casa. Se ambos executarem sem editar
`MotorDePartida`, a separação entre frozen-spots e hot-spots estará comprovada.

## 8. Frozen-spots, hot-spots e componentes prontos

Frozen-spots relevantes:

- o algoritmo final de `MotorDePartida.executar()`;
- o grafo do ciclo e a execução única por instância;
- a rotação encapsulada em `GerenciadorDeTurnos`;
- a ordem de chamada da distribuição e das Strategies de regras;
- a validação estrutural de vencedores, placar e identidades;
- a autoridade limitada de cada contexto público.

Hot-spots usados pela Trilha A:

- subclasse de `MotorDePartida<C>` e seu `executarTurno` obrigatório;
- hooks opcionais `preparar`, `aposDistribuir` e `aoEncerrar`;
- `MotivoDeEncerramento`, para vocabulário próprio de encerramento;
- Factory e Strategy de distribuição da Trilha B;
- ações e Strategies de decisão da Trilha C;
- Strategies de validação, vitória e pontuação da Trilha D.

Componentes da Trilha A prontos para reuso:

- `MotorDePartida`;
- `PartidaConfig` e seu Builder;
- `EstadoPartida`;
- `VisaoDaPartida` e `ContextoDePartida`;
- `ResultadoDoTurno`;
- `DesfechoDePartida`, `ResultadoDePartida` e `MotivoPadrao`.

A assinatura da API só deve ser declarada definitivamente congelada depois que os
dois clientes da Trilha E compilarem e executarem importando apenas `api` e
`engine.MotorDePartida`.

## 9. Decisões segundo padrões, SOLID e GRASP

O padrão principal da Trilha A é **Template Method**. `PartidaConfig.Builder` é um
apoio de construção; Factory Method e Strategy são colaborações pertencentes às
outras trilhas. O enum de ciclo não é apresentado como State. **Observer** está
comprovado no runtime: o motor mantém a lista de observadores, publica os seis eventos
padrão, aceita eventos específicos das subclasses e isola falhas de cada ouvinte. Os
testes cobrem ordem de notificação, evento próprio, descadastro durante o callback e
ouvinte que lança exceção.

| Princípio | Decisão de modelagem |
|---|---|
| SRP / Alta Coesão | fluxo, estado, turnos, agregado e valores finais possuem donos diferentes |
| OCP | novos jogos especializam o turno e injetam regras sem condicionais por nome de jogo no engine |
| LSP | subclasses que respeitam os contratos dos hooks preservam o ciclo porque o Template Method não pode ser sobrescrito |
| ISP | visão, contexto de mutação, distribuição e validação oferecem portas distintas |
| DIP | o engine depende de contratos de `api`, não de Trinca, Blackjack ou implementações concretas |
| Baixo Acoplamento | somente o motor é público em `engine`; detalhes internos não vazam aos clientes |
| Controlador | `MotorDePartida` recebe e coordena o caso de uso “executar partida” |
| Especialista na Informação | o estado conhece transições; o gerenciador conhece a rotação; cada Strategy conhece sua regra |
| Creator | o motor cria o agregado transitório e os colaboradores internos da execução |
| Indireção | contextos e diretivas medeiam o acesso ao estado e ao controle de turnos |
| Variações Protegidas | regras e mecanismos variáveis ficam atrás de hooks ou interfaces públicas |

`ContextoDeDistribuicaoInterno` exerce uma adaptação interna entre a Strategy de
distribuição e o agregado. Para a defesa dos padrões exigidos, porém, a evidência
central desta trilha continua sendo o Template Method executado e testado.

## 10. Testes e Javadoc

Os testes diretamente ligados à Trilha A cobrem:

- grafo de estados e rejeição de transições ilegais;
- Builder, cópia defensiva, colaboradores obrigatórios e identidades únicas;
- desfechos, motivos, placar, empate e valores finais imutáveis;
- avanço, repetição, inversão e pulos com dois ou mais jogadores;
- uso de `Math.floorMod` no sentido anti-horário;
- controle de mãos, baralho, identidade de cartas e bloqueio de mutação final;
- sequência do Template Method e execução única;
- integração das três Strategies de regras;
- repetição da mesma vez depois de `JogadaInvalidaException`;
- encerramento antes do primeiro turno;
- rejeição de vencedor externo e placar incompleto;
- publicação de evento próprio pela subclasse, com método protegido e final;
- direção das dependências e superfície pública do engine por análise de bytecode.

Não é necessário duplicar nesses testes as regras completas de Trinca e Blackjack.
Formação de combinações, reciclagem do descarte, valor do Ás, política da casa e
condições concretas de vitória pertencem aos testes de aceitação da Trilha E.

Após esta revisão, `./mvnw clean test` executa **137 testes**, sem falhas, erros ou
testes ignorados. Desse total, **71 testes** exercitam diretamente os tipos públicos e
internos atribuídos à Trilha A, e outros **3 testes arquiteturais** protegem as
fronteiras que envolvem o engine.

O Javadoc dos tipos públicos e hooks protegidos da Trilha A registra finalidade,
estado esperado, parâmetros, retornos, exceções, imutabilidade, execução única e
ausência de rollback. A geração seletiva com `-Xdoclint:all -Werror` foi concluída
sem avisos ou erros.

## 11. Limite da entrega

A parte de Lucas no framework está concluída. Permanecem fora desta entrega:

- implementação de `MotorDeTrinca`, `MotorDeBlackjack` e seus tipos — Trilha E;
- testes de aceitação completos dos dois clientes — Trilha E;
- validação final da suficiência da API pelos dois clientes concretos.

A direção das dependências já não depende de inspeção manual:
`FronteirasArquiteturaisTest` falha o build se `api` conhecer o engine ou jogos, se o
engine conhecer clientes, ou se um colaborador interno se tornar público.

O limite operacional também deve ser declarado: o runtime é síncrono, uma instância
serve para uma única partida e mutações feitas por um hook antes de uma falha não são
desfeitas automaticamente.

Para resumir na apresentação: **o framework conserva o ciclo, o estado, a ordem e as
invariantes; o jogo fornece o turno e injeta suas regras; contextos limitam a
autoridade; diretivas comunicam efeitos; e nenhum detalhe de Trinca ou Blackjack
entra no engine.**
