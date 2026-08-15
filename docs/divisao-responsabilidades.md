# Divisão de responsabilidades — Projeto Final MAP

**Equipe:** Lucas · Allan · Raffael · Lívia · Júlio

**Cliente principal:** Trinca

**Segundo cliente:** Blackjack básico

**Status:** baseline das Trilhas A–D integrada na branch local `trilha/a-motor`;
integração do código à `main`, aplicações completas e relatório final pendentes

## 1. Princípio de colaboração

O corte é por responsabilidade e fronteira de contrato, não por quantidade de
classes. Cada dono mantém código, testes, Javadoc e seção correspondente do relatório.

Regras comuns:

1. quem altera um tipo atualiza seus testes e Javadoc;
2. mudança em API pública exige aviso aos demais donos;
3. cliente não importa internals de `engine`;
4. código de framework não importa Trinca ou Blackjack;
5. `main` deve permanecer compilando;
6. documentação deve descrever o código efetivo, não uma arquitetura planejada que já
   foi substituída.

## 2. Trilha A — motor, ciclo de vida e turnos

**Responsável:** Lucas

### Tipos sob responsabilidade principal

- `engine.MotorDePartida`;
- internals de `engine`: `GerenciadorDeTurnos`, `SentidoDeRotacao`,
  `CicloDeVidaDaPartida`, `PartidaEmExecucao` e contexto interno de distribuição;
- `api.PartidaConfig`, `EstadoPartida`, `ContextoDePartida`, `VisaoDaPartida`;
- `ResultadoDoTurno`, `DesfechoDePartida`, `ResultadoDePartida` e motivos de
  encerramento.

### Responsabilidades

- manter o Template Method e a Inversão de Controle;
- controlar transições de estado e impedir segunda execução;
- aplicar repetição, inversão e pulos sem expor o gerenciador;
- validar vencedores e placar;
- fornecer contextos controlados aos jogos;
- isolar listeners defeituosos e impedir repetição infinita de jogadas inválidas;
- garantir a direção `engine → api`.

### Entrega para a defesa

Explicar por que `MotorDePartida` é público em `engine`, enquanto o gerenciador de
turnos permanece no mesmo pacote sem `public`, e demonstrar que o framework chama o
jogo por meio dos hooks.

## 3. Trilha B — cartas, baralho, mão e distribuição

**Responsável:** Júlio

### Tipos sob responsabilidade principal

- `Carta`;
- `Baralho`, `BaralhoPadrao`, `BaralhoFactory`;
- `MaoDeCartas`;
- `EstrategiaDeDistribuicao` e `ContextoDeDistribuicao`.

### Responsabilidades

- manter `Carta` aberta a modelos diferentes;
- encapsular a coleção e as invariantes de `BaralhoPadrao`;
- permitir composições de baralho criadas pelos clientes;
- garantir compra, adição e embaralhamento sem duplicar identidade de carta;
- oferecer mão somente leitura;
- permitir formas diferentes de distribuição.

### Entrega para a defesa

Demonstrar que uma coleção devolvida pelo framework não pode ser modificada por fora e
comparar as fábricas de 104 cartas da Trinca e 52 do Blackjack.

## 4. Trilha C — jogadores, decisão e I/O

**Responsável:** Allan

### Tipos sob responsabilidade principal

- `Jogador`, `JogadorPadrao`;
- `Jogada`, `EtapaDeTurno`;
- `ContextoDeDecisao`, `ContextoDeDecisaoPadrao`;
- `EstrategiaDeDecisao`;
- `DecisaoAleatoria`, `DecisaoGulosa`, `DecisaoHumanaConsole`;
- `EntradaSaida`, `ControleEntradaSaida`.

### Responsabilidades

- separar identidade de comportamento por composição;
- manter ações e etapas abertas para os clientes;
- não revelar informação privada em contextos de decisão;
- permitir troca de Strategy sem trocar identidade;
- isolar console por uma porta testável;
- trafegar `Jogada`, nunca `String`, pelo domínio.

`JogadorPadrao` aceita estratégia opcional: partidas dirigidas externamente podem usar
somente a identidade; jogos que pedem decisão ao jogador devem configurá-la.

### Entrega para a defesa

Trocar uma decisão humana por bot sem recompilar o engine e mostrar o teste da porta
de console com `Reader`/`Writer` injetados.

## 5. Trilha D — regras, exceções e eventos

**Responsável:** Lívia

### Tipos sob responsabilidade principal

- `RegraDeValidacaoStrategy` e `ContextoDeValidacao`;
- `RegraDeVitoriaStrategy`;
- `RegraDePontuacaoStrategy`;
- `EventoDePartida`, eventos padrão e `PartidaListener`;
- pacote `api.excecao`.

### Responsabilidades

