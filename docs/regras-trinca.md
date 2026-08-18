# Regras da Trinca - versão de referência do projeto

**Status:** regras aprovadas e aplicação cliente implementada na branch
`trilha-E/trinca`; o framework reutilizável permanece na `main`

**Validação:** 22 testes da Trinca e 160 testes na suíte integrada, sem falhas.

**Objetivo:** definir a variante de nove cartas que a equipe adotou como a Trinca
oficial do projeto.

> O nome e detalhes desse tipo de jogo variam regionalmente (Pife, Pif Paf,
> Cacheta, Pontinho ou Trinca). Este documento fixa a variante usada neste projeto;
> outra variante deve ser implementada por novas regras, sem alterar o `engine`.

## 1. Visão geral

Trinca é uma partida para **dois a cinco jogadores humanos** com um baralho francês
comum de 52 cartas, sem curingas. A quantidade é escolhida ao iniciar a aplicação. O
limite de cinco participantes mantém a partida adequada a um único baralho mesmo
depois de distribuir nove cartas por pessoa e iniciar o descarte. Cada jogador procura
organizar as cartas da mão em combinações válidas: trincas e sequências.

Exemplos: trinca `7 de copas, 7 de ouros, 7 de paus`; sequência `5, 6 e 7 de copas`.

A aplicação é executada em console. A primeira entrega será humano x humano; o
framework deve continuar independente da forma de decisão para permitir bots depois.

## 2. Componentes e preparação

| Elemento | Regra |
|---|---|
| Jogadores | De dois a cinco humanos, escolhidos na configuração inicial. |
| Baralho | Um `Baralho` francês com 52 cartas, sem curingas e sem cartas repetidas. |
| Mão inicial | Nove cartas por jogador. |
| Monte de compra | Cartas restantes, viradas para baixo. |
| Pilha de descarte | Começa com uma carta do monte, virada para cima. |
| Primeiro turno | Jogador definido pela estratégia de ordem de turnos; nesta versão, o primeiro jogador configurado. |

O baralho é embaralhado antes da distribuição. A distribuição ocorre alternando os
jogadores, uma carta por vez, até cada um possuir nove cartas.

## 3. Objetivo e condição de vitória

Um jogador vence imediatamente quando, após comprar uma carta, organiza **as nove
cartas que permanecerão em sua mão** em combinações válidas e descarta a décima
carta. A checagem acontece ao fim do turno.

- Uma trinca é formada por três cartas de mesmo valor e naipes distintos.
- Uma sequência é formada por três ou mais cartas consecutivas do mesmo naipe.
- O Ás pode ser usado antes do `2` ou depois do `K`, mas não pode ligar `K` a `2`.
- Todas as nove cartas devem participar de uma ou mais combinações válidas; não há
  carta solta na mão vencedora.
- A partida não termina por esgotamento do monte; a reciclagem do descarte mantém o
  ciclo de compras disponível até alguém vencer.

## 4. Fluxo de um turno

1. **Comprar uma carta:** escolher exatamente uma origem: o topo do monte de compra
   ou o topo da pilha de descarte.
2. **Ficar com dez cartas na mão:** após a compra, a mão possui dez cartas.
3. **Descartar uma carta:** escolher exatamente uma das dez cartas da mão e
   colocá-la no topo da pilha de descarte.
4. **Encerrar o turno:** o motor consulta a regra de vitória e passa a vez ao próximo
   jogador se a partida continuar, publicando `TurnoIniciado` e `TurnoEncerrado` aos
   observadores cadastrados.

Não é permitido pular compra, comprar duas cartas, descartar carta que não está na
mão ou encerrar o turno com dez cartas. Essas situações resultam em uma exceção de
jogada inválida tratada pelo console, sem encerrar a partida.

## 5. Esgotamento do monte

- Se houver carta no monte, a compra ocorre normalmente.
- Se não houver carta no monte e o descarte possuir ao menos duas cartas, **toda** a
  pilha de descarte, inclusive o topo anterior, volta para o monte e é embaralhada.
- Uma carta é retirada desse monte embaralhado para iniciar um novo descarte; por isso
  o novo topo é aleatório. A compra do jogador vem das cartas restantes.
