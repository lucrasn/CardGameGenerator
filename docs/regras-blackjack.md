# Regras do Blackjack - mesa de referência do projeto

**Status:** proposta para aprovação da equipe.  
**Objetivo arquitetural:** validar que o framework suporta um jogo sem descarte,
pontuação por valor acumulado, dealer automatizado, ações adicionais e múltiplas mãos.

> "Blackjack normal" varia entre mesas. Este documento fixa uma configuração para o
> projeto; não pretende representar todas as regras de cassino.

## 1. Configuração da mesa

| Item | Decisão do projeto |
|---|---|
| Participantes | Um jogador humano contra um dealer automatizado |
| Baralho | Um baralho francês de 52 cartas, sem coringas |
| Embaralhamento | Novo embaralhamento no início de cada partida |
| Aposta inicial | Valor inteiro positivo configurado; saldo e fichas pertencem ao cliente Blackjack |
| Blackjack natural | Ás + carta de valor 10 nas duas cartas iniciais; paga 3:2 |
| Dealer | Compra com 16 ou menos e para em qualquer 17, inclusive soft 17 (S17) |
| Seguro | Oferecido quando a carta aberta do dealer é Ás; paga 2:1 se houver Blackjack do dealer |
| Rendição | Rendição tardia antes de pedir carta; devolve metade da aposta |
| Dobrar | Permitido nas duas cartas iniciais, inclusive depois de dividir; recebe exatamente mais uma carta |
| Dividir | Permitido para par de mesmo valor, até quatro mãos; ases divididos recebem uma carta por mão |

## 2. Valores das cartas

- `2` a `10`: valor nominal;
- `J`, `Q` e `K`: 10 pontos;
- Ás: 11, ou 1 quando 11 faria a mão ultrapassar 21.

Uma mão é `soft` quando ao menos um Ás ainda vale 11. Uma mão estoura quando seu menor
total possível é maior que 21.

## 3. Preparação

1. validar saldo e registrar a aposta inicial;
2. distribuir duas cartas ao jogador e duas ao dealer, alternadamente;
3. deixar uma carta do dealer aberta e a outra oculta;
4. verificar Blackjack natural do jogador e do dealer;
5. oferecer seguro se a carta aberta do dealer for Ás;
6. iniciar as decisões do jogador se a partida não tiver terminado.

## 4. Ações do jogador

- **Pedir carta:** acrescenta uma carta à mão ativa; pode repetir até parar ou estourar.
- **Parar:** encerra as decisões da mão ativa.
- **Dobrar:** dobra a aposta, entrega uma única carta e encerra a mão.
- **Dividir:** cria duas mãos a partir de um par e associa uma aposta igual a cada uma.
- **Render-se:** encerra a mão e devolve metade da aposta inicial.
- **Seguro:** registra aposta lateral de até metade da aposta principal.

Cada ação é um `record` próprio no pacote `blackjack` e implementa a interface aberta
`Jogada`. O framework não precisa conhecer esses tipos.

## 5. Turno do dealer

Depois que todas as mãos do jogador terminarem sem que todas estejam estouradas ou
rendidas, o dealer revela a carta oculta. Sua estratégia é obrigatória:

- pedir carta com total 16 ou menor;
- parar em total 17 ou maior, inclusive soft 17;
- encerrar imediatamente se estourar.

## 6. Resultado e pagamento

Cada mão do jogador é comparada separadamente ao dealer:

- mão estourada perde;
- dealer estourado faz vencer toda mão ativa não estourada;
- maior total até 21 vence;
- totais iguais empatam e devolvem a aposta;
- Blackjack natural vence 21 formado com três ou mais cartas;
- Blackjack natural paga 3:2; vitória comum paga 1:1;
- seguro vencedor paga 2:1;
- rendição devolve metade da aposta.

Saldo, aposta e pagamento são estado específico do cliente Blackjack. O framework
fornece cartas, mãos, jogadores, fluxo, decisão, eventos e resultado, mas não conhece
dinheiro ou regras de cassino.

## 7. Casos de aceitação

1. jogador pode pedir carta e parar sem alterar o `core`;
2. dealer segue automaticamente S17;
3. Ás vale 11 ou 1 conforme a mão;
4. uma mão com mais de 21 perde;
5. Blackjack natural é distinguido de 21 com mais cartas;
6. dividir cria duas mãos independentes para o mesmo jogador;
7. dobrar entrega exatamente uma carta e encerra a mão;
8. seguro, rendição, empate e pagamentos seguem a seção 6;
9. todas as ações e regras concretas ficam no pacote `blackjack`;
10. nenhuma alteração em `cardgame.core` é necessária.

## 8. Aprovação

- [ ] equipe aprova um baralho, S17, Blackjack 3:2 e seguro 2:1;
- [ ] equipe aprova split até quatro mãos e double after split;
- [ ] equipe aprova rendição tardia;
- [ ] Trilha D confirma que os contextos públicos permitem validar essas ações;
- [ ] Trilha E confirma que aposta e saldo ficam integralmente no cliente Blackjack.

## 9. Referência

Configuração baseada em regras públicas de Blackjack que incluem hit/stand, split,
double down, seguro e rendição. A equipe deve citar a regra de mesa escolhida no
relatório final: <https://www.casinosbc.com/content/dam/casinosbc/about-games/how-to-play/blackjack/Rules-For-Play-Blackjack.pdf>.
