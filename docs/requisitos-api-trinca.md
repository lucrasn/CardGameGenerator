# API pública do CardGame Framework - contrato para congelamento

**Destinatários:** Trilhas A, B, C e D
**Clientes de validação:** Trinca e Blackjack
**Origem funcional:** [regras-trinca.md](regras-trinca.md)
**Status:** candidata a congelamento na reunião da Fase 0.

Este documento define a fronteira que as outras trilhas podem implementar em
paralelo. Nomes e responsabilidades abaixo deixam de ser sugestões depois da
aprovação do checklist final. Alterações posteriores exigem comunicação ao grupo e
teste de compatibilidade com os dois jogos clientes.

## 1. Decisões arquiteturais

### 1.1 Framework e aplicações clientes

O framework fornece o fluxo, componentes reutilizáveis e contratos de extensão. A
Trinca e o Blackjack fornecem apenas regras, cartas, ações, estratégias, console e
especializações do motor.

```text
br.edu.uepb.map.cardgame.api       contratos e implementações públicas reutilizáveis
br.edu.uepb.map.cardgame.core      implementação interna do framework
br.edu.uepb.map.trinca             aplicação cliente Trinca
br.edu.uepb.map.blackjack          aplicação cliente Blackjack
```

Regra mecânica: código de `trinca` e `blackjack` pode importar `api`, Java padrão e
seus próprios tipos, mas nunca `cardgame.core`. O `core` nunca importa os jogos.

### 1.2 Template Method público

Para preservar o Template Method definido em `padroes-de-projeto.md`, o contrato é:

```text
api.MotorDePartida (classe abstrata; executar() final)
├── trinca.MotorDeTrinca
└── blackjack.MotorDeBlackjack
```

`MotorDePartida` pertence à API e recebe somente contratos públicos. Estado mutável,
mesa e gerenciador de turnos continuam internos, acessíveis às subclasses apenas por
`ContextoDePartida`, que oferece operações controladas.

Não haverá uma fachada `Partidas` nesta versão: ela esconderia o ponto de extensão
por herança usado para justificar Template Method.

## 2. Catálogo da API por responsável

| Dono | Tipos públicos obrigatórios |
|---|---|
| A | `MotorDePartida`, `PartidaConfig`, `ContextoDePartida`, `EstadoPartida`, `ResultadoDePartida`, `MotivoDeEncerramento` |
| B | `Carta`, `Baralho`, `BaralhoPadrao`, `BaralhoFactory`, `MaoDeCartas`, `MaoDeCartasPadrao`, `EstrategiaDeDistribuicao`, `ContextoDeDistribuicao` |
| C | `Jogador`, `JogadorPadrao`, `Jogada`, `EstrategiaDeDecisao`, `ContextoDeDecisao`, `EtapaDeTurno` |
| D | `RegraDeValidacaoStrategy`, `RegraDeVitoriaStrategy`, `RegraDePontuacaoStrategy`, `ContextoDeValidacao`, `ContextoDeVitoria`, `ContextoDePontuacao`, `AvaliacaoDeVitoria`, `PartidaListener`, `EventoDePartida` e exceções |

Eventos podem ficar em `api.evento` e exceções em `api.excecao`. Os demais tipos
podem permanecer diretamente em `api` para evitar uma hierarquia prematura de
pacotes.

## 3. Cartas, baralho e mão - Trilha B

### 3.1 Carta

`Carta` não expõe `naipe`, `valor`, `cor` ou `símbolo`: esses atributos não existem em
todos os jogos. O framework exige somente identidade estável.

```java
public interface Carta {
    UUID id();
}
```

Cada carta concreta inclui o identificador na sua igualdade. Isso distingue as duas
cartas de mesmo valor e naipe presentes nos dois baralhos da Trinca.

### 3.2 Baralho público e implementação reutilizável

```java
public interface Baralho {
    int quantidadeDeCartas();
    boolean estaVazio();
    Carta comprar();
    void adicionar(Collection<? extends Carta> cartas);
    void embaralhar();
}

public interface BaralhoFactory {
    Baralho criarBaralho();
}
```

`BaralhoPadrao` é uma classe pública final que implementa `Baralho`. Seu construtor
recebe uma coleção, faz cópia defensiva e nunca devolve a coleção interna.
`adicionar` existe para o motor reciclar descartes; também copia a coleção recebida.
`comprar` lança `BaralhoVazioException` quando usado sem carta disponível.

