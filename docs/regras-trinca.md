# Regras da Trinca - versão de referência do projeto

**Status:** regras aprovadas para orientar a Fase 0  
**Objetivo:** definir a variante de nove cartas que a equipe adotou como a Trinca
oficial do projeto.

> O nome e detalhes desse tipo de jogo variam regionalmente (Pife, Pif Paf,
> Cacheta, Pontinho ou Trinca). Este documento fixa a variante usada neste projeto;
> outra variante deve ser implementada por novas regras, sem alterar o `core`.

## 1. Visão geral

Trinca é uma partida para **dois jogadores humanos** com dois baralhos franceses
comuns, sem curingas. Cada jogador procura organizar as cartas da mão em combinações
válidas: trincas e sequências.

Exemplos: trinca `7 de copas, 7 de ouros, 7 de paus`; sequência `5, 6 e 7 de copas`.

A aplicação é executada em console. A primeira entrega será humano x humano; o
framework deve continuar independente da forma de decisão para permitir bots depois.

## 2. Componentes e preparação

| Elemento | Regra |
|---|---|
| Jogadores | Dois humanos na configuração inicial; a API deve aceitar uma coleção para futura expansão. |
| Baralho | Dois baralhos de 52 cartas: 104 cartas, sem curingas. |
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
- Se não houver mais cartas para comprar ou reciclar, a partida termina empatada.

## 4. Fluxo de um turno

1. **Comprar uma carta:** escolher exatamente uma origem: o topo do monte de compra
   ou o topo da pilha de descarte.
2. **Ficar com dez cartas na mão:** após a compra, a mão possui dez cartas.
3. **Descartar uma carta:** escolher exatamente uma das dez cartas da mão e
   colocá-la no topo da pilha de descarte.
4. **Encerrar o turno:** o motor publica os eventos relevantes, verifica a vitória e
   passa a vez ao próximo jogador se a partida continuar.

Não é permitido pular compra, comprar duas cartas, descartar carta que não está na
mão ou encerrar o turno com dez cartas. Essas situações resultam em uma exceção de
jogada inválida tratada pelo console, sem encerrar a partida.

## 5. Esgotamento do monte

- Se houver carta no monte, a compra ocorre normalmente.
- Se não houver, as cartas da pilha de descarte, exceto o topo, são embaralhadas e
  passam a formar o novo monte.
- Se a pilha só possuir a carta do topo, não há carta para reciclar e a partida
  termina empatada.

O topo do descarte é preservado para que a opção de compra do descarte continue
existindo após a reciclagem.

## 6. Pontuação

Esta primeira versão é de uma única rodada: vitória concede **1 ponto** ao vencedor;
empate concede **0 ponto** a ambos. O cálculo fica em uma
`RegraDePontuacaoStrategy`, e não no console nem no motor.

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

## 8. Eventos esperados pela aplicação de console

O cliente de console observa a partida; ele não deve ser chamado diretamente pelo
motor. A Trinca necessita, no mínimo, dos eventos: partida iniciada/cartas
distribuídas, turno iniciado, carta comprada, carta descartada, jogada inválida,
turno encerrado e partida finalizada. Os eventos não devem vazar cartas da mão de um
jogador ao adversário.

## 9. Implicações para a API pública

Esta regra exige que a API permita, sem conhecer classes da Trinca:

1. representar uma carta com valor e naipe no cliente concreto;
2. criar e embaralhar dois baralhos de 52 cartas por uma `BaralhoFactory`;
3. distribuir uma quantidade configurável de cartas, incluindo nove por jogador;
4. consultar a mão de forma somente leitura e adicionar/remover cartas de modo
   controlado;
5. representar uma ação de turno tipada: compra por origem e descarte de carta;
6. escolher a ação por uma estratégia de decisão, humana ou automatizada;
7. configurar regras de validação, pontuação e vitória separadamente;
8. receber eventos da partida por listeners;
9. informar falhas por exceções de domínio.

Se algum item não puder ser atendido apenas pela API pública, a Trilha E deve
registrar a lacuna para o responsável pelo contrato. O pacote
`br.edu.uepb.map.trinca` não deve importar classes internas de `core`.

## 10. Casos de aceitação da Trinca

1. Uma partida inicia com dois jogadores, nove cartas para cada um, uma carta no
   descarte e o restante no monte.
2. Um jogador compra e descarta uma carta, terminando novamente com nove cartas.
3. Um jogador compra o topo do descarte e o descarte passa a ter a carta escolhida
   pelo jogador.
4. Descartar uma carta ausente da mão é rejeitado e não altera o estado da partida.
5. Uma mão com nove cartas organizadas em três combinações válidas vence ao fim do
   turno.
6. Ao esgotar o monte, o descarte é reciclado e seu topo é preservado.
7. Quando não há cartas para comprar ou reciclar, a partida termina em empate.
8. A mesma partida pode ser jogada por um humano e um bot sem alterar o motor.

## 11. Decisões deliberadamente fora do escopo

Não fazem parte da primeira versão: coringas, compra de múltiplas cartas, blefe,
tempo limite, pontuação acumulada em várias rodadas e regras regionais alternativas.

## 12. Decisões da equipe

- [x] a variante de nove cartas com trincas e sequências é a Trinca oficial;
- [x] a primeira demonstração será humano x humano;
- [x] o segundo jogo de demonstração será Blackjack com regras completas da mesa
      escolhida pela equipe;
- [ ] os contratos públicos serão congelados com base na seção 9.

### Regra ainda necessária para fechar o Blackjack

"Blackjack normal" não é um regulamento único: cassinos variam, por exemplo, em
`dealer hits/stands on soft 17`, número de baralhos, seguro, divisão e pagamento do
blackjack natural. Antes da implementação do segundo cliente, a equipe precisa
escolher e registrar uma mesa de regras específica.