- manter validação, vitória e pontuação independentes;
- definir quais falhas são recuperáveis;
- garantir eventos imutáveis e extensíveis;
- evitar acoplamento do motor ao console;
- avaliar Decorator somente se validações combináveis demonstrarem necessidade real.

### Entrega para a defesa

Adicionar uma regra e um listener novos sem editar o engine, relacionando a solução a
OCP, DIP e Observer.

## 6. Trilha E — aplicações, UML e relatório

**Responsável:** Raffael

### Artefatos sob responsabilidade principal

- pacote da aplicação Trinca;
- pacote da aplicação Blackjack;
- `docs/diagrama-classes.puml`;
- consolidação do relatório e exemplos da apresentação.

### Responsabilidades

- implementar as regras aprovadas usando somente a superfície pública;
- transformar os stubs atuais em aplicações completas;
- registrar lacunas genéricas em vez de contorná-las com acesso a internals;
- manter UML com relações e multiplicidades compatíveis com o código;
- consolidar o relatório de até oito páginas;
- preparar uma demonstração reproduzível dos dois jogos.

### Entrega para a defesa

Executar os dois clientes sobre a mesma baseline e mostrar que diferenças de baralho,
distribuição, decisão e vitória não exigem condicionais por jogo no framework.

## 7. Tabela de propriedade

| Trilha | Responsável | Área principal |
|---|---|---|
| A | Lucas | motor, contextos de execução, estado, turno e resultado |
| B | Júlio | carta, baralho, mão e distribuição |
| C | Allan | jogador, ações, decisão e I/O |
| D | Lívia | regras, eventos e exceções |
| E | Raffael | clientes, UML e relatório |

Propriedade não significa exclusividade. Mudança transversal deve ser revisada pelos
donos afetados. O merge entre A e C, por exemplo, preservou decisão por composição sem
forçar o engine a exigir uma estratégia para toda identidade.

## 8. Cobertura dos requisitos

| Requisito | Dono primário | Evidência |
|---|---|---|
| API pública | A + B + C + D | pacotes e Javadoc |
| ≥ 5 pontos de extensão | B + C + D + A | dez hot-spots |
| separação | A + E | `ClientesStubTest` |
| aplicação cliente | E | stubs atuais; versões completas pendentes |
| interfaces/abstrata | todos | contratos e motor abstrato |
| exceções | D | `api.excecao` |
| encapsulamento | B + A | testes de baralho, mão, configuração e resultado |
| testes | cada trilha | 103 testes integrados |
| Javadoc | cada dono | Maven Javadoc |
| UML | E, revisado por todos | PlantUML versionado |
| exemplos | E | Trinca e Blackjack |
| decisões | todos, consolidação E | documentos arquiteturais |

## 9. Padrões por trilha

| Padrão | Responsáveis | Evidência |
|---|---|---|
| Template Method | A | `MotorDePartida` |
| Strategy | B, C e D | distribuição, decisão e regras |
| Factory Method | B | `BaralhoFactory` |
| Observer | D + A | eventos/listener + publicação no motor |
| Builder auxiliar | A | `PartidaConfig.Builder` |

Decorator não é parte da baseline atual. Ele só deve entrar se jogos completos
exigirem combinações independentes de validação e a equipe conseguir demonstrar o
ganho sobre uma Strategy simples.

## 10. Estado das fases

| Fase | Estado | Critério de saída |
|---|---|---|
| modelo conceitual | concluída para a baseline | documentos coerentes |
| implementação A–D | baseline integrada | build e Javadoc verdes |
| integração arquitetural | concluída | dois stubs executam; 103 testes |
| Trinca completa | pendente | casos de aceitação de `regras-trinca.md` |
| Blackjack completo | pendente | casos de aceitação do escopo básico |
| API estável | pendente | clientes completos sem novo vazamento de internals |
| UML/relatório/defesa | em andamento | material revisado pelos cinco integrantes |

## 11. Convenções de Git

- branches de trilha permanecem locais até revisão;
- nunca trocar de branch com alterações não commitadas;
- usar commits pequenos por finalidade;
- documentação destinada à `main` deve estar em commit que altere somente `docs/`;
- levar esse commit por `cherry-pick`, em vez de copiar arquivos de uma árvore suja;
- não usar `--ours` ou `--theirs` globalmente em conflitos de API;
- não reescrever `main` publicada com `push --force`.

Fluxo recomendado para documentação:

```text
feature limpa
→ editar docs
→ git add docs/
→ commit exclusivamente documental
→ worktree limpo da main
→ cherry-pick do commit de docs
→ testes/revisão
→ push normal da main
```

## 12. Próxima coordenação da equipe

1. E transforma cada stub em cliente completo;
2. donos A–D respondem apenas a lacunas genéricas comprovadas por teste;
3. todos revisam o UML após os clientes completos;
4. cada integrante prepara a explicação da própria trilha e de uma trilha vizinha;
5. a equipe congela a API somente depois dessa validação.
