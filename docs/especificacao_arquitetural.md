# Especificação arquitetural — CardGame Framework

**Status:** baseline Java integrada e testada em `trilha/a-motor`; código ainda não
publicado na `main`

**Plataforma:** Java 26, Maven e JUnit 5

**Clientes de validação:** Trinca e Blackjack básico

## 1. Fontes de verdade

Em caso de divergência, aplicar esta ordem:

1. enunciado em `docs/proposta/AtividadeProposta.pdf`;
2. decisões normativas em `ARQUITETURA_FRAMEWORK_MAP.md`;
3. esta especificação de implementação;
4. modelo conceitual e catálogo de padrões;
5. regras próprias de cada aplicação cliente.

Código e testes são a evidência executável. Se divergirem de uma decisão normativa, a
divergência deve ser registrada e corrigida; não se altera silenciosamente o documento
ou o código para esconder o problema.

Nesta revisão, “implementado” significa presente na branch local `trilha/a-motor`, que
incorporou a `main`. A publicação antecipada destes documentos serve à revisão da
equipe e não afirma que a `main` remota já contenha o novo `engine`.

## 2. Arquitetura física

```text
clientes ─────> cardgame.api
clientes ─────> cardgame.engine.MotorDePartida
engine   ─────> cardgame.api
api      ─────> Java
```

`cardgame.engine` contém o runtime. Somente `MotorDePartida` é público; os demais
tipos não possuem modificador `public`. Não há pacote de produção `core`.

## 3. Superfície pública

### 3.1 Domínio reutilizável

- `Carta`, `Baralho`, `BaralhoPadrao`, `BaralhoFactory`;
- `MaoDeCartas`;
- `Jogador`, `JogadorPadrao`.

### 3.2 Decisão e I/O

- `Jogada`, `EtapaDeTurno`;
- `ContextoDeDecisao`, `ContextoDeDecisaoPadrao`;
- `EstrategiaDeDecisao`;
- `EntradaSaida` e `api.io.ControleEntradaSaida`;
- `DecisaoAleatoria`, `DecisaoGulosa` e `DecisaoHumanaConsole`.

### 3.3 Partida

- `PartidaConfig` e `PartidaConfig.Builder`;
- `EstadoPartida`, `VisaoDaPartida`, `ContextoDePartida`;
- `ContextoDeDistribuicao`, `ContextoDeValidacao`;
- `ResultadoDoTurno`, `DesfechoDePartida`, `ResultadoDePartida`;
- `MotivoDeEncerramento`, `MotivoPadrao`;
- `engine.MotorDePartida`.

### 3.4 Extensões, eventos e erros

- `EstrategiaDeDistribuicao`;
- `RegraDeValidacaoStrategy`, `RegraDeVitoriaStrategy`,
  `RegraDePontuacaoStrategy`;
- `EventoDePartida`, `PartidaListener` e eventos padrão;
- `PartidaException` e subclasses.

## 4. Configuração

`PartidaConfig` é imutável e criada por Builder.

| Propriedade | Obrigatória | Regra |
|---|---:|---|
| jogadores | sim | mínimo 2; ids não nulos e distintos |
| fábrica de baralho | sim | não pode devolver `null` |
| distribuição | sim | recebe contexto limitado à preparação |
| regra de vitória | sim | devolve `Optional<DesfechoDePartida>`, nunca `null` |
| regra de pontuação | não | padrão registra zero para todos |
| regra de validação | não | padrão aceita qualquer jogada |
| primeiro jogador | não | índice zero por padrão; deve existir na lista |

Listas são copiadas defensivamente.

## 5. Contrato do motor

`MotorDePartida` é abstrato e seu construtor protegido recebe a configuração. O método
`executar()` é público, final e de uso único.

### 5.1 Operação primitiva

```java
protected abstract ResultadoDoTurno executarTurno(ContextoDePartida contexto);
```

O motor concreto interpreta a mecânica do jogo e devolve uma diretiva. Ele não avança
turnos diretamente.

### 5.2 Hooks opcionais

```java
protected void preparar(ContextoDePartida contexto);
protected void aposDistribuir(ContextoDePartida contexto);
protected void aoEncerrar(VisaoDaPartida visao, ResultadoDePartida resultado);
```

`preparar` ocorre antes da distribuição; `aposDistribuir`, depois dela e antes da
primeira avaliação; `aoEncerrar`, depois do estado final e do evento correspondente.

### 5.3 Ordem garantida

