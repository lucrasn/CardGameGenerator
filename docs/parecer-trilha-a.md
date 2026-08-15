# Parecer técnico — Trilha A

**Responsável:** Lucas

**Escopo:** motor, ciclo de vida, turnos, contexto, estado e resultado

**Estado:** integrado seletivamente à `main` em 15/08/2026

## 1. Conclusão

A Trilha A está estruturalmente bem orientada para um framework: o fluxo fica em um
Template Method público, os colaboradores mutáveis são internos e o cliente recebe
contextos estreitos. A integração deixou de usar cópias próprias dos contratos das
outras trilhas e passou a consumir baralho/distribuição genéricos da `main`.

A implementação não deve ser apresentada como framework final completo. As Strategies
de regras e Observer pertencem à Trilha D e ainda são contratos vazios. Trinca e
Blackjack também não estão integrados na `main`.

## 2. Decisão de pacote

Estrutura adotada:

```text
api/
  EstadoPartida, PartidaConfig<C>, ContextoDePartida<C>,
  VisaoDaPartida<C>, ResultadoDoTurno, DesfechoDePartida,
  ResultadoDePartida, MotivoDeEncerramento...

engine/
  MotorDePartida<C>              public
  GerenciadorDeTurnos            package-private
  SentidoDeRotacao               package-private
  CicloDeVidaDaPartida           package-private
  PartidaEmExecucao<C>           package-private
  ContextoDeDistribuicaoInterno  package-private
```

Essa organização é honesta: turnos são um detalhe do runtime e ficam fisicamente ao
lado do motor. Não existe uma dependência `api → core`, e um cliente não consegue
instanciar o gerenciador.

O pacote `core` ainda existe para outras trilhas. As antigas classes da Trilha A em
`core` foram removidas para não manter duas fontes de verdade.

## 3. Template Method e IoC

`MotorDePartida.executar()` é `final`. Ele controla:

1. transições de estado;
2. criação e embaralhamento do baralho;
3. criação do agregado interno;
4. preparação e distribuição;
5. laço de turnos;
6. avaliação e pontuação;
7. validação e congelamento do resultado.

O jogo concreto não avança a vez nem finaliza diretamente. Ele devolve
`ResultadoDoTurno`, e o framework aplica o efeito. Essa é a evidência central de
Inversão de Controle.

## 4. Compatibilidade com a Trilha B

A feature anterior usava tipos não genéricos e métodos diferentes dos publicados.
A integração foi adaptada para:

- `MotorDePartida<C extends Carta>`;
- `PartidaConfig<C>`;
- `BaralhoFactory<C>.criar()`;
- `Baralho<C>.quantidade()`;
- `EstrategiaDeDistribuicao<C>`;
- `ContextoDeDistribuicao<C>.entregarProximaCarta()`;
- `MaoDeCartasPadrao<C>` como armazenamento interno.

Nenhum arquivo da Trilha B foi modificado pelo commit da Trilha A.

## 5. Compatibilidade com a Trilha C

O motor usa `Jogador` como identidade e ordem. Ele não assume construtores de
`JogadorPadrao`, não modifica a Strategy de decisão e não coloca mão ou pontuação no
jogador.

Os testes da Trilha A usam `JogadorDeTeste`, evitando depender de implementação
concreta de outra trilha. O dublê já usado por testes da distribuição foi preservado.

## 6. Limite imposto pela Trilha D

Foram preservadas sem alteração:

- `RegraDeValidacaoStrategy`;
- `RegraDeVitoriaStrategy`;
- `RegraDePontuacaoStrategy`;
- `PartidaListener`;
- exceções implementadas.

Como as quatro interfaces ainda não têm métodos, o motor não pode chamá-las. A solução
provisória foi:

- validação específica: o turno pode lançar `JogadaInvalidaException`;
- vitória: hook abstrato `avaliarDesfecho`;
- pontuação: hook `calcularPontuacao`, zero por padrão;
- eventos: não implementados.

Isso evita inventar unilateralmente assinaturas de outra trilha. Quando a Trilha D
for integrada, a equipe deve decidir se os hooks serão adaptados ou substituídos.

## 7. Encapsulamento

Pontos positivos:

- colaboradores de engine sem `public`;
- listas de jogadores copiadas;
- mãos consultadas como `List<C>` imutável;
- baralho não exposto pelo contexto;
- resultado e desfecho defensivamente copiados;
- mutação bloqueada no estado final;
- identidades de cartas verificadas entre baralho e mãos.

Decisão importante: embora `MaoDeCartas<C>` tenha mutadores públicos por contrato da
Trilha B, `VisaoDaPartida` não devolve essa interface. Assim uma regra de leitura não
consegue alterar uma mão.

## 8. Robustez

O motor recusa:

- execução repetida;
- configuração com menos de dois participantes;
- identidade de jogador repetida;
- fábrica, distribuição ou baralho nulos;
- resultado de turno nulo;
- avaliação nula;
- vencedor que não participa;
- placar nulo, incompleto, excedente ou duplicado por identidade;
- mutação após finalização.

Jogadas inválidas repetem o hook sem avançar a vez. O limite de 100 tentativas impede
que um bot defeituoso trave o processo indefinidamente.

## 9. SOLID e GRASP

- **SRP / Alta Coesão:** fluxo, estado, turnos e agregado estão separados;
- **OCP / Polimorfismo:** o motor é especializado por hooks e tipos genéricos;
- **LSP:** o algoritmo público é final;
- **ISP:** visão de leitura, contexto de mutação e distribuição têm autoridades
  diferentes;
- **DIP:** engine consome abstrações em `api`;
- **Especialista:** estado conhece transições; gerenciador conhece rotação;
- **Controlador:** motor coordena o caso de uso;
- **Creator:** motor cria a execução interna.

## 10. Testes

Cobertura da Trilha A:

- grafo e terminalidade dos estados;
- construção e imutabilidade da configuração;
- motivos, desfecho, resultado e diretivas;
- visibilidade package-private dos internals;
- avanço para N participantes, inversão e pulos;
- transferência e unicidade de cartas;
- sequência do Template Method;
- repetição de jogada inválida;
- encerramento antes do primeiro turno;
- validação de vencedores e placar;
- bloqueio de segunda execução e mutação final.

Resultado integrado: **105 testes, zero falhas e zero erros**.

## 11. Débitos e próximos passos

1. receber os contratos aprovados da Trilha D;
2. integrar Strategy de regras sem manter duas variações redundantes;
3. implementar Observer e eventos com testes de isolamento;
4. adicionar testes arquiteturais automáticos de dependência;
5. validar a API com Trinca e Blackjack concretos;
6. revisar o limite de tentativas caso o modo de execução passe a ser assíncrono;
7. revisar concorrência se partidas forem executadas por múltiplas threads.

## 12. Veredito

A Trilha A pode permanecer na `main`: compila com os contratos reais das Trilhas B e
C, não altera contratos vazios da D e possui testes proporcionais ao risco. A
arquitetura ainda precisa da integração D/E antes de sustentar a afirmação “framework
capaz de gerar qualquer jogo de cartas”; no estado atual, a formulação defensável é
“runtime genérico e extensível para uma família de jogos, validado parcialmente”.