```java
public final class BaralhoDeTrincaFactory implements BaralhoFactory {
    @Override
    public Baralho criarBaralho() {
        return new BaralhoPadrao(cartasDaTrinca());
    }
}
```

`BaralhoBase` deixa de fazer parte da arquitetura. A Trilha B deve transformar o
arquivo atual em `api.BaralhoPadrao`; não deve haver duas implementações padrão com
nomes diferentes.

### 3.3 Mão pública e reutilizável

O enunciado exige mão de cartas como abstração. Ela não pode existir apenas como uma
lista dentro de `core`, pois Trinca e Blackjack precisam consultar mãos e o Blackjack
precisa criar outra mão ao dividir um par.

```java
public interface MaoDeCartas {
    int quantidadeDeCartas();
    List<Carta> cartas();              // cópia/visão imutável
    boolean contem(UUID cartaId);
}
```

`MaoDeCartasPadrao` é pública, final e reutilizável. Ela oferece operações controladas
`adicionar(Carta)` e `remover(UUID)` sem permitir acesso à lista mutável. Um jogador
pode possuir `1..*` mãos: Trinca usa uma; Blackjack pode usar várias após `split`.

### 3.4 Distribuição sem tipos internos

```java
public interface EstrategiaDeDistribuicao {
    void distribuir(ContextoDeDistribuicao contexto);
}

public interface ContextoDeDistribuicao {
    List<Jogador> jogadores();                // imutável
    Carta comprarDoBaralho();
    MaoDeCartas maoPrincipalDe(Jogador jogador);
    void entregarCarta(Jogador jogador, MaoDeCartas mao, Carta carta);
}
```

O contexto valida se jogador, mão e carta pertencem à partida. A distribuição da
Trinca entrega nove cartas alternadamente; a do Blackjack entrega duas.

## 4. Jogadores, ações e decisões - Trilha C

### 4.1 Identidade separada da decisão

```java
public interface Jogador {
    UUID id();
    String nome();
    EstrategiaDeDecisao estrategiaDeDecisao();
}

public interface EstrategiaDeDecisao {
    Jogada decidir(ContextoDeDecisao contexto);
}
```

`JogadorPadrao` é uma implementação pública reutilizável que recebe nome e estratégia
por composição. Humano, bot e dealer diferem pela estratégia, não por subclasses de
jogador. As mãos e a pontuação pertencem ao estado da partida, consultado por
contextos; isso permite várias mãos no Blackjack.

### 4.2 Ações abertas para novos jogos

```java
public interface Jogada {}
public interface EtapaDeTurno {}
```

Ambas são interfaces não seladas. Cada cliente define seus tipos imutáveis:

```java
// Trinca
record Comprar(OrigemCompra origem) implements Jogada {}
record Descartar(UUID cartaId) implements Jogada {}
enum EtapaTrinca implements EtapaDeTurno { COMPRAR, DESCARTAR }

// Blackjack
record PedirCarta(UUID maoId) implements Jogada {}
record Parar(UUID maoId) implements Jogada {}
record Dobrar(UUID maoId) implements Jogada {}
record Dividir(UUID maoId) implements Jogada {}
```

O framework não usa `instanceof` contra ações concretas dos jogos. `MotorDeTrinca` e
`MotorDeBlackjack` interpretam suas ações e pedem validação pela Strategy configurada.

### 4.3 Contexto privado de decisão

```java
public interface ContextoDeDecisao {
    Jogador jogadorAtual();
    List<MaoDeCartas> maosDoJogadorAtual();  // imutável
    Map<Jogador, Integer> quantidadesDeCartasDosDemais();
    List<Carta> cartasPublicas();
    int quantidadeNoBaralho();
    EtapaDeTurno etapa();
}
```

Esse contexto nunca fornece mãos adversárias nem a ordem do baralho. Para dois
humanos no mesmo terminal, a interface de console limpa/separa a tela antes de mostrar
a mão do jogador atual.

## 5. Regras e seus contextos - Trilha D

As três estratégias são independentes e recebem apenas snapshots públicos. Nenhuma
delas recebe `Mesa`, `GerenciadorDeTurnos` ou coleção mutável.

```java
public interface RegraDeValidacaoStrategy {
    void validar(ContextoDeValidacao contexto);
}

public interface RegraDeVitoriaStrategy {
    AvaliacaoDeVitoria avaliar(ContextoDeVitoria contexto);
}

public interface RegraDePontuacaoStrategy {
    Map<Jogador, Integer> calcular(ContextoDePontuacao contexto);
}
```