```text
criar/embaralhar baralho
→ preparar
→ distribuir
→ aposDistribuir
→ iniciar
→ avaliar
→ [turno → avaliar → aplicar diretiva]*
→ pontuar
→ finalizar
→ aoEncerrar
```

## 6. Contextos

`VisaoDaPartida` fornece estado, jogadores, jogador atual, mãos e quantidade restante
no baralho.

`ContextoDePartida` acrescenta operações controladas:

- comprar do baralho;
- adicionar/remover carta de uma mão;
- devolver cartas ao baralho e embaralhá-lo;
- validar uma `Jogada` pela Strategy configurada;
- publicar um evento específico do cliente.

Não há `avancarTurno()` nem `finalizar()` públicos.

`ContextoDeDecisao` contém uma etapa e ações permitidas. Implementações específicas
podem acrescentar informação pública, mas não expor internals.

## 7. Turnos

`ResultadoDoTurno` representa exatamente uma diretiva coerente:

- `avancar()`;
- `repetir()`;
- `inverter()`;
- `pular(int quantidade)`.

`GerenciadorDeTurnos` é interno e suporta N jogadores, rotação horária/anti-horária e
pulos acumulados. O resultado não pode simultaneamente repetir e pular jogadores.

## 8. Vitória, motivo e placar

`RegraDeVitoriaStrategy` devolve vazio enquanto a partida continua ou um
`DesfechoDePartida` quando encerra.

`MotivoDeEncerramento` é extensível. Um motivo de vitória exige ao menos um vencedor;
um empate pode registrar co-vencedores ou lista vazia. Identidades não podem se repetir,
e todos os nomes registrados como vencedores devem participar da partida.

`RegraDePontuacaoStrategy` recebe a visão e o desfecho. O placar final deve conter
exatamente uma entrada não nula por identidade participante.

## 9. Eventos

O motor publica eventos padrão de início da partida, início/fim do turno, jogada
rejeitada e encerramento. Clientes podem publicar eventos adicionais.

Listeners são agregados por identidade, podem ser removidos e são notificados a partir
de uma cópia da coleção. Falha não verificada de um listener é registrada e não derruba
a partida.

## 10. Exceções

`PartidaException` estende `RuntimeException`.

| Exceção | Situação |
|---|---|
| `BaralhoVazioException` | compra sem carta disponível |
| `EstadoDePartidaInvalidoException` | operação incompatível com o ciclo de vida |
| `JogadaInvalidaException` | recusa recuperável de uma jogada |

Uma jogada inválida repete o mesmo turno e gera `JogadaRejeitada`. Após 100 recusas
consecutivas, o engine lança `IllegalStateException` para impedir laço infinito.

## 11. Encapsulamento

- configurações, resultados, desfechos, eventos e contextos copiam coleções recebidas;
- `MaoDeCartas.cartas()` devolve visão imutável;
- nenhum colaborador mutável de `engine` é público;
- uma carta não pode estar simultaneamente no baralho e em uma mão;
- participantes são comparados por `UUID` nas invariantes de partida.

## 12. Padrões implementados

| Padrão | Evidência |
|---|---|
| Template Method | `MotorDePartida.executar()` final e hooks protegidos |
| Strategy | distribuição, decisão, validação, vitória e pontuação |
| Factory Method | `BaralhoFactory` e fábricas fornecidas pelos clientes |
| Observer | `PartidaListener` e eventos |

Builder auxilia `PartidaConfig`, mas não entra na contagem mínima. Decorator foi
avaliado e adiado por falta de necessidade comprovada.

## 13. Rastreabilidade

| Requisito | Evidência | Estado |
|---|---|---|
| API pública | pacotes e Javadoc | implementado |
| ≥ 5 extensões | dez hot-spots | implementado |
| separação | teste de dependências | implementado |
| cliente | dois stubs; Trinca completa pendente | parcial |
| interfaces/abstrata | API + motor | implementado |
| exceções | hierarquia pública | implementado |
| encapsulamento | testes de cópia e mutação | implementado |
| testes | 103 testes | implementado |
| Javadoc | `mvn javadoc:javadoc` | implementado |
| UML | PlantUML versionado | implementado, sujeito à evolução da API |
| exemplos | stubs executáveis | implementado como prova arquitetural |
| justificativas | documentos de arquitetura/padrões | implementado |

## 14. Limitações e próximas entregas

- implementar Trinca completa como primeira aplicação;
- implementar Blackjack básico completo como segunda prova;
- revisar a API depois dessas implementações antes de declará-la estável;
- gerar a imagem final do PlantUML e recortes para o relatório de até oito páginas;
- manter Java 26 uniforme nas máquinas da equipe e no CI.
