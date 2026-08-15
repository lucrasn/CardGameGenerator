# Regras do Blackjack básico — cliente de validação

**Status:** escopo mínimo aprovado arquiteturalmente; detalhes de interface ainda
podem ser refinados pela Trilha E.

## 1. Objetivo

Blackjack básico demonstra um fluxo diferente da Trinca sem exigir apostas, múltiplas
mãos ou regras de cassino que ampliariam o framework.

## 2. Participantes e baralho

- um jogador humano;
- uma “casa” representada como participante com estratégia automatizada;
- um baralho francês de 52 cartas, sem curingas;
- duas cartas iniciais para cada participante.

“Casa” é um papel da aplicação, não uma subclasse obrigatória de jogador do framework.

## 3. Valores

- cartas 2–10 valem o número;
- J, Q e K valem 10;
- Ás vale 11, ou 1 quando necessário para não ultrapassar 21.

Esse cálculo pertence às cartas/regras do Blackjack.

## 4. Turno do jogador

O jogador escolhe:

- `PEDIR`: compra uma carta e pode decidir novamente;
- `PARAR`: encerra suas decisões.

A estratégia humana ou automatizada produz uma `Jogada` tipada a partir de um
`ContextoDeDecisao` específico do Blackjack.

## 5. Turno da casa

- pede carta com total menor que 17;
- para com total 17 ou maior;
- encerra se ultrapassar 21.

## 6. Desfecho

- participante que ultrapassa 21 perde;
- se apenas a casa ultrapassa 21, o jogador vence;
- se nenhum ultrapassa, vence o maior total;
- totais iguais produzem empate.

Pontuação pode ser neutra nesta demonstração. Não há pagamentos.

## 7. Fora do escopo

- apostas e saldo;
- seguro;
- split e múltiplas mãos;
- double down;
- rendição;
- pagamentos 3:2 ou 2:1;
- várias rodadas.

## 8. Capacidades exercitadas

O cliente valida:

1. composição própria de baralho;
2. distribuição diferente da Trinca;
3. carta com cálculo de valor específico;
4. decisão automatizada por Strategy tipada;
5. pedir/repetir e parar/avançar por `ResultadoDoTurno`;
6. regra de vitória por limite;
7. ausência de tipo `Dealer` dentro do framework;
8. uso exclusivo de `api` e `engine.MotorDePartida`.

## 9. Casos de aceitação

1. cada participante recebe duas cartas;
2. Ás pode valer 1 ou 11;
3. pedir acrescenta exatamente uma carta;
4. parar encerra decisões daquele participante;
5. casa respeita o limite 17;
6. estouro, vitória e empate são reconhecidos;
7. nenhuma alteração no engine é necessária.