### 5.1 Contexto de validação

```java
public interface ContextoDeValidacao {
    Jogador jogadorAtual();
    Jogada jogada();
    EtapaDeTurno etapa();
    List<MaoDeCartas> maosDoJogadorAtual();
    List<Carta> cartasPublicas();
    int quantidadeNoBaralho();
    EstadoPartida estado();
}
```

`validar` não altera o estado. Em falha, lança `JogadaInvalidaException` com mensagem
segura para apresentação no console.

### 5.2 Contexto e resultado de vitória

```java
public interface ContextoDeVitoria {
    List<Jogador> jogadores();
    Map<Jogador, List<MaoDeCartas>> maosPorJogador();
    Jogador jogadorAtual();
    List<Carta> cartasPublicas();
    EstadoPartida estado();
}

public record AvaliacaoDeVitoria(
        boolean encerrada,
        List<Jogador> vencedores,
        MotivoDeEncerramento motivo) {
    // construtor faz cópia defensiva e valida combinações incoerentes
}
```

A regra de vitória apenas inspeciona a situação. Reciclar descarte, comprar carta,
alterar mão ou avançar turno são responsabilidades do contexto/motor.

### 5.3 Contexto de pontuação

```java
public interface ContextoDePontuacao {
    List<Jogador> jogadores();
    Map<Jogador, List<MaoDeCartas>> maosPorJogador();
    AvaliacaoDeVitoria avaliacaoFinal();
}
```

A pontuação retorna um mapa novo. O motor copia o mapa antes de colocá-lo em
`ResultadoDePartida`.

## 6. Motor, configuração e resultado - Trilha A

### 6.1 Template Method

```java
public abstract class MotorDePartida {
    protected MotorDePartida(PartidaConfig configuracao);

    public final ResultadoDePartida executar();

    protected void preparar(ContextoDePartida contexto) {}
    protected abstract void executarTurno(ContextoDePartida contexto);
    protected void aoEncerrar(ContextoDePartida contexto,
                              ResultadoDePartida resultado) {}
}
```

O método final controla: validar configuração, criar/embaralhar baralho, distribuir,
executar turnos, avaliar vitória, pontuar, criar resultado, publicar eventos e
finalizar. A distribuição e as regras são Strategies; `executarTurno` é o passo
específico do jogo.

### 6.2 Contexto mutável controlado

`ContextoDePartida` é a porta de operações para o motor concreto. Ele não devolve
objetos internos nem listas mutáveis.

```java
public interface ContextoDePartida {
    EstadoPartida estado();
    List<Jogador> jogadores();
    Jogador jogadorAtual();
    List<MaoDeCartas> maosDe(Jogador jogador);
    MaoDeCartas criarMaoPara(Jogador jogador);
    Carta comprarDoBaralho();
    void entregarCarta(Jogador jogador, MaoDeCartas mao, Carta carta);
    Carta removerDaMao(Jogador jogador, MaoDeCartas mao, UUID cartaId);
    List<Carta> cartasPublicas();
    void publicarCarta(Carta carta);
    void recolherCartaPublica(UUID cartaId);
    void devolverAoBaralho(Collection<UUID> cartasIds, boolean embaralhar);
    void avancarTurno();
    void publicar(EventoDePartida evento);
}
```

Trinca usa a área de cartas públicas como descarte e devolve ao baralho todas menos o
topo quando precisa reciclar. A operação permanece genérica: o contexto recebe os IDs
escolhidos pelo motor concreto e não contém uma regra chamada "preservar topo".
Blackjack usa a mesma visão para as cartas expostas do dealer e pode manter
aposta/seguro como estado específico em `MotorDeBlackjack`, sem contaminar o
framework.

### 6.3 Builder

`PartidaConfig` é imutável e criado por Builder porque possui mais de quatro
dependências obrigatórias/opcionais:

```java
PartidaConfig config = PartidaConfig.builder()
    .jogadores(jogadores)
    .baralhoFactory(factory)
    .estrategiaDeDistribuicao(distribuicao)
    .regraDeValidacao(validacao)
    .regraDeVitoria(vitoria)
    .regraDePontuacao(pontuacao)
    .primeiroJogador(indice)
    .listeners(listeners)
    .build();
```

O Builder valida: dois ou mais jogadores, referências não nulas, jogadores distintos,
índice inicial válido e coleções copiadas defensivamente.

### 6.4 Tipos públicos já implementados no lugar errado