- Se não houver carta no monte e o descarte possuir apenas o topo, comprar do monte
  não é oferecido. O jogador ainda pode comprar do descarte e, ao fim do turno,
  descarta outra carta, mantendo a partida em andamento.

Esta variante não possui empate por esgotamento: o monte é reciclado quando possível
e sempre permanece a alternativa de comprar a carta visível do descarte.

## 6. Pontuação

Cada rodada concede **1 ponto** ao vencedor e **0 ponto** aos demais. Depois de mostrar
o vencedor, a aplicação oferece uma nova rodada com os mesmos participantes e mantém
um placar acumulado. Uma nova instância de `MotorTrinca` e um novo baralho são criados
a cada rodada. O cálculo de cada resultado pertence à `RegraDePontuacaoStrategy`; a
aplicação apenas soma esses resultados enquanto o grupo escolher continuar.

## 7. Regras de validação separadas

| Validação | Resultado quando inválida |
|---|---|
| A partida está em andamento e é a vez do jogador | `EstadoDePartidaInvalidoException` ou `JogadaInvalidaException` |
| A origem de compra foi escolhida e possui carta disponível | `JogadaInvalidaException` ou `BaralhoVazioException` |
| Após comprar, o jogador possui dez cartas | `JogadaInvalidaException` |
| A carta escolhida para descarte pertence à mão do jogador | `JogadaInvalidaException` |
| Após descartar, o jogador possui nove cartas | `JogadaInvalidaException` |

A regra de vitória não rejeita jogadas: ela inspeciona o estado válido da mão no fim
do turno.

## 8. Experiência do console e eventos

Ao iniciar a aplicação:

1. o grupo escolhe entre dois e cinco participantes;
2. cada posição recebe uma das cinco cores fixas de jogador, escolhidas para terem
   contraste entre si; vermelho não pertence a essa paleta;
3. todas as mãos começam ordenadas por valor crescente.

A quantidade não usa menu: a aplicação mostra `Digite a quantidade de jogadores
(mínimo 2 e máximo 5):` e lê diretamente um inteiro no intervalo. Entradas não
numéricas ou fora da faixa repetem a pergunta sem iniciar a partida.

No início de cada turno, o terminal é limpo, solicita a troca de pessoa e então mostra
automaticamente um separador colorido e a mão do participante correto. Combinações
prontas são agrupadas e destacadas na cor dessa pessoa; as demais cartas respeitam a
ordenação atual. O próprio menu de compra ou descarte permite alternar entre valor
crescente e agrupamento por naipe. A última escolha fica guardada separadamente para
cada participante e será usada em seu próximo turno. Depois da compra, a mão de dez
cartas é reapresentada para que uma nova combinação apareça antes da escolha do
descarte.

Na reapresentação, a carta que acabou de entrar na mão é identificada explicitamente
como `CARTA COMPRADA NESTA JOGADA` e também recebe uma marca em sua opção de descarte.
Isso ocorre tanto para compras do monte quanto para compras do descarte, mesmo quando
a carta não completa nenhuma combinação.

A troca usa as sequências ANSI para apagar a tela visível, reposicionar o cursor e
solicitar ao terminal a remoção do histórico de rolagem. A mesma limpeza ocorre antes
do resultado, evitando que outra pessoa recupere a mão anterior apenas usando o
scroll. Esse isolamento depende de um emulador de terminal compatível com ANSI e com
o comando `CSI 3 J`.

Se a carta recém-comprada completar uma trinca ou sequência, essa nova combinação é
anunciada e destacada em vermelho. O destaque vermelho também aparece nas opções de
descarte e a confirmação dupla se aplica a qualquer carta do novo grupo. Ele é apenas
um alerta da etapa posterior à compra: depois do descarte, uma combinação que
permaneceu pronta volta a ser exibida na cor normal do jogador.

O topo do descarte é mostrado acima das opções de compra e reaparece caso a pessoa
mude a ordenação antes de escolher. A opção diz apenas “Comprar do descarte”, sem
repetir a carta. Se a pessoa selecionar para descarte uma carta pertencente a uma
combinação destacada, o console exige uma segunda confirmação e permite voltar à
lista sem alterar a mão.

