# Relatório da Trilha A — motor, ciclo de vida e turnos

**Responsável:** Lucas N. de Araújo

**Data da consolidação:** 15/08/2026

**Status:** `main` incorporada à branch local, baseline validada e publicação da
feature pendente

## 1. Resultado entregue

A Trilha A implementou o frozen-spot da partida e consolidou a fronteira física na
branch local `trilha/a-motor`:

```text
clientes ──> api
clientes ──> engine.MotorDePartida
engine   ──> api
api      ──> Java
```

`MotorDePartida` é o único tipo público de `engine`. Os colaboradores de ciclo de
vida, turnos, estado mutável, mãos e distribuição interna não possuem `public`.

O merge `4968e9e` trouxe a `main` para dentro da feature; ele não levou a feature para
a `main`. Portanto, este relatório pode ser publicado para revisão antes do código,
mas não deve ser interpretado como prova de que o repositório remoto já contém a
baseline.

## 2. Tipos principais

### API pública da execução

- `EstadoPartida`;
- `MotivoDeEncerramento` e `MotivoPadrao`;
- `DesfechoDePartida` e `ResultadoDePartida`;
- `ResultadoDoTurno`;
- `VisaoDaPartida` e `ContextoDePartida`;
- `PartidaConfig`.

### Engine

- público: `MotorDePartida`;
- internos: `GerenciadorDeTurnos`, `SentidoDeRotacao`,
  `CicloDeVidaDaPartida`, `PartidaEmExecucao` e
  `ContextoDeDistribuicaoInterno`.

## 3. Decisão `engine` em vez de `core`

O motor é ponto de extensão e precisa ser público. O gerenciador de turnos é detalhe
interno usado pelo motor. Mantê-los juntos em `engine` permite que o acesso de pacote
funcione sem publicar o gerenciador.

Colocar o motor em `api` e turnos em `core` criaria dependência `api → core`. Colocar
turnos em `api` os apresentaria falsamente como contrato de cliente. O arranjo adotado
mantém as dependências acíclicas e a intenção dos pacotes explícita.

## 4. Template Method

`executar()` é final e garante:

```text
criar/embaralhar baralho
→ preparar
→ distribuir
→ aposDistribuir
→ iniciar
→ repetir turnos e avaliar vitória
→ pontuar/finalizar
→ aoEncerrar
```

Há uma única operação primitiva obrigatória:

```java
protected abstract ResultadoDoTurno executarTurno(ContextoDePartida contexto);
```

O cliente não recebe acesso ao gerenciador. Ele devolve `ResultadoDoTurno`, e o
engine aplica avanço, repetição, inversão e pulos.

## 5. Invariantes protegidas

- uma partida executa uma única vez;
- estados seguem a tabela declarada em `EstadoPartida`;
- são exigidos ao menos dois jogadores com UUIDs distintos;
- uma carta não pode ocupar baralho e mão ao mesmo tempo;
- somente participantes acessam mãos;
- vencedores pertencem à partida;
- o placar contém exatamente uma entrada por participante;
- resultados, motivos e vencedores não formam estados incoerentes;
- coleções públicas são cópias ou visões imutáveis.

## 6. Correções realizadas

### Listener defeituoso

Cada `PartidaListener` é protegido contra `RuntimeException`. A falha é registrada e
os demais observadores continuam; erros mais graves não são engolidos.

### Repetição infinita

Jogadas inválidas são recuperáveis, mas um cliente defeituoso não pode repetir para
sempre. O limite é 100 recusas consecutivas no mesmo turno; depois disso, o motor lança
`IllegalStateException` com a última causa.

### Validação conectada

`RegraDeValidacaoStrategy` passou a integrar `PartidaConfig` e é acionada por
`ContextoDePartida.validarJogada`. O engine constrói `ContextoDeValidacao`, mantendo o
snapshot coerente.

### Hook depois da distribuição

`aposDistribuir` atende montagem de mesa que exige mãos prontas, como virar o descarte
da Trinca ou revelar carta do dealer.

### Especialista em transições

`EstadoPartida` voltou a conhecer destinos legais. `CicloDeVidaDaPartida` consulta o
enum em vez de duplicar um `switch`.

## 7. Integração com a Trilha C

O merge com a `main` possuía conflitos em `ContextoDeDecisao`,
`EstrategiaDeDecisao`, `Jogada`, `Jogador`, `JogadorPadrao` e em um teste legado.

A resolução adotada:

- mantém `Jogada` e `EtapaDeTurno` abertas;
- oferece `ContextoDeDecisao` somente leitura com etapa e ações permitidas;
- preserva as decisões aleatória, gulosa e humana;
- permite `JogadorPadrao` com ou sem Strategy;
- mantém identidade e decisão por composição;
- move `ContextoDeDecisaoPadrao` para a API pública;
- não reintroduz pacote de produção `core`;
- remove o falso legado que dependia do pacote excluído.

O stub de Blackjack passou a obter a Strategy do jogador e continua conhecendo apenas
API pública e `engine.MotorDePartida`.

## 8. Testes

A baseline integrada executa **103 testes, zero falhas e zero erros**.

Coberturas relevantes da Trilha A:

| Área | Evidência |
|---|---|
| estados | transições, terminalidade e imutabilidade |
| configuração | participantes, ids e coleções |
| turnos | N jogadores, sentido, pulos e repetição |
| agregado | movimentação de carta e proteção pós-encerramento |
| motor | hooks, eventos, rejeições, limite e placar |
| arquitetura | Trinca/Blackjack sem imports internos |
| integração C | decisões, jogador e I/O de console |

Comandos:

```bash
./mvnw clean verify
./mvnw javadoc:javadoc
```

## 9. Decisões abertas

Não bloqueiam a baseline, mas precisam ser validadas pelos clientes completos:

- se uma mão principal por jogador é suficiente;
- se validações reais justificam Decorator;
- se novos eventos padrão são comuns a ambos os jogos;
- se o contexto de decisão precisa de outro dado comum;
- se a API pode ser congelada após Trinca e Blackjack completos.

## 10. Conclusão

A Trilha A está adequada ao escopo acadêmico: demonstra Template Method, Inversão de
Controle, baixo acoplamento, alta coesão e Especialista na Informação. A arquitetura
não promete cobrir literalmente todo jogo possível; ela fornece hot-spots comprovados
por dois clientes diferentes e uma regra explícita para evoluir sem enviesar o núcleo
do framework — que, fisicamente, é o runtime interno de `engine`.