`EstadoPartida` e `ResultadoDePartida` são retornados ou consumidos pela API; portanto,
devem migrar de `core` para `api`. `MotorDePartida` também deve migrar para `api` antes
que outros imports se consolidem. `GerenciadorDeTurnos` e `SentidoDeRotacao`
permanecem em `core`.

`ResultadoDePartida.houveEmpate()` deve consultar o motivo explícito de encerramento,
e não inferir empate pela quantidade de vencedores. Vários vencedores podem ser
co-vencedores em jogos futuros, sem que isso represente empate.

## 7. Eventos e exceções - Trilha D

### 7.1 Observer extensível

```java
public interface EventoDePartida {}

public interface PartidaListener {
    void aoOcorrer(EventoDePartida evento);
}
```

`EventoDePartida` não é selada: jogos podem criar eventos próprios. O framework
fornece eventos comuns imutáveis para partida iniciada, turno iniciado/encerrado,
jogada rejeitada e partida finalizada. Eventos de compra não revelam a carta; eventos
de descarte podem revelá-la porque ela se tornou pública.

### 7.2 Exceções

```text
PartidaException extends RuntimeException
|- JogadaInvalidaException
|- EstadoDePartidaInvalidoException
`- BaralhoVazioException
```

- `JogadaInvalidaException`: recuperável; publica evento e repete a decisão.
- `EstadoDePartidaInvalidoException`: erro de ciclo de vida; propaga ao chamador.
- `BaralhoVazioException`: falha da operação de compra; o motor pode reciclar antes
  de tentar novamente ou encerrar conforme a regra do jogo.

## 8. Pontos de extensão e componentes reutilizáveis

| # | Hot-spot público | Como o cliente estende |
|---|---|---|
| 1 | `Carta` | cria novos tipos de carta |
| 2 | `BaralhoFactory` | define a composição do baralho |
| 3 | `EstrategiaDeDistribuicao` | define quantidade e ordem de entrega |
| 4 | `EstrategiaDeDecisao` | implementa humano, bot ou dealer |
| 5 | `Jogada` / `EtapaDeTurno` | cria ações e fases próprias |
| 6 | `RegraDeValidacaoStrategy` | cria validações do jogo |
| 7 | `RegraDeVitoriaStrategy` | cria condições de vitória |
| 8 | `RegraDePontuacaoStrategy` | cria cálculo de pontos |
| 9 | `EventoDePartida` / `PartidaListener` | cria eventos e observadores |
| 10 | operações protegidas de `MotorDePartida` | especializa o turno do jogo |

Componentes reutilizáveis: `BaralhoPadrao`, `MaoDeCartasPadrao`, `JogadorPadrao`,
`MotorDePartida.executar()`, `PartidaConfig`, infraestrutura de contexto,
`GerenciadorDeTurnos`, notificação e exceções.

## 9. Testes de contrato que liberam as trilhas

Antes de declarar a API congelada, cada dono implementa ao menos estes testes:

1. `BaralhoPadrao` e `MaoDeCartasPadrao` não expõem coleções mutáveis.
2. `BaralhoDeTrincaFactory` cria 104 cartas e identidades distintas.
3. `ContextoDeDistribuicao` distribui 9 cartas sem expor mão interna.
4. `ContextoDeDecisao` não revela mãos adversárias.
5. Uma ação nova `Parar implements Jogada` compila sem editar o framework.
6. Regras da Trinca compilam usando apenas os três contextos públicos.
7. `MotorDeTrinca` compila importando apenas `api`.
8. Um listener recebe eventos sem o motor conhecer o console.
9. Jogada inválida não altera o estado e permite nova tentativa.
10. Nenhum arquivo do framework importa `trinca` ou `blackjack`.

## 10. Critério de congelamento

- [ ] A confirma `MotorDePartida`, contextos, Builder, estados e resultado.
- [ ] B confirma `Carta`, `BaralhoPadrao`, `MaoDeCartasPadrao` e distribuição.
- [ ] C confirma jogador por composição, ações abertas e contexto privado.
- [ ] D confirma assinaturas das três regras, eventos e exceções.
- [ ] E implementa pequenos stubs de Trinca e `Parar` do Blackjack usando somente a
      API, comprovando que nenhum tipo interno está faltando.
- [ ] Todos aprovam que mudanças posteriores na API exigem revisão cruzada.

Após os seis itens, as assinaturas são congeladas e A-D podem implementar em
paralelo. O UML deve ser gerado somente depois desse ponto.
