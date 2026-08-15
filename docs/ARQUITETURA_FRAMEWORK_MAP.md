# Arquitetura normativa — CardGame Framework

**Status:** baseline implementada e validada na branch local `trilha/a-motor` em
15/08/2026; publicação do código na `main` pendente de revisão

**Escopo:** framework reutilizável para jogos de cartas com turnos sequenciais

**Clientes de validação:** Trinca e Blackjack básico

Este documento é o mapa normativo da arquitetura atual. Ele substitui a versão que
descrevia apenas uma fase de planejamento e dizia “não implementar código ainda”. O
código já existe; portanto, as decisões abaixo descrevem o contrato efetivamente
implementado e coberto por testes.

> **Nota de publicação:** este documento pode chegar à `main` antes do código da
> Trilha A. As evidências de implementação e os 103 testes referem-se à branch local
> `trilha/a-motor`, após ela incorporar a `main` no merge `4968e9e`. Até a revisão e a
> integração dessa feature, a documentação representa a baseline proposta e validada,
> não o estado compilável da `main` remota.

## 1. Objetivo e limite da promessa

O projeto oferece infraestrutura para construir jogos de cartas sem duplicar baralho,
mãos, participantes, ciclo de vida, turnos, regras, eventos e tratamento de falhas.
Ele aplica Inversão de Controle: o framework conduz a partida e chama os pontos de
extensão fornecidos por cada jogo.

O framework não “gera automaticamente qualquer jogo de cartas”. A afirmação correta é:

> A baseline permite implementar diferentes jogos de cartas baseados em turnos,
> mantendo as regras e o estado específico fora do framework.

Trinca e Blackjack são provas de reutilização, não modelos dos quais a API foi
copiada. Funcionalidades como apostas, tempo real, rede, persistência, múltiplas mesas
ou várias mãos simultâneas podem exigir extensões futuras.

## 2. Requisitos arquiteturais atendidos

| Requisito do enunciado | Evidência atual |
|---|---|
| API pública definida | pacote `cardgame.api` e `engine.MotorDePartida` |
| Pelo menos cinco pontos de extensão | dez hot-spots catalogados na seção 7 |
| Framework separado dos clientes | dependências verificadas por `ClientesStubTest` |
| Aplicação cliente | dois clientes-stub executáveis; jogos completos ainda são entregas posteriores |
| Interfaces e classe abstrata | contratos públicos + `MotorDePartida` abstrato |
| Exceções | hierarquia `PartidaException` |
| Coleções encapsuladas | cópias defensivas e visões somente leitura |
| Testes automatizados | 103 testes na baseline integrada |
| Javadoc | API pública documentada e validada pelo Maven |
| UML | `docs/diagrama-classes.puml` |
| Exemplos | stubs de Trinca e Blackjack em `src/test/java` |
| Decisões justificadas | este mapa e `padroes-de-projeto.md` |

## 3. Estrutura de pacotes e direção das dependências

```text
br.edu.uepb.map.cardgame.api
  contratos, valores imutáveis e implementações públicas reutilizáveis

br.edu.uepb.map.cardgame.api.evento
  eventos padrão da partida

br.edu.uepb.map.cardgame.api.excecao
  exceções de domínio

br.edu.uepb.map.cardgame.api.estrategia
  decisões humana, aleatória e gulosa

br.edu.uepb.map.cardgame.api.io
  adaptador de console para a porta EntradaSaida

br.edu.uepb.map.cardgame.engine
  MotorDePartida público e colaboradores internos sem public

br.edu.uepb.map.trinca / br.edu.uepb.map.blackjack
  aplicações clientes; atualmente stubs arquiteturais em src/test/java
```

Direção permitida:

```text
trinca / blackjack ──> api
trinca / blackjack ──> engine.MotorDePartida
engine              ──> api
api                 ──> Java
```

Direções proibidas:

- `api → engine`;
- framework → pacote de jogo concreto;
- cliente → colaborador interno de `engine`.

Não existe pacote de produção `core`. Colocar um “core” fisicamente dentro de `api`
seria uma fronteira enganosa; manter turnos em outro pacote público exporia detalhes
internos. Por isso, `MotorDePartida` e seus colaboradores vivem juntos em `engine`,
mas somente o motor é público.

## 4. Abstrações e responsabilidades

| Abstração | Responsabilidade | Não conhece |
|---|---|---|
| `Carta` | identidade estável de uma carta | naipe, valor ou regra obrigatórios |
| `Baralho` | armazenar, comprar, adicionar e embaralhar cartas | jogadores, vitória ou descarte |
| `MaoDeCartas` | visão somente leitura da mão principal | mutação direta ou combinação vencedora |
| `Jogador` | identidade e porta opcional de decisão | mão mutável, pontuação ou regra concreta |
| `PartidaConfig` | configuração imutável das colaborações | estado transitório da execução |
| `VisaoDaPartida` | snapshot público para regras | operações mutáveis |
| `ContextoDePartida` | operações controladas para o motor concreto | avanço de turno e finalização direta |
| `MotorDePartida` | ciclo de vida, repetição, turnos, eventos e resultado | regras de Trinca ou Blackjack |
| `ResultadoDoTurno` | diretiva imutável de repetir, avançar, inverter ou pular | mutação do gerenciador de turnos |
| `DesfechoDePartida` | vencedores e motivo reconhecidos pela regra | cálculo do placar |
| `ResultadoDePartida` | resultado final imutável | execução da partida |

