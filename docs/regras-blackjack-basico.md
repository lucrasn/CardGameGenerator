# Regras do Blackjack básico — cliente de validação

**Status:** implementado na branch `jogo/blackjack`, com aplicação interativa e 30
testes próprios integrados à suíte do projeto.

## 1. Objetivo

Blackjack básico demonstra um fluxo diferente da Trinca sem exigir apostas, múltiplas
mãos ou regras de cassino que ampliariam o framework.

## 2. Participantes e baralho

- um jogador humano;
- uma “casa” representada como participante com estratégia automatizada;
- um baralho francês de 52 cartas, sem curingas;
- duas cartas iniciais para cada participante.

“Casa” é um papel da aplicação, não uma subclasse obrigatória de jogador do framework.
Enquanto o jogador decide, a primeira carta da casa fica visível e a segunda permanece
fechada. A mão completa é revelada antes da primeira decisão automatizada da casa.

## 3. Valores

- cartas 2–10 valem o número;
- J, Q e K valem 10;
- Ás vale 11, ou 1 quando necessário para não ultrapassar 21.

Esse cálculo pertence às cartas/regras do Blackjack.

## 4. Turno do jogador

O jogador escolhe:

- `PEDIR`: compra uma carta e pode decidir novamente;
- `PARAR`: encerra suas decisões.

Ao alcançar exatamente 21, somente `PARAR` continua disponível. Ao ultrapassar 21, a
rodada termina imediatamente.

A estratégia humana ou automatizada produz uma `Jogada` a partir de um
`ContextoDeDecisao` específico do Blackjack. O contrato base permanece independente
das ações concretas; o cliente interpreta a jogada devolvida.

## 5. Turno da casa

- pede carta com total menor que 17;
- para com total 17 ou maior;
- encerra se ultrapassar 21.

## 6. Desfecho

- participante que ultrapassa 21 perde;
- se apenas a casa ultrapassa 21, o jogador vence;
- se nenhum ultrapassa, vence o maior total;
- totais iguais produzem empate.

Um total de 21 com exatamente as duas cartas iniciais é Blackjack natural. Se somente
um participante o formar, ele vence antes do primeiro turno; dois naturais produzem
empate.

O placar da rodada concede um ponto ao vencedor e zero aos demais. Em um empate,
ninguém pontua. Não há pagamentos.

## 7. Fora do escopo

- apostas e saldo;
- seguro;
- split e múltiplas mãos;
- double down;
- rendição;
- pagamentos 3:2 ou 2:1;
- campanha ou um sapato persistente entre rodadas.

A interface permite jogar novamente e conserva apenas um placar simples de vitórias;
cada rodada usa um baralho novo e constitui uma partida independente do framework.

## 8. Capacidades exercitadas

O cliente implementado valida:

1. composição própria de baralho;
2. distribuição diferente da Trinca;
3. carta com cálculo de valor específico;
4. decisão automatizada por Strategy sobre ações tipadas;
5. pedir/repetir e parar/avançar por `ResultadoDoTurno`;
6. regra de vitória por limite;
7. ausência de tipo `Dealer` dentro do framework;
8. uso exclusivo de `api` e `engine.MotorDePartida`;
9. eventos próprios publicados pelo ponto protegido do motor;
10. ocultação da carta fechada no contexto entregue à Strategy humana.

## 9. Casos de aceitação

1. [x] cada participante recebe duas cartas;
2. [x] Ás pode valer 1 ou 11, inclusive em mãos com vários ases;
3. [x] pedir acrescenta exatamente uma carta e repete o participante;
4. [x] parar encerra as decisões e avança a vez;
5. [x] casa pede abaixo de 17 e para a partir de 17, inclusive suave;
6. [x] naturais, estouros, maior pontuação e empate são reconhecidos;
7. [x] nenhuma alteração no engine foi necessária;
8. [x] a carta fechada não chega ao contexto da decisão humana;
9. [x] o placar concede um ponto ao vencedor e zero em empates;
10. [x] a fronteira arquitetural falha o build diante de acesso a internals.

## 10. Organização da implementação

| Componente | Responsabilidade |
|---|---|
| `CartaBlackjack`, `ValorBlackjack`, `NaipeBlackjack` | vocabulário do baralho francês |
| `BaralhoBlackjackFactory` | criar 52 cartas novas por partida |
| `PontuacaoDaMaoBlackjack` | calcular total, mão suave, natural e estouro |
| `AcaoBlackjack` | representar `PEDIR` e `PARAR` |
| `ContextoDecisaoBlackjack` | entregar uma visão segura às Strategies |
| `DecisaoHumanaBlackjackConsole` | interação da pessoa pelo terminal |
| `EstrategiaCasaBlackjack` | política automática de compra até 17 |
| `RegraValidacaoBlackjack` | recusar ações ilegais antes da mutação |
| `RegraVitoriaBlackjack` | reconhecer e explicar o desfecho |
| `RegraPontuacaoBlackjack` | converter o desfecho em placar |
| `MotorBlackjack` | implementar o passo variável de um turno |
| `MesaBlackjack` | guardar paradas, papéis e últimas compras |
| `ApresentadorBlackjackConsole` | observar eventos e renderizar a vez da casa |
| `AplicacaoBlackjack` | montar participantes, rodadas e placar acumulado |

O engine não recebeu `Dealer`, limite 21, valor de Ás, carta fechada ou política de 17.
Esses conceitos aparecem somente no cliente, preservando OCP, DIP e Information Expert.

## 11. Padrões demonstrados

- **Template Method:** `MotorDePartida.executar()` continua controlando o ciclo;
- **Factory Method:** `BaralhoBlackjackFactory` fornece a composição do baralho;
- **Strategy:** decisão humana, decisão da casa, validação, vitória, pontuação e
  distribuição variam por composição;
- **Observer:** o motor publica revelação da casa, compra e parada como eventos próprios;
- **Builder:** `PartidaConfig` monta as colaborações sem construtor posicional longo;
- **Value Object:** cartas, pontuação da mão, contextos e eventos são imutáveis.

## 12. Executar e validar

Requisito: JDK 26. Na raiz do repositório:

```bash
./mvnw compile && java -cp target/classes br.edu.uepb.map.blackjack.AplicacaoBlackjack
```

Para executar toda a suíte, incluindo as regras arquiteturais:

```bash
./mvnw test
```