O cliente de console deverá observar a partida; ele não deve ser chamado diretamente
pelo motor. Na baseline atual, o cliente pode cadastrar um `PartidaListener` e receber
os seis eventos padrão publicados por `MotorDePartida`: início de partida, cartas
distribuídas, início e fim de turno, jogada rejeitada e partida finalizada. Subclasses
também podem publicar eventos específicos pelo ponto protegido `publicarEvento`. Os
records de evento não expõem cartas privadas da mão de um jogador ao adversário.

## 9. Implicações para a API pública

Esta regra exige que a API permita, sem conhecer classes da Trinca:

1. representar uma carta com valor e naipe no cliente concreto;
2. criar e embaralhar um baralho de 52 cartas por uma `BaralhoFactory`;
3. distribuir uma quantidade configurável de cartas, incluindo nove por jogador;
4. consultar a mão de forma somente leitura e adicionar/remover cartas de modo
   controlado;
5. representar uma ação de turno tipada: compra por origem e descarte de carta;
6. escolher a ação por uma estratégia de decisão, humana ou automatizada;
7. configurar regras de validação, pontuação e vitória separadamente, por três
   Strategies independentes registradas em `PartidaConfig`;
8. receber eventos da partida cadastrando um `PartidaListener` no motor;
9. informar falhas por exceções de domínio.

Se algum item não puder ser atendido apenas pela API pública, a Trilha E deve
registrar a lacuna para o responsável pelo contrato. O pacote
`br.edu.uepb.map.trinca` não deve importar colaboradores internos de `engine`; o único
tipo desse pacote que o cliente conhece é `MotorDePartida`.

## 10. Casos de aceitação da Trinca

1. Uma partida inicia com a quantidade escolhida entre dois e cinco jogadores, nove
   cartas para cada um, uma carta no descarte e o restante no monte.
2. Um jogador compra e descarta uma carta, terminando novamente com nove cartas.
3. Um jogador compra o topo do descarte e o descarte passa a ter a carta escolhida
   pelo jogador.
4. Descartar uma carta ausente da mão é rejeitado e não altera o estado da partida.
5. Uma mão com nove cartas organizadas em três combinações válidas vence ao fim do
   turno.
6. Ao esgotar o monte, todo o descarte é reciclado, embaralhado e uma de suas cartas
   passa a ser o novo topo visível antes da compra.
7. Com monte vazio e apenas uma carta no descarte, somente a compra do descarte é
   oferecida e a partida continua, sem empate por esgotamento.
8. A mesma partida pode ser jogada por um humano e um bot sem alterar o motor.
9. Cores, ordenação e confirmação de descarte pertencem apenas à decisão humana de
   console e não alteram as regras nem o engine.
10. Uma combinação completada pela carta comprada aparece em vermelho somente até o
    descarte e suas cartas continuam protegidas pela confirmação dupla.
11. Depois do vencedor, o grupo pode iniciar outra rodada e o placar soma os resultados
    até a escolha de encerrar.
12. A troca de jogador apaga a tela e o histórico de rolagem antes de revelar a próxima
    mão.

## 11. Decisões deliberadamente fora do escopo

Não fazem parte da primeira versão: coringas, compra de múltiplas cartas, blefe,
tempo limite e regras regionais alternativas.

## 12. Decisões da equipe

- [x] a variante de nove cartas com trincas e sequências é a Trinca oficial;
- [x] a primeira demonstração será entre dois a cinco humanos;
- [x] o segundo jogo de demonstração será Blackjack básico;
- [x] a aplicação comprova a seção 9 sem importar internals;
- [ ] os contratos serão congelados somente depois da Trinca e do Blackjack completos.

### Escopo usado para validar com Blackjack

O escopo do segundo cliente é mantido exclusivamente na branch `jogo/blackjack`:
duas cartas iniciais, pedir/parar, Ás como 1 ou 11, casa compra até 17 e limite 21.
Seguro, divisão, dobro, rendição e pagamentos ficam fora da primeira versão.