Estado específico de mesa continua no cliente. Pilha de descarte, cartas abertas do
dealer, apostas, combinações e valor flexível do Ás não pertencem ao framework.

## 5. Fronteira pública e implementação interna

### 5.1 API pública

Um autor de jogo pode depender de:

- domínio: `Carta`, `Baralho`, `BaralhoPadrao`, `MaoDeCartas`, `Jogador` e
  `JogadorPadrao`;
- decisão: `Jogada`, `EtapaDeTurno`, `ContextoDeDecisao`,
  `ContextoDeDecisaoPadrao`, `EstrategiaDeDecisao` e `EntradaSaida`;
- partida: `PartidaConfig`, `EstadoPartida`, `VisaoDaPartida`,
  `ContextoDePartida`, `ContextoDeDistribuicao`, `ResultadoDoTurno`,
  `DesfechoDePartida` e `ResultadoDePartida`;
- extensões: fábrica, distribuição, validação, vitória, pontuação e listeners;
- eventos e exceções dos subpacotes públicos;
- `engine.MotorDePartida`, único ponto público do runtime.

### 5.2 Engine interno

Permanecem package-private:

- `GerenciadorDeTurnos`;
- `SentidoDeRotacao`;
- `CicloDeVidaDaPartida`;
- `PartidaEmExecucao` e sua mão interna;
- `ContextoDeDistribuicaoInterno`.

Esses tipos podem mudar sem recompilar um jogo cliente. A ausência de `public` é a
garantia mecânica da fronteira, não apenas uma convenção documental.

## 6. Fluxo do Template Method

`MotorDePartida.executar()` é `final` e só pode ser chamado uma vez:

```text
CONFIGURADA
   ↓
PREPARANDO
   ├─ criar e embaralhar o baralho
   ├─ preparar(contexto)                    hook opcional
   ├─ distribuir(contextoDeDistribuicao)    Strategy
   └─ aposDistribuir(contexto)              hook opcional
   ↓
EM_ANDAMENTO
   ├─ publicar PartidaIniciada
   ├─ avaliar encerramento inicial
   └─ para cada turno:
        publicar TurnoIniciado
        executarTurno(contexto)             operação primitiva
          └─ até 100 tentativas se a jogada for rejeitada
        publicar TurnoEncerrado
        avaliar vitória
        aplicar ResultadoDoTurno internamente
   ↓
FINALIZADA
   ├─ calcular placar
   ├─ publicar PartidaFinalizada
   └─ aoEncerrar(visao, resultado)          hook opcional
```

O jogo não avança a vez e não finaliza a partida diretamente. Ele devolve uma
`ResultadoDoTurno`; somente `GerenciadorDeTurnos` interpreta a diretiva. Isso protege
o frozen-spot do Template Method.

## 7. Pontos de extensão implementados

| # | Hot-spot | Contrato | Variação | Padrão principal |
|---|---|---|---|---|
| 1 | tipo de carta | `Carta` | atributos próprios de cada jogo | Factory Method |
| 2 | criação do baralho | `BaralhoFactory` | composição e implementação do baralho | Factory Method |
| 3 | distribuição | `EstrategiaDeDistribuicao` | quantidade, ordem e destinatários | Strategy |
| 4 | decisão | `EstrategiaDeDecisao` | humano, bot, dealer | Strategy |
| 5 | ações e fases | `Jogada`, `EtapaDeTurno` | vocabulário de cada jogo | interfaces abertas |
| 6 | validação | `RegraDeValidacaoStrategy` | pré-condições da jogada | Strategy |
| 7 | vitória | `RegraDeVitoriaStrategy` | encerramento, vencedores e motivo | Strategy |
| 8 | pontuação | `RegraDePontuacaoStrategy` | cálculo de placar | Strategy |
| 9 | eventos | `EventoDePartida`, `PartidaListener` | fatos e observadores adicionais | Observer |
| 10 | turno concreto | `executarTurno` | mecânica específica | Template Method |

Os quatro GoF efetivamente usados para o mínimo da disciplina são Template Method,
Strategy, Factory Method e Observer. Decorator foi analisado, mas não foi forçado na
baseline porque ainda não existe uma combinação de validações que justifique sua
estrutura.

## 8. Decisão do jogador sem viés de jogo

`ContextoDeDecisao` expõe apenas:

