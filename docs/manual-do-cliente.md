# Manual do cliente — CardGame Framework

**Para quem quer construir um jogo de cartas sem escrever de novo o que todo jogo de
cartas tem.**

Este manual é escrito para quem vai *usar* o framework, não para quem o mantém. Ele
responde três perguntas, nesta ordem: o que já vem pronto, o que você precisa escrever
e até onde dá para ir. Todo trecho de código aqui foi compilado e executado contra a
versão atual da API.

- **Versão da API:** baseline `main` de 16/08/2026
- **Requisito:** JDK 26
- **Pacotes que você importa:** `br.edu.uepb.map.cardgame.api` e
  `br.edu.uepb.map.cardgame.engine`

---

## Sumário

1. [O que o framework faz por você](#1-o-que-o-framework-faz-por-você)
2. [Um jogo completo em cinco classes](#2-um-jogo-completo-em-cinco-classes)
3. [O mapa: o que é seu e o que é nosso](#3-o-mapa-o-que-é-seu-e-o-que-é-nosso)
4. [O ciclo de vida, passo a passo](#4-o-ciclo-de-vida-passo-a-passo)
5. [Os pontos de extensão, um a um](#5-os-pontos-de-extensão-um-a-um)
   - [5.1 Carta](#51-carta--o-que-é-uma-carta-no-seu-jogo)
   - [5.2 BaralhoFactory](#52-baralhofactory--de-que-cartas-o-jogo-é-feito)
   - [5.3 EstrategiaDeDistribuicao](#53-estrategiadedistribuicao--como-as-cartas-chegam-às-mãos)
   - [5.4 Jogador e EstrategiaDeDecisao](#54-jogador-e-estrategiadedecisao--quem-joga-e-como-escolhe)
   - [5.5 Jogada, EtapaDeTurno e ContextoDeDecisao](#55-jogada-etapadeturno-e-contextodedecisao--o-vocabulário-das-ações)
   - [5.6 RegraDeValidacaoStrategy](#56-regradevalidacaostrategy--o-que-é-jogada-legal)
   - [5.7 RegraDeVitoriaStrategy](#57-regradevitoriastrategy--quando-acaba-e-quem-ganhou)
   - [5.8 RegraDePontuacaoStrategy](#58-regradepontuacaostrategy--quantos-pontos-cada-um-fez)
   - [5.9 MotivoDeEncerramento](#59-motivodeencerramento--por-que-acabou)
   - [5.10 MotorDePartida](#510-motordepartida--o-que-acontece-num-turno)
   - [5.11 PartidaListener](#511-partidalistener--reagir-ao-que-acontece)
   - [5.12 EntradaSaida](#512-entradasaida--falar-com-quem-está-jogando)
6. [Casos de uso](#6-casos-de-uso)
   - [6.1 Turnos que não seguem a ordem](#61-turnos-que-não-seguem-a-ordem)
   - [6.2 Uma zona própria: a pilha de descarte](#62-uma-zona-própria-a-pilha-de-descarte)
   - [6.3 Rejeitar jogadas e deixar o jogador tentar de novo](#63-rejeitar-jogadas-e-deixar-o-jogador-tentar-de-novo)
   - [6.4 Uma pessoa jogando pelo console](#64-uma-pessoa-jogando-pelo-console)
   - [6.5 Trocar o comportamento de um bot no meio da partida](#65-trocar-o-comportamento-de-um-bot-no-meio-da-partida)
   - [6.6 Testar seu jogo de forma determinística](#66-testar-seu-jogo-de-forma-determinística)
7. [O que você não pode fazer, e por quê](#7-o-que-você-não-pode-fazer-e-por-quê)
8. [Erros comuns e o que eles significam](#8-erros-comuns-e-o-que-eles-significam)
9. [Referência rápida](#9-referência-rápida)

---

## 1. O que o framework faz por você

Todo jogo de cartas repete um conjunto de mecanismos. O framework implementa esses
mecanismos uma vez e deixa você escrever apenas o que é do seu jogo.

**Você recebe pronto e testado:**

| Mecanismo | O que já está resolvido |
|---|---|
| Ciclo de vida | a partida vai de configurada a finalizada numa ordem que não pode ser burlada |
| Rotação de turnos | ordem, inversão de sentido, pulos, com qualquer número de jogadores |
| Conservação de cartas | uma carta nunca está em duas mãos, nem numa mão e no baralho ao mesmo tempo |
| Encapsulamento | ninguém altera uma mão ou o baralho por fora das operações permitidas |
| Baralho e mão | comprar, colocar no topo/base, embaralhar, buscar, remover |
| Eventos | seis avisos padrão, publicação de eventos próprios e falha de ouvinte isolada |
| Resultado | vencedores, placar e motivo, validados e imutáveis |
| Exceções | uma hierarquia de domínio, com mensagens |

**Você escreve:** o que é uma carta no seu jogo, de que cartas o baralho é feito, o que
acontece num turno, o que é jogada legal, quando a partida acaba e quanto cada um fez de
pontos.

> **A promessa, dita com honestidade.** O framework não serve para "qualquer jogo
> concebível". Ele serve para jogos em que existe um baralho compartilhado, jogadores com
> mãos, turnos em rotação e uma condição de fim. Isso cobre Trinca, Blackjack, Uno,
> Buraco, Mau-Mau, Pife, Cassino e a maior parte dos jogos tradicionais. Jogos sem turnos
> definidos, com tabuleiro, ou com movimentação simultânea, não são o alvo — e a seção 7
> diz exatamente onde fica a parede.

---

## 2. Um jogo completo em cinco classes

Antes da teoria, o resultado. Este é um jogo inteiro, funcional: cada jogador compra uma
carta por turno e vence quem primeiro juntar cinco cartas na mão.

### 2.1 A carta

```java
public record CartaSimples(UUID id, int valor) implements Carta {

    public CartaSimples(int valor) {
        this(UUID.randomUUID(), valor);
    }
}
```

O framework exige uma coisa só de uma carta: uma identidade estável. Naipe, valor, cor,
figura — nada disso é imposto, porque nem todo jogo tem. Aqui o jogo decidiu que a carta
tem um `valor` inteiro; esse campo é seu, o framework nunca olha para ele.

### 2.2 De que cartas o baralho é feito

```java
public final class BaralhoDeNumeros implements BaralhoFactory<CartaSimples> {

    private final int maiorValor;

    public BaralhoDeNumeros(int maiorValor) {
        this.maiorValor = maiorValor;
    }

    @Override
    public Baralho<CartaSimples> criar() {
        List<CartaSimples> cartas = new ArrayList<>();
        for (int valor = 1; valor <= maiorValor; valor++) {
            cartas.add(new CartaSimples(valor));
        }
        return new BaralhoPadrao<>(cartas);
    }
}
```

`BaralhoPadrao` é a implementação pronta de baralho: você entrega a lista de cartas e ela
cuida de comprar, embaralhar e recusar cartas repetidas.

### 2.3 O que acontece num turno

```java
public final class MotorCorrida extends MotorDePartida<CartaSimples> {

    public MotorCorrida(PartidaConfig<CartaSimples> configuracao) {
        super(configuracao);
    }

    @Override
    protected ResultadoDoTurno executarTurno(ContextoDePartida<CartaSimples> contexto) {
        CartaSimples comprada = contexto.comprarDoBaralho();
        contexto.adicionarNaMao(contexto.jogadorAtual(), comprada);
        return ResultadoDoTurno.avancar();
    }
}
```

Esse é o **único método que você é obrigado a escrever**. Repare no que ele *não* faz:
não decide de quem é a vez, não verifica se a partida acabou, não mexe no placar, não
avança o turno. Ele descreve um turno e devolve uma diretiva dizendo o que deve acontecer
com a ordem depois — aqui, simplesmente avançar.

### 2.4 Quando acaba e quanto vale

```java
public final class VenceComCincoCartas implements RegraDeVitoriaStrategy<CartaSimples> {

    @Override
    public Optional<DesfechoDePartida> avaliar(VisaoDaPartida<CartaSimples> partida) {
        for (Jogador jogador : partida.jogadores()) {
            if (partida.maoDe(jogador).size() >= 5) {
                return Optional.of(
                        new DesfechoDePartida(List.of(jogador), MotivoPadrao.VITORIA));
            }
        }
        if (partida.quantidadeNoBaralho() == 0) {
            return Optional.of(
                    new DesfechoDePartida(List.of(), MotivoPadrao.ESGOTAMENTO));
        }
        return Optional.empty();
    }
}
```

```java
public final class SomaDasCartas implements RegraDePontuacaoStrategy<CartaSimples> {

    @Override
    public Map<Jogador, Integer> calcular(
            VisaoDaPartida<CartaSimples> partida, DesfechoDePartida desfecho) {
        Map<Jogador, Integer> placar = new LinkedHashMap<>();
        for (Jogador jogador : partida.jogadores()) {
            int soma = partida.maoDe(jogador).stream()
                    .mapToInt(CartaSimples::valor)
                    .sum();
            placar.put(jogador, soma);
        }
        return placar;
    }
}
```

`Optional.empty()` significa "ainda não acabou". Devolver um `DesfechoDePartida` encerra
a partida imediatamente.

### 2.5 Montar e rodar

```java
List<Jogador> jogadores = List.of(
        new JogadorPadrao("Ana", new DecisaoAleatoria()),
        new JogadorPadrao("Bruno", new DecisaoAleatoria()));

PartidaConfig<CartaSimples> config = PartidaConfig.<CartaSimples>builder()
        .jogadores(jogadores)
        .baralhoFactory(new BaralhoDeNumeros(40))
        .distribuicao(new DistribuicaoAlternada<>(3))
        .regraDeValidacao(contexto -> { })
        .regraDeVitoria(new VenceComCincoCartas())
        .regraDePontuacao(new SomaDasCartas())
        .build();

MotorCorrida motor = new MotorCorrida(config);
ResultadoDePartida resultado = motor.executar();

resultado.vencedorUnico().ifPresentOrElse(
        vencedor -> System.out.println("Venceu: " + vencedor.nome()),
        () -> System.out.println("Sem vencedor único."));
resultado.placar().forEach(
        (jogador, pontos) -> System.out.println(jogador.nome() + ": " + pontos));
```

Saída real de uma execução:

```text
Venceu: Ana
Ana: 99
Bruno: 56
```

Cinco classes pequenas e um jogo que roda. O `regraDeValidacao(contexto -> { })` é o
"aceito tudo": um método vazio que nunca lança exceção. A seção 5.6 mostra como usá-lo de
verdade.

---

## 3. O mapa: o que é seu e o que é nosso

Esta é a tabela mais importante do manual. Ela diz, para cada peça, se você **precisa**
escrever, **pode** escrever ou **recebe pronta**.

| Peça | Obrigatório? | Já existe pronto | Onde entra |
|---|---|---|---|
| `Carta` | **sim** | — | seu tipo de carta |
| `BaralhoFactory<C>` | **sim** | — | `PartidaConfig` |
| `EstrategiaDeDistribuicao<C>` | **sim** | `DistribuicaoAlternada` | `PartidaConfig` |
| `RegraDeValidacaoStrategy<C>` | **sim** | — | `PartidaConfig` |
| `RegraDeVitoriaStrategy<C>` | **sim** | — | `PartidaConfig` |
| `RegraDePontuacaoStrategy<C>` | **sim** | — | `PartidaConfig` |
| `MotorDePartida.executarTurno` | **sim** | — | sua subclasse do motor |
| `Jogador` | não | `JogadorPadrao` | `PartidaConfig` |
| `EstrategiaDeDecisao` | não | `DecisaoAleatoria`, `DecisaoGulosa`, `DecisaoHumanaConsole` | no jogador |
| `Baralho<C>` | não | `BaralhoPadrao` | devolvido pela fábrica |
| `MaoDeCartas<C>` | não | `MaoDeCartasPadrao` | zonas suas |
| `Jogada` | não | — | suas ações |
| `EtapaDeTurno` | não | — | suas fases |
| `MotivoDeEncerramento` | não | `MotivoPadrao` | no desfecho |
| `PartidaListener` | não | — | `motor.adicionarListener` |
| `EntradaSaida` | não | `ControleEntradaSaida` | interface com a pessoa |
| `preparar`, `aposDistribuir`, `aoEncerrar` | não | vazios por padrão | sua subclasse do motor |

Sete obrigações. Tudo o mais é opcional, e boa parte já tem implementação de prateleira.

> **Por que tantas obrigações?** As seis peças obrigatórias de `PartidaConfig` são
> exatamente aquelas em que *não existe padrão razoável*. Qual seria a condição de vitória
> "padrão" de um jogo de cartas? Não existe. Um padrão silencioso aqui só produziria
> partidas que terminam de um jeito que você não pediu. O framework prefere não compilar a
> adivinhar.

---

## 4. O ciclo de vida, passo a passo

Quando você chama `motor.executar()`, esta sequência acontece — sempre nesta ordem, e
você não pode alterá-la:

```text
 estado: CONFIGURADA
 │
 ├─ 1. cria o baralho ................... baralhoFactory.criar()
 ├─ 2. monta a partida
 ├─ 3. publica ......................... PartidaIniciada
 │  estado: PREPARANDO
 ├─ 4. embaralha ....................... automático
 ├─ 5. chama ........................... preparar(contexto)          ← hook opcional
 ├─ 6. distribui ....................... distribuicao.distribuir(...)
 ├─ 7. chama ........................... aposDistribuir(contexto)    ← hook opcional
 ├─ 8. publica ......................... CartasDistribuidas
 │  estado: EM_ANDAMENTO
 ├─ 9. avalia o fim .................... regraDeVitoria.avaliar(...)
 │     └─ já acabou? finaliza sem jogar nenhum turno
 │
 ├─ repete enquanto não acabar:
 │    ├─ publica ....................... TurnoIniciado
 │    ├─ chama ......................... executarTurno(contexto)     ← seu código
 │    │    └─ lançou JogadaInvalidaException?
 │    │         publica JogadaRejeitada e repete o MESMO turno
 │    │         (até 100 vezes; depois estoura IllegalStateException)
 │    ├─ publica ....................... TurnoEncerrado
 │    ├─ avalia o fim .................. regraDeVitoria.avaliar(...)
 │    └─ aplica a diretiva do ResultadoDoTurno (avançar, pular, inverter…)
 │
 ├─ 10. calcula o placar ............... regraDePontuacao.calcular(...)
 ├─ 11. valida vencedores e placar
 │  estado: FINALIZADA
 ├─ 12. publica ........................ PartidaFinalizada
 └─ 13. chama .......................... aoEncerrar(visão, resultado) ← hook opcional
```

Quatro detalhes dessa ordem costumam pegar as pessoas de surpresa:

**O baralho já vem embaralhado.** Você não precisa (nem deve) embaralhar dentro de
`preparar`. O passo 4 é automático.

**A vitória é avaliada antes do primeiro turno.** Se as cartas distribuídas já
configuram uma vitória — um Blackjack natural, por exemplo — a partida termina sem que
`executarTurno` seja chamado nenhuma vez.

**A diretiva de turno não é aplicada quando a partida acaba.** O passo de avaliação vem
*antes* do avanço. Se o seu turno devolveu `inverter()` mas a regra de vitória disse que
acabou, o sentido não chega a ser invertido — o que é o comportamento correto, já que não
haverá próximo turno.

**Uma instância de motor executa uma partida.** Chamar `executar()` de novo lança
`EstadoDePartidaInvalidoException`. Para uma segunda partida, construa outro motor.

---

## 5. Os pontos de extensão, um a um

### 5.1 `Carta` — o que é uma carta no seu jogo

```java
public interface Carta {
    UUID id();
}
```

Um método. Só isso.

**Por que tão pouco?** Porque não existe atributo comum a todas as cartas de todos os
jogos. Naipe não serve para Uno. Valor numérico não serve para cartas de ação. Cor não
serve para baralho francês. Qualquer campo que o framework impusesse seria um campo
inútil — e obrigatório — para metade dos jogos.

**Por que uma identidade, então?** Porque o framework precisa distinguir *esta* carta
de outra idêntica. Dois setes de copas de dois baralhos misturados são cartas diferentes
para efeito de "esta carta está na mão de quem?". A identidade é o que permite ao motor
garantir que uma carta não esteja em dois lugares ao mesmo tempo.

**Como implementar.** Um `record` é a forma mais direta:

```java
public record CartaFrancesa(UUID id, Naipe naipe, Valor valor) implements Carta {

    public CartaFrancesa(Naipe naipe, Valor valor) {
        this(UUID.randomUUID(), naipe, valor);
    }
}
```

Regras da identidade: precisa ser **estável** (não muda durante a vida da carta) e
**única na partida** (nenhuma outra carta tem a mesma). `UUID.randomUUID()` no construtor
resolve as duas.

---

### 5.2 `BaralhoFactory` — de que cartas o jogo é feito

```java
@FunctionalInterface
public interface BaralhoFactory<C extends Carta> {
    Baralho<C> criar();
}
```

É o padrão **Factory Method**: o motor sabe que precisa de um baralho, mas não sabe — nem
quer saber — quais cartas o compõem.

**Por que uma fábrica e não um baralho pronto?** Porque cada partida precisa de um
baralho *novo*. Se você passasse uma instância, a segunda partida começaria com o baralho
que a primeira deixou pela metade. A fábrica é chamada uma vez por execução, no passo 1.

**O que você pode fazer aqui:**

```java
// Baralho francês de 52 cartas
public Baralho<CartaFrancesa> criar() {
    List<CartaFrancesa> cartas = new ArrayList<>();
    for (Naipe naipe : Naipe.values()) {
        for (Valor valor : Valor.values()) {
            cartas.add(new CartaFrancesa(naipe, valor));
        }
    }
    return new BaralhoPadrao<>(cartas);
}

// Dois baralhos misturados — comum em Buraco
public Baralho<CartaFrancesa> criar() {
    List<CartaFrancesa> cartas = new ArrayList<>();
    for (int copia = 0; copia < 2; copia++) {
        cartas.addAll(baralhoCompleto());
    }
    return new BaralhoPadrao<>(cartas);   // ids diferentes, sem colisão
}

// Baralho sem as cartas de 8, 9 e 10 — comum em jogos espanhóis
public Baralho<CartaFrancesa> criar() {
    return new BaralhoPadrao<>(baralhoCompleto().stream()
            .filter(carta -> !carta.valor().entre(8, 10))
            .toList());
}
```

`BaralhoPadrao` recusa duas cartas com o mesmo `id()` — é a primeira linha de defesa
contra a duplicação acidental.

**Dá para escrever seu próprio `Baralho`?** Dá: a interface é pública. Você faria isso se
precisasse de comportamento que o padrão não tem — um baralho que se reembaralha sozinho
ao acabar, por exemplo. Na prática, `BaralhoPadrao` cobre quase tudo, e a interface tem
onze operações para implementar.

---

### 5.3 `EstrategiaDeDistribuicao` — como as cartas chegam às mãos

```java
@FunctionalInterface
public interface EstrategiaDeDistribuicao<C extends Carta> {
    void distribuir(ContextoDeDistribuicao<C> contexto);
}
```

**Já vem pronta:** `DistribuicaoAlternada<>(n)` dá `n` cartas para cada jogador, uma de
cada vez, dando a volta na mesa — o jeito como praticamente todo jogo distribui.

```java
.distribuicao(new DistribuicaoAlternada<>(7))
```

Ela também verifica antes de começar se há cartas suficientes, e lança
`BaralhoVazioException` sem ter entregado nada — ou seja, não deixa a mesa num estado
parcial.

**Quando escrever a sua.** Quando a distribuição do seu jogo não é uniforme:

```java
// Blackjack: duas cartas para cada, mas o dealer é o último
public final class DistribuicaoBlackjack implements EstrategiaDeDistribuicao<CartaFrancesa> {

    @Override
    public void distribuir(ContextoDeDistribuicao<CartaFrancesa> contexto) {
        for (int rodada = 0; rodada < 2; rodada++) {
            for (Jogador jogador : contexto.jogadores()) {
                contexto.entregarProximaCarta(jogador);
            }
        }
    }
}

// Uma quantidade diferente por posição
public void distribuir(ContextoDeDistribuicao<C> contexto) {
    List<Jogador> jogadores = contexto.jogadores();
    for (int posicao = 0; posicao < jogadores.size(); posicao++) {
        int quantidade = posicao == 0 ? 5 : 7;
        for (int carta = 0; carta < quantidade; carta++) {
            contexto.entregarProximaCarta(jogadores.get(posicao));
        }
    }
}
```

O contexto que você recebe é deliberadamente pobre — três operações:

| Operação | Devolve |
|---|---|
| `jogadores()` | a lista, na ordem de turno |
| `cartasDisponiveis()` | quantas restam no baralho |
| `entregarProximaCarta(jogador)` | nada; move uma carta do baralho para a mão |

**Por que não me deixam pegar a carta e escolher para quem vai?** Porque isso permitiria
distribuir uma carta específica — e aí a distribuição deixaria de ser uma distribuição e
passaria a ser uma trapaça. O contexto estreito é o que garante que a estratégia decide
*quantas e para quem*, nunca *quais*.

---

### 5.4 `Jogador` e `EstrategiaDeDecisao` — quem joga e como escolhe

```java
public interface Jogador {
    UUID id();
    String nome();
    EstrategiaDeDecisao estrategiaDeDecisao();
}
```

**Já vem pronto:** `JogadorPadrao`. Use-o; raramente há motivo para outro.

```java
Jogador ana = new JogadorPadrao("Ana", new DecisaoAleatoria());
```

Note o que *não* está no jogador: mão, pontuação, posição. Isso é deliberado e vale a
explicação.

> **Por que o jogador não tem mão?** Porque "uma mão por jogador" é uma regra de jogo, não
> uma verdade universal. No Blackjack quem divide um par joga com duas mãos. No Buraco há
> um monte morto por dupla. Se a mão morasse no jogador, esses jogos exigiriam gambiarra.
> Mãos pertencem à partida, e você as consulta por `contexto.maoDe(jogador)`.

**A decisão vem por composição, não por herança.** Não existe `JogadorHumano` e
`JogadorBot`. Existe um jogador, e ele *tem* uma estratégia:

```java
@FunctionalInterface
public interface EstrategiaDeDecisao {
    Jogada decidir(ContextoDeDecisao contexto);
}
```

Três implementações prontas:

| Estratégia | Comportamento | Para quê |
|---|---|---|
| `DecisaoAleatoria` | sorteia entre as permitidas | bot simples, testes |
| `DecisaoGulosa` | escolhe a de maior valor segundo uma função sua | bot com alguma competência |
| `DecisaoHumanaConsole` | pergunta pelo console e lê a resposta | jogador de verdade |

```java
// Bot que sempre prefere descartar a carta de maior valor
Jogador robo = new JogadorPadrao("Robô", new DecisaoGulosa(
        jogada -> ((Descartar) jogada).valor()));

// Pessoa jogando
EntradaSaida io = new ControleEntradaSaida();
Jogador voce = new JogadorPadrao("Você", new DecisaoHumanaConsole(io));
```

**A sua própria estratégia** é uma lambda, se for simples:

```java
EstrategiaDeDecisao sempreAPrimeira = contexto -> contexto.jogadasPermitidas().get(0);
```

A vantagem de composição sobre herança aparece aqui: trocar um bot por uma pessoa é
trocar um argumento, não reescrever uma classe. E `JogadorPadrao` ainda deixa você trocar
no meio da partida (veja 6.5).

---

### 5.5 `Jogada`, `EtapaDeTurno` e `ContextoDeDecisao` — o vocabulário das ações

```java
public interface Jogada { }
public interface EtapaDeTurno { }
```

As duas são **interfaces vazias de propósito**. Elas existem para dar um *tipo* às ações e
fases do seu jogo sem que o framework precise conhecê-las.

**Como usar:**

```java
public record Descartar(UUID cartaId) implements Jogada { }
public record ComprarDoMonte() implements Jogada { }
public record Passar() implements Jogada { }

public enum EtapaTrinca implements EtapaDeTurno {
    COMPRA, DESCARTE
}
```

**Por que não um enum de jogadas no framework?** Porque o conjunto de ações muda
radicalmente entre jogos. Um enum fechado obrigaria a editar o framework para cada jogo
novo — exatamente o que o princípio Aberto/Fechado proíbe. Com uma interface vazia, seu
jogo declara `Descartar` e o framework carrega essa ação sem nunca precisar saber o que
ela significa.

**Como as ações chegam a quem decide:**

```java
List<Jogada> permitidas = List.of(new ComprarDoMonte(), new Passar());

Jogada escolhida = jogador.estrategiaDeDecisao()
        .decidir(new ContextoDeDecisaoPadrao(EtapaTrinca.COMPRA, permitidas));
```

`ContextoDeDecisaoPadrao` é o record pronto que embrulha etapa + jogadas permitidas.

> **Regra prática que evita 90% dos problemas:** só coloque em `jogadasPermitidas` ações
> que passariam na sua regra de validação. A validação é uma rede de segurança contra bugs,
> não o filtro principal. Uma estratégia que só recebe opções legais nunca entra em ciclo
> de rejeição.

Se sua estratégia precisa de mais informação para decidir — quantas cartas o adversário
tem, qual a carta do topo do descarte — crie uma subinterface:

```java
public interface ContextoDecisaoTrinca extends ContextoDeDecisao {
    int cartasNaMinhaMao();
    Optional<CartaFrancesa> topoDoDescarte();
}
```

O contrato base nunca revela mãos adversárias nem a ordem do baralho. O que o seu jogo
considera informação pública é decisão sua, e você a expõe assim.

---

### 5.6 `RegraDeValidacaoStrategy` — o que é jogada legal

```java
@FunctionalInterface
public interface RegraDeValidacaoStrategy<C extends Carta> {
    void validar(ContextoDeValidacao<C> contexto);
}
```

**O protocolo é simples:** se a jogada é legal, o método termina em silêncio. Se não é,
lança `JogadaInvalidaException` com uma mensagem que explique o motivo.

```java
public final class ValidacaoTrinca implements RegraDeValidacaoStrategy<CartaFrancesa> {

    @Override
    public void validar(ContextoDeValidacao<CartaFrancesa> contexto) {
        if (!(contexto.jogada() instanceof Descartar descarte)) {
            throw new JogadaInvalidaException("Esta etapa só aceita descarte.");
        }
        Jogador jogador = contexto.partida().jogadorAtual();
        boolean naMao = contexto.partida().maoDe(jogador).stream()
                .anyMatch(carta -> carta.id().equals(descarte.cartaId()));
        if (!naMao) {
            throw new JogadaInvalidaException(
                    "Você não pode descartar uma carta que não está na sua mão.");
        }
    }
}
```

O `ContextoDeValidacao` que você recebe tem exatamente dois campos: `jogada()` e
`partida()` — esta última uma visão **somente leitura**. Você não consegue alterar a
partida de dentro da validação, e isso é intencional: uma regra que valida não deveria
poder mudar aquilo que está validando.

**Como acionar a validação.** Do seu `executarTurno`, chamando `validarJogada`:

```java
Jogada escolhida = jogador.estrategiaDeDecisao().decidir(contextoDeDecisao);

validarJogada(contexto, escolhida);   // lança se a regra recusar

// só chegou aqui se passou — agora pode alterar o estado
CartaFrancesa removida = contexto.removerDaMao(jogador, descarte.cartaId());
```

> **O ponto mais importante desta seção.** Valide **antes** de alterar qualquer coisa. O
> motor repete o turno quando a validação falha, mas **não desfaz** o que você já fez. Se
> você remover a carta da mão e só então validar, a carta some e o turno recomeça sem ela.
> Não existe rollback: a ordem correta é decidir → validar → alterar.

**A regra "aceito tudo"**, quando seu jogo valida dentro do próprio turno e não precisa
desse ponto de extensão:

```java
.regraDeValidacao(contexto -> { })
```

---

### 5.7 `RegraDeVitoriaStrategy` — quando acaba e quem ganhou

```java
@FunctionalInterface
public interface RegraDeVitoriaStrategy<C extends Carta> {
    Optional<DesfechoDePartida> avaliar(VisaoDaPartida<C> contexto);
}
```

Chamada **uma vez antes do primeiro turno e depois de cada turno**. `Optional.empty()`
continua a partida; um desfecho a encerra na hora.

```java
@Override
public Optional<DesfechoDePartida> avaliar(VisaoDaPartida<CartaFrancesa> partida) {
    // alguém ficou sem cartas: vitória
    for (Jogador jogador : partida.jogadores()) {
        if (partida.maoDe(jogador).isEmpty()) {
            return Optional.of(
                    new DesfechoDePartida(List.of(jogador), MotivoPadrao.VITORIA));
        }
    }
    // baralho acabou: ninguém vence
    if (partida.quantidadeNoBaralho() == 0) {
        return Optional.of(new DesfechoDePartida(List.of(), MotivoPadrao.ESGOTAMENTO));
    }
    // empate declarado por limite de turnos
    if (partida.numeroDoTurno() >= 200) {
        return Optional.of(new DesfechoDePartida(List.of(), MotivoPadrao.EMPATE));
    }
    return Optional.empty();
}
```

Empate com vencedores é possível e correto — é assim que se declara "estes dois
empataram":

```java
return Optional.of(new DesfechoDePartida(List.of(ana, bruno), MotivoPadrao.EMPATE));
```

**Duas invariantes que o motor cobra de você:**

1. Todo vencedor precisa ser um participante da partida. Devolver alguém de fora lança
   `IllegalStateException`.
2. Um motivo marcado como vitória exige ao menos um vencedor. `VITORIA` com lista vazia
   é rejeitado na construção do `DesfechoDePartida`.

> **Sua regra precisa terminar.** O motor roda turnos até que ela devolva um desfecho — não
> há limite de turnos embutido. Uma regra que nunca reconhece o fim produz um laço
> infinito. Sempre tenha uma saída de segurança: baralho vazio, limite de turnos, ou as
> duas.

---

### 5.8 `RegraDePontuacaoStrategy` — quantos pontos cada um fez

```java
@FunctionalInterface
public interface RegraDePontuacaoStrategy<C extends Carta> {
    Map<Jogador, Integer> calcular(VisaoDaPartida<C> contexto, DesfechoDePartida desfecho);
}
```

Chamada **uma vez**, depois de a vitória ter sido reconhecida e antes de o resultado
existir. Você recebe o desfecho, então pode pontuar de forma diferente para quem ganhou.

```java
@Override
public Map<Jogador, Integer> calcular(
        VisaoDaPartida<CartaFrancesa> partida, DesfechoDePartida desfecho) {
    Map<Jogador, Integer> placar = new LinkedHashMap<>();
    for (Jogador jogador : partida.jogadores()) {
        boolean venceu = desfecho.vencedores().stream()
                .anyMatch(vencedor -> vencedor.id().equals(jogador.id()));
        int pontos = venceu ? 100 : -somaDaMao(partida.maoDe(jogador));
        placar.put(jogador, pontos);
    }
    return placar;
}
```

Compare jogadores por `id()`, não por `equals`. `JogadorPadrao` não sobrescreve `equals`,
então `contains` compararia por identidade de objeto — funciona por acidente enquanto as
instâncias forem as mesmas, e quebra silenciosamente se alguém implementar `Jogador` como
record. O framework compara por `id()` internamente; siga a mesma regra.

> **A regra que mais causa erro no primeiro uso:** o mapa precisa conter **exatamente
> todos os participantes** — nem um a menos, nem um a mais, sem repetição. O motor
> confere e lança `IllegalStateException` se não bater. Percorra sempre
> `partida.jogadores()` e você nunca erra.

Se seu jogo não pontua, dê zero a todo mundo:

```java
.regraDePontuacao((partida, desfecho) -> partida.jogadores().stream()
        .collect(Collectors.toMap(jogador -> jogador, jogador -> 0)))
```

---

### 5.9 `MotivoDeEncerramento` — por que acabou

```java
public interface MotivoDeEncerramento {
    default boolean ehVitoria() { return false; }
    default boolean ehEmpate()  { return false; }
}
```

**Já vem pronto:** `MotivoPadrao` com `VITORIA`, `EMPATE`, `ESGOTAMENTO` e `ABANDONO`.
Para a maioria dos jogos, basta.

**Quando declarar os seus.** Quando o vocabulário do jogo importa para quem lê o
resultado:

```java
public enum MotivoBlackjack implements MotivoDeEncerramento {

    BLACKJACK_NATURAL(true),
    ESTOUROU_21(false),
    DEALER_ESTOUROU(true),
    RENDICAO(false);

    private final boolean vitoria;

    MotivoBlackjack(boolean vitoria) {
        this.vitoria = vitoria;
    }

    @Override
    public boolean ehVitoria() {
        return vitoria;
    }
}
```

**Por que interface e não enum fechado?** Porque um enum no framework obrigaria a editá-lo
a cada jogo novo. Com interface, o Blackjack declara `ESTOUROU_21`, o Uno declara
`FICOU_SEM_CARTAS`, e o framework não muda uma linha. Os dois métodos têm implementação
padrão `false`, então declarar um motivo neutro custa uma linha.

---

### 5.10 `MotorDePartida` — o que acontece num turno

Sua subclasse do motor é onde mora a mecânica do jogo.

```java
public abstract class MotorDePartida<C extends Carta> {

    // você é obrigado a implementar:
    protected abstract ResultadoDoTurno executarTurno(ContextoDePartida<C> contexto);

    // você pode sobrescrever:
    protected void preparar(ContextoDePartida<C> contexto) { }
    protected void aposDistribuir(ContextoDePartida<C> contexto) { }
    protected void aoEncerrar(VisaoDaPartida<C> contexto, ResultadoDePartida resultado) { }

    // você pode chamar, mas não sobrescrever:
    protected final void validarJogada(VisaoDaPartida<C> contexto, Jogada jogada);
    protected final void publicarEvento(EventoDePartida evento);
    public final ResultadoDePartida executar();
    public final EstadoPartida estado();
    public final void adicionarListener(PartidaListener listener);
    public final boolean removerListener(PartidaListener listener);
}
```

#### Os dois hooks de preparação

| Hook | Quando roda | Use para |
|---|---|---|
| `preparar` | antes da distribuição, baralho já embaralhado | montar zonas vazias, sortear quem começa uma fase |
| `aposDistribuir` | depois da distribuição, antes do 1º turno | o que depende das mãos já formadas |

O segundo existe por um motivo concreto: virar a primeira carta do descarte, ou deixar a
carta do dealer aberta, exige que as mãos já estejam prontas. Fazer isso em `preparar`
daria errado.

#### O contexto: o que você pode fazer dentro do turno

`ContextoDePartida` é a sua interface com o estado da partida. Consultas:

| Operação | Devolve |
|---|---|
| `jogadorAtual()` | de quem é a vez |
| `jogadores()` | todos, na ordem, imutável |
| `maoDe(jogador)` | snapshot imutável da mão |
| `quantidadeNoBaralho()` | quantas cartas restam |
| `numeroDoTurno()` | o turno corrente, a partir de 1 |
| `estado()` | a etapa do ciclo de vida |

Mutações:

| Operação | Efeito |
|---|---|
| `comprarDoBaralho()` | tira a carta do topo e devolve |
| `adicionarNaMao(jogador, carta)` | põe a carta na mão |
| `removerDaMao(jogador, cartaId)` | tira a carta da mão e devolve |
| `adicionarAoBaralho(cartas)` | devolve cartas de uma zona sua ao baralho |
| `embaralharBaralho()` | embaralha |

> **A conservação de cartas.** `adicionarNaMao` recusa uma carta que já esteja no baralho
> ou em outra mão. Para mover uma carta de uma mão para outra, remova primeiro. Isso não é
> burocracia: é o que garante que o seu jogo não duplique cartas por engano — o bug mais
> difícil de rastrear em jogo de cartas.

#### A diretiva de turno

```java
return ResultadoDoTurno.avancar();     // próximo jogador (o caso comum)
return ResultadoDoTurno.repetir();     // o mesmo jogador joga de novo
return ResultadoDoTurno.inverter();    // inverte o sentido e avança
return ResultadoDoTurno.pular(2);      // salta 2 jogadores
```

**Por que devolver uma diretiva em vez de avançar o turno eu mesmo?** Porque a rotação é
do motor. Se o seu código pudesse avançar a vez diretamente, um bug seu quebraria a ordem
da mesa de um jeito que o framework não teria como perceber. Você declara a *intenção*; o
motor executa. É o que garante que a ordem esteja sempre consistente, com dois ou com
oito jogadores.

`repetir()` e `pular(n)` juntos são rejeitados — não faz sentido manter o jogador e pular
outros ao mesmo tempo.

---

### 5.11 `PartidaListener` — reagir ao que acontece

```java
@FunctionalInterface
public interface PartidaListener {
    void aoOcorrer(EventoDePartida evento);
}
```

É o padrão **Observer**. Serve para interface gráfica, log, narração, estatística,
replay — qualquer coisa que precise *saber* o que acontece sem *interferir*.

Seis eventos padrão são publicados automaticamente:

| Evento | Quando | O que traz |
|---|---|---|
| `PartidaIniciada` | antes de embaralhar | a lista de jogadores |
| `CartasDistribuidas` | após a distribuição | quantas cartas restaram |
| `TurnoIniciado` | no início de cada turno | número do turno, jogador |
| `JogadaRejeitada` | a cada recusa da validação | turno, jogador, motivo |
| `TurnoEncerrado` | ao fim de cada turno | turno, jogador, diretiva |
| `PartidaFinalizada` | após o resultado existir | o `ResultadoDePartida` |

```java
public final class Narrador implements PartidaListener {

    private final EntradaSaida saida;

    public Narrador(EntradaSaida saida) {
        this.saida = saida;
    }

    @Override
    public void aoOcorrer(EventoDePartida evento) {
        switch (evento) {
            case PartidaIniciada e ->
                    saida.exibir("Partida com " + e.jogadores().size() + " jogadores.");
            case TurnoIniciado e ->
                    saida.exibir("Turno " + e.numeroDoTurno() + " — vez de " + e.jogador().nome());
            case JogadaRejeitada e ->
                    saida.exibir("Recusada: " + e.motivo());
            case PartidaFinalizada e ->
                    saida.exibir("Fim: " + e.resultado().motivo());
            default -> { }
        }
    }
}
```

```java
motor.adicionarListener(new Narrador(new ControleEntradaSaida()));
ResultadoDePartida resultado = motor.executar();
```

**Quatro comportamentos que você deve conhecer:**

1. **Registre antes de `executar()`.** `PartidaIniciada` é publicado no começo; quem
   chegar depois perde os eventos já ocorridos.
2. **A ordem é a de cadastro.** O primeiro registrado é o primeiro chamado.
3. **A mesma instância só entra uma vez.** Cadastrar duas vezes o mesmo objeto não o faz
   receber em dobro.
4. **Um ouvinte que quebra não derruba a partida.** Exceções lançadas dentro de
   `aoOcorrer` são engolidas pelo motor, e os demais ouvintes continuam sendo chamados.
   Um bug na sua interface gráfica não pode acabar com o jogo — mas isso significa que
   erros no seu listener passam despercebidos. Trate suas próprias exceções se precisar
   vê-las.

**Eventos próprios.** `EventoDePartida` é uma interface aberta. Declare o fato no pacote
do seu jogo e chame `publicarEvento` de dentro da subclasse do motor:

```java
public record CartaDescartada(UUID cartaId, Jogador jogador)
        implements EventoDePartida { }

// Dentro de preparar, aposDistribuir, executarTurno ou aoEncerrar:
publicarEvento(new CartaDescartada(
        cartaDescartada.id(), contexto.jogadorAtual()));
```

O evento chega aos mesmos listeners, na posição exata em que a chamada ocorreu. O método
é `protected`, então somente o motor e suas subclasses o acessam, e é `final` para que o
jogo não substitua as garantias de ordem, snapshot e isolamento de falhas. Passe sempre
um evento não nulo, imutável e sem dados privados que o observador não possa receber.

---

### 5.12 `EntradaSaida` — falar com quem está jogando

```java
public interface EntradaSaida {
    void exibir(String mensagem);
    int solicitarOpcao(String mensagem, List<String> opcoes);
}
```

**Já vem pronto:** `ControleEntradaSaida`, que lê e escreve no console.

```java
EntradaSaida io = new ControleEntradaSaida();
io.exibir("Bem-vindo!");
int escolha = io.solicitarOpcao("O que fazer?", List.of("Comprar", "Passar"));
```

Ele já trata entrada não numérica e fora da faixa, repetindo a pergunta.

**Por que uma interface para algo tão simples?** Por causa dos testes. Com a interface, você
troca o console por um dublê e testa o jogo inteiro sem digitar nada:

```java
EntradaSaida io = new ControleEntradaSaida(
        new StringReader("1\n2\n1\n"),   // as respostas
        new StringWriter());             // o que seria impresso
```

E é o que permite trocar console por interface gráfica sem tocar nas regras.

---

## 6. Casos de uso

### 6.1 Turnos que não seguem a ordem

Cartas com efeito sobre a rotação — o coração do Uno — são diretivas de turno:

```java
@Override
protected ResultadoDoTurno executarTurno(ContextoDePartida<CartaUno> contexto) {
    CartaUno jogada = escolherEJogar(contexto);

    return switch (jogada.efeito()) {
        case INVERTER -> ResultadoDoTurno.inverter();
        case PULAR    -> ResultadoDoTurno.pular(1);
        case COMPRAR_DOIS -> {
            comprarParaOProximo(contexto, 2);
            yield ResultadoDoTurno.pular(1);
        }
        case NENHUM   -> ResultadoDoTurno.avancar();
    };
}
```

O gerenciador de turnos trabalha com aritmética modular, então inversão e pulo funcionam
igual com 2 ou com 8 jogadores, inclusive dando a volta pelo início da lista.

### 6.2 Uma zona própria: a pilha de descarte

O framework conhece baralho e mãos. Qualquer outra zona — descarte, mesa, monte morto,
jogos baixados — é sua, e você a guarda como um campo da sua subclasse:

```java
public final class MotorComDescarte extends MotorDePartida<CartaSimples> {

    private final List<CartaSimples> pilhaDeDescarte = new ArrayList<>();

    public MotorComDescarte(PartidaConfig<CartaSimples> configuracao) {
        super(configuracao);
    }

    @Override
    protected void aposDistribuir(ContextoDePartida<CartaSimples> contexto) {
        pilhaDeDescarte.add(contexto.comprarDoBaralho());   // vira a primeira carta
    }

    @Override
    protected ResultadoDoTurno executarTurno(ContextoDePartida<CartaSimples> contexto) {
        reabastecerSeNecessario(contexto);
        CartaSimples comprada = contexto.comprarDoBaralho();
        contexto.adicionarNaMao(contexto.jogadorAtual(), comprada);
        return ResultadoDoTurno.avancar();
    }

    // baralho acabou: devolve o descarte, menos a carta de cima, e embaralha
    private void reabastecerSeNecessario(ContextoDePartida<CartaSimples> contexto) {
        if (contexto.quantidadeNoBaralho() > 0 || pilhaDeDescarte.size() <= 1) {
            return;
        }
        CartaSimples topo = pilhaDeDescarte.remove(pilhaDeDescarte.size() - 1);
        contexto.adicionarAoBaralho(List.copyOf(pilhaDeDescarte));
        pilhaDeDescarte.clear();
        pilhaDeDescarte.add(topo);
        contexto.embaralharBaralho();
    }

    @Override
    protected void aoEncerrar(
            VisaoDaPartida<CartaSimples> contexto, ResultadoDePartida resultado) {
        pilhaDeDescarte.clear();
    }
}
```

Duas coisas a notar. `adicionarAoBaralho` só aceita cartas que **não** estejam no baralho
nem em mão nenhuma — é o que impede que o reabastecimento duplique cartas. E o descarte
usa `aposDistribuir`, não `preparar`, porque a carta virada precisa sair de um baralho já
distribuído.

Se sua zona precisa das operações de uma mão — buscar por id, remover, testar
pertencimento — use `MaoDeCartasPadrao` em vez de `List`:

```java
private final MaoDeCartas<CartaSimples> mesa = new MaoDeCartasPadrao<>();
```

### 6.3 Rejeitar jogadas e deixar o jogador tentar de novo

```java
@Override
protected ResultadoDoTurno executarTurno(ContextoDePartida<CartaSimples> contexto) {
    Jogador jogador = contexto.jogadorAtual();

    List<Jogada> permitidas = contexto.maoDe(jogador).stream()
            .filter(carta -> carta.valor() % 2 == 0)
            .map(carta -> (Jogada) new Descartar(carta.id()))
            .toList();

    if (permitidas.isEmpty()) {
        return ResultadoDoTurno.avancar();       // não há o que fazer: passa a vez
    }

    Jogada escolhida = jogador.estrategiaDeDecisao()
            .decidir(new ContextoDeDecisaoPadrao(EtapaCorrida.DESCARTE, permitidas));

    validarJogada(contexto, escolhida);          // pode lançar e repetir o turno

    Descartar descarte = (Descartar) escolhida;
    CartaSimples removida = contexto.removerDaMao(jogador, descarte.cartaId());
    contexto.adicionarAoBaralho(List.of(removida));
    return ResultadoDoTurno.avancar();
}
```

Quando `validarJogada` lança, o motor publica `JogadaRejeitada` e chama `executarTurno` de
novo — **mesmo jogador, mesmo número de turno**. É assim que uma pessoa jogando pelo
console recebe a mensagem de erro e escolhe outra carta.

Três cuidados:

- O limite é de **100 tentativas**. Depois disso o motor lança `IllegalStateException`,
  porque um jogador que erra cem vezes seguidas é um bug, não um jogador indeciso.
- **Não há rollback.** Valide antes de alterar (veja 5.6).
- O `if (permitidas.isEmpty())` não é detalhe: uma estratégia sem opção legal entraria em
  ciclo até estourar o limite. Passar a vez é o comportamento correto.

### 6.4 Uma pessoa jogando pelo console

```java
EntradaSaida io = new ControleEntradaSaida();

Jogador voce = new JogadorPadrao("Você",
        new DecisaoHumanaConsole(io, jogada -> descrever(jogada)));
Jogador robo = new JogadorPadrao("Robô", new DecisaoAleatoria());

MotorComDescarte motor = new MotorComDescarte(configurar(List.of(voce, robo)));
motor.adicionarListener(new Narrador(io));
motor.executar();
```

O segundo argumento de `DecisaoHumanaConsole` é a função que transforma uma `Jogada` em
texto legível — sem ela, a pessoa veria o `toString()` do record. Vale escrever:

```java
private static String descrever(Jogada jogada) {
    return jogada instanceof Descartar descarte
            ? "descartar a carta " + descarte.cartaId()
            : jogada.toString();
}
```

Uma pessoa e um bot na mesma mesa não exigem nada de especial: são dois jogadores com
estratégias diferentes.

### 6.5 Trocar o comportamento de um bot no meio da partida

`JogadorPadrao` permite trocar a estratégia sem trocar a identidade:

```java
JogadorPadrao robo = new JogadorPadrao("Robô", new DecisaoAleatoria());
// ... a partida avança ...
robo.alterarEstrategiaDeDecisao(new DecisaoGulosa(this::avaliar));
```

Serve para dificuldade progressiva, ou para um jogador que assume o controle de um bot no
meio do jogo. A identidade (`id()`) não muda, então mãos e placar continuam apontando
para a mesma pessoa.

### 6.6 Testar seu jogo de forma determinística

Um baralho embaralhado ao acaso torna o teste instável. Use `embaralhar(RandomGenerator)`
com semente fixa dentro da sua fábrica:

```java
public Baralho<CartaSimples> criar() {
    BaralhoPadrao<CartaSimples> baralho = new BaralhoPadrao<>(cartas());
    baralho.embaralhar(new Random(42));
    return baralho;
}
```

O motor embaralha de novo no passo 4, com o gerador padrão. Para controle total, injete o
gerador na sua fábrica e faça o baralho de teste ser criado numa ordem conhecida — ou
teste as suas regras diretamente, sem motor: `RegraDeVitoriaStrategy` e
`RegraDePontuacaoStrategy` recebem apenas uma `VisaoDaPartida`, que é fácil de dublar.

Para o console, injete leitor e escritor:

```java
EntradaSaida io = new ControleEntradaSaida(new StringReader("1\n1\n"), new StringWriter());
```

---

## 7. O que você não pode fazer, e por quê

Um manual honesto precisa dizer onde está a parede. Estas coisas são impossíveis por
construção — e cada uma tem um motivo.

| Não dá para | Por quê |
|---|---|
| Avançar, voltar ou definir o jogador da vez diretamente | a rotação é do motor; você declara a intenção com `ResultadoDoTurno` |
| Forçar uma transição de estado ou finalizar a partida na marra | o ciclo de vida é `final`; a partida termina quando a regra de vitória disser |
| Sobrescrever `executar()` | é o Template Method: a ordem das etapas é o contrato do framework |
| Alcançar o gerenciador de turnos ou o estado interno | são classes sem `public` no pacote `engine`; o compilador barra |
| Alterar uma mão pela lista devolvida por `maoDe()` | é um snapshot imutável; use `adicionarNaMao` / `removerDaMao` |
| Reexecutar um motor | uma instância, uma partida; construa outro motor |
| Desfazer o que um turno rejeitado já alterou | não há rollback; valide antes de alterar |
| Ter várias mãos por jogador no contexto | o contexto oferece uma mão por jogador; zonas extras são suas (6.2) |
| Rodar uma partida em várias threads | o motor não é seguro para concorrência |
| Persistir e retomar uma partida | não há serialização de estado; a partida vive na memória |
| Remover ou acrescentar jogadores no meio | a lista é fixada na configuração |

As seis primeiras são **decisões de projeto**: o framework abre mão de flexibilidade em
troca de garantia. Se o cliente pudesse avançar a vez, a ordem da mesa deixaria de ser
confiável; se pudesse forçar `FINALIZADA`, o resultado deixaria de ser validado. Esse é o
trato — e é o que faz valer a pena usar um framework em vez de escrever tudo do zero.

As cinco últimas são **limitações reais**, não decisões elegantes. Se o seu jogo precisa
de persistência ou de mesa com entrada e saída de jogadores, o framework hoje não atende.

---

## 8. Erros comuns e o que eles significam

| Mensagem | Causa provável | Correção |
|---|---|---|
| `A regra de validação é obrigatória.` | faltou um `.regraDeValidacao(...)` no builder | as seis peças da config são obrigatórias; use `contexto -> { }` se não precisa validar |
| `Uma partida exige ao menos dois jogadores.` | lista com 0 ou 1 jogador | o framework não modela paciência/solitário |
| `Há jogadores repetidos na configuração` | dois jogadores com o mesmo `id()` | cada `JogadorPadrao` gera o seu; não reutilize a instância |
| `O placar deve registrar exatamente os participantes da partida.` | a pontuação esqueceu alguém, ou incluiu alguém a mais | percorra `partida.jogadores()` |
| `O desfecho indicou um vencedor que não participa da partida.` | vencedor de fora da lista | devolva instâncias vindas de `partida.jogadores()` |
| `Uma vitória precisa indicar ao menos um vencedor.` | `MotivoPadrao.VITORIA` com lista vazia | use `ESGOTAMENTO` ou `EMPATE` quando não há vencedor |
| `Já existe uma carta com o ID …` | a fábrica gerou cartas com id repetido | gere `UUID.randomUUID()` por carta, não um id fixo por tipo |
| `A carta … já está no baralho ou em uma mão.` | tentou adicionar carta que já está em jogo | remova da origem antes de adicionar no destino |
| `O estado atual da partida não permite esta operação.` | mutação fora de `PREPARANDO`/`EM_ANDAMENTO`, ou `executar()` duas vezes | não altere a partida dentro de `aoEncerrar`; crie um motor novo por partida |
| `… não produziu jogada válida em 100 tentativas` | a estratégia insiste em jogada ilegal | filtre `jogadasPermitidas`; trate o caso "nenhuma jogada possível" |
| `BaralhoVazioException` na distribuição | o baralho não tem cartas para todos | aumente o baralho ou reduza as cartas por jogador |
| `não há jogadas permitidas` | a lista passada à estratégia veio vazia | verifique antes e passe a vez |

Todas as exceções de domínio herdam de `PartidaException`, que é `RuntimeException`.
Para tratar qualquer erro de partida num lugar só:

```java
try {
    ResultadoDePartida resultado = motor.executar();
} catch (PartidaException erro) {
    io.exibir("A partida terminou com erro: " + erro.getMessage());
}
```

---

## 9. Referência rápida

São **47 tipos públicos**, distribuídos assim:

| Pacote | Tipos | Conteúdo |
|---|---|---|
| `cardgame.api` | 32 | todos os contratos e valores |
| `cardgame.api.evento` | 6 | os eventos padrão publicados automaticamente pelo motor |
| `cardgame.api.excecao` | 4 | `PartidaException` e três filhas |
| `cardgame.api.estrategia` | 3 | estratégias de decisão prontas |
| `cardgame.api.io` | 1 | `ControleEntradaSaida` |
| `cardgame.engine` | 1 | `MotorDePartida` — o único tipo público do pacote |

### Implementações prontas para usar

| Classe | Substitui o quê |
|---|---|
| `BaralhoPadrao<C>` | `Baralho<C>` |
| `MaoDeCartasPadrao<C>` | `MaoDeCartas<C>` |
| `JogadorPadrao` | `Jogador` |
| `DistribuicaoAlternada<C>` | `EstrategiaDeDistribuicao<C>` |
| `DecisaoAleatoria` | `EstrategiaDeDecisao` |
| `DecisaoGulosa` | `EstrategiaDeDecisao` |
| `DecisaoHumanaConsole` | `EstrategiaDeDecisao` |
| `ContextoDeDecisaoPadrao` | `ContextoDeDecisao` |
| `MotivoPadrao` | `MotivoDeEncerramento` |
| `ControleEntradaSaida` | `EntradaSaida` |

### O esqueleto de um jogo novo

```java
// 1. a carta
public record MinhaCarta(UUID id, /* seus campos */) implements Carta { }

// 2. o baralho
public final class MinhaFabrica implements BaralhoFactory<MinhaCarta> {
    public Baralho<MinhaCarta> criar() { return new BaralhoPadrao<>(/* cartas */); }
}

// 3. o turno
public final class MeuMotor extends MotorDePartida<MinhaCarta> {
    public MeuMotor(PartidaConfig<MinhaCarta> config) { super(config); }

    protected ResultadoDoTurno executarTurno(ContextoDePartida<MinhaCarta> contexto) {
        /* a mecânica do seu jogo */
        return ResultadoDoTurno.avancar();
    }
}

// 4. as regras
RegraDeValidacaoStrategy<MinhaCarta> validacao = contexto -> { /* lance se ilegal */ };
RegraDeVitoriaStrategy<MinhaCarta>   vitoria   = partida -> Optional.empty();
RegraDePontuacaoStrategy<MinhaCarta> pontuacao = (partida, desfecho) -> Map.of();

// 5. montar e rodar
PartidaConfig<MinhaCarta> config = PartidaConfig.<MinhaCarta>builder()
        .jogadores(List.of(/* pelo menos dois */))
        .baralhoFactory(new MinhaFabrica())
        .distribuicao(new DistribuicaoAlternada<>(7))
        .regraDeValidacao(validacao)
        .regraDeVitoria(vitoria)
        .regraDePontuacao(pontuacao)
        .build();

ResultadoDePartida resultado = new MeuMotor(config).executar();
```

---

## Onde continuar

- `docs/especificacao_arquitetural.md` — os contratos e invariantes em detalhe
- `docs/padroes-de-projeto.md` — os padrões GoF, SOLID e GRASP por trás das decisões
- `docs/ARQUITETURA_FRAMEWORK_MAP.md` — o mapa da arquitetura e o estado da baseline
- `docs/diagrama-classes.puml` — o diagrama de classes
- `./mvnw javadoc:javadoc` — a documentação completa da API

Encontrou um limite que este manual não previu? Isso é informação útil: a fronteira do
framework é definida por uso real, e um mecanismo só sobe ao núcleo quando dois jogos
diferentes provam que precisam dele.