- uma `EtapaDeTurno` definida pelo cliente;
- uma lista imutável de `Jogada` permitidas.

Um jogo pode implementar uma subinterface com informações públicas adicionais. O
contexto não revela mãos adversárias, ordem do baralho ou estruturas mutáveis.

`JogadorPadrao` aceita construção com ou sem `EstrategiaDeDecisao`. A forma sem
estratégia atende jogos cuja entrada chega por outra fronteira; solicitar uma decisão
nesse estado falha explicitamente. A forma com estratégia permite trocar humano, bot
ou dealer por composição, sem subclasses como `JogadorHumano` e `JogadorBot`.

## 9. Configuração e invariantes

`PartidaConfig.Builder` exige:

- pelo menos dois jogadores com identificadores distintos;
- `BaralhoFactory`;
- `EstrategiaDeDistribuicao`;
- `RegraDeVitoriaStrategy`.

São opcionais:

- `RegraDePontuacaoStrategy`, com implementação neutra por padrão;
- `RegraDeValidacaoStrategy`, que aceita qualquer jogada por padrão;
- índice do primeiro jogador, zero por padrão.

Coleções recebidas são copiadas. O engine também verifica que:

- uma carta não ocupa simultaneamente baralho e mão;
- somente participantes configurados acessam mãos;
- vencedores pertencem à partida;
- o placar contém exatamente uma entrada por identidade participante;
- transições seguem a tabela de `EstadoPartida`;
- resultados e desfechos não representam combinações incoerentes.

## 10. Eventos e exceções

Eventos padrão:

- `PartidaIniciada`;
- `TurnoIniciado`;
- `JogadaRejeitada`;
- `TurnoEncerrado`;
- `PartidaFinalizada`.

Clientes podem criar outros `EventoDePartida` e publicá-los pelo contexto. Cada
listener é isolado: uma `RuntimeException` de um observador é registrada, e os demais
continuam recebendo eventos.

Exceções de domínio são não verificadas e descendem de `PartidaException`:

- `BaralhoVazioException`;
- `EstadoDePartidaInvalidoException`;
- `JogadaInvalidaException`.

Uma `JogadaInvalidaException` é recuperável durante o turno: o motor notifica a
rejeição e repete a operação primitiva. Cem recusas consecutivas indicam defeito no
cliente e resultam em `IllegalStateException`.

## 11. SOLID e GRASP

- **SRP / Alta Coesão:** turnos, ciclo de vida, baralho, regras, decisão e eventos
  possuem responsáveis distintos.
- **OCP:** novos jogos adicionam implementações dos hot-spots sem editar o engine.
- **LSP:** subclasses de `MotorDePartida` preservam o fluxo porque `executar()` é final.
- **ISP:** validação, vitória e pontuação são interfaces separadas.
- **DIP / Baixo Acoplamento:** engine depende de contratos em `api`, nunca de jogos.
- **Especialista na Informação:** `EstadoPartida` conhece transições;
  `GerenciadorDeTurnos` conhece rotação; `BaralhoPadrao` conhece suas cartas.

As justificativas e alternativas rejeitadas estão detalhadas em
`docs/padroes-de-projeto.md`.

## 12. Validação por clientes

Os stubs atuais demonstram, sem importar internals:

- Trinca com 104 cartas, nove cartas por jogador, descarte mantido pelo cliente,
  validação, vitória e pontuação próprias;
- Blackjack com 52 cartas, duas cartas iniciais, decisões por Strategy e repetição de
  turno ao pedir carta.

Eles são testes arquiteturais, não implementações completas das regras descritas nos
documentos de cada jogo.

## 13. Limites conhecidos da baseline

- uma mão principal por participante;
- turnos sequenciais em uma única thread;
- um baralho compartilhado por execução;
- sem persistência, rede ou interface gráfica;
- sem múltiplas rodadas ou torneio;
- Trinca e Blackjack completos ainda precisam ser implementados como aplicações.

Esses limites são escopo honesto. Uma necessidade só deve entrar no framework quando
for reutilizável por jogos independentes e puder ser expressa sem termos de um cliente
específico.

## 14. Verificação

```bash
./mvnw clean verify
./mvnw javadoc:javadoc
```

A baseline integrada possui 103 testes. `ClientesStubTest` verifica automaticamente
que o framework não importa Trinca/Blackjack, que `api` não importa `engine` e que os
clientes só alcançam o tipo público do runtime.

## 15. Regra para evoluções

Antes de ampliar a API pública, responder:

1. a capacidade aparece em mais de um jogo?
2. o cliente precisa conhecê-la para usar ou estender o framework?
3. ela preserva a direção `cliente → engine → api`?
4. pode ser expressa sem vocabulário de Trinca ou Blackjack?
5. existe teste de cliente demonstrando a lacuna e depois a solução?

Mudanças de assinatura pública exigem comunicação entre as trilhas, atualização do
UML, Javadoc e teste de integração no mesmo commit.
