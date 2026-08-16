# Divisão de responsabilidades

**Equipe:** Lucas · Allan · Raffael · Lívia · Júlio

**Cliente principal:** Trinca

**Segundo cliente de extensibilidade:** Blackjack básico

**Status em 16/08/2026:** parte reutilizável das Trilhas A, B, C e D integrada à
`main`. Falta a camada de aplicação, sob responsabilidade da Trilha E.

## 1. Regra de colaboração

O corte é por propriedade de contrato, não por quantidade de classes. Cada trilha:

1. define e implementa seus tipos;
2. escreve os testes e o Javadoc desses tipos;
3. não completa silenciosamente interfaces pertencentes a outro integrante;
4. negocia mudanças de assinatura antes de alterar consumidores;
5. mantém a `main` compilando.

Essa regra evita o problema encontrado na antiga feature da Trilha A: ela havia
criado versões próprias de contratos de baralho, jogador, regras e eventos para
conseguir executar isoladamente. A integração atual remove esse viés e usa os
contratos reais da `main`.

## 2. Trilha A — Motor e ciclo de vida

**Responsável:** Lucas.

Tipos públicos de responsabilidade da trilha:

- `api.EstadoPartida`;
- `api.PartidaConfig<C>`;
- `api.VisaoDaPartida<C>` e `api.ContextoDePartida<C>`;
- `api.ResultadoDoTurno`;
- `api.DesfechoDePartida`, `api.ResultadoDePartida`;
- `api.MotivoDeEncerramento` e `api.MotivoPadrao`;
- `engine.MotorDePartida<C>`.

Detalhes internos, todos sem `public`:

- `GerenciadorDeTurnos`;
- `SentidoDeRotacao`;
- `CicloDeVidaDaPartida`;
- `PartidaEmExecucao<C>`;
- `ContextoDeDistribuicaoInterno<C>`.

Responsabilidades:

- controlar a sequência preparar → distribuir → jogar → avaliar → pontuar → encerrar;
- manter a máquina de estados e impedir segunda execução;
- avançar, repetir, inverter e pular turnos para N participantes;
- proteger baralho e mãos por um contexto de operações controladas;
- validar vencedores e a cobertura do placar;
- manter o runtime independente de Trinca e Blackjack.

Não pertencem à Trilha A: decidir a composição do baralho, implementar decisão de
jogador, definir as assinaturas das regras ou criar o protocolo de eventos.

## 3. Trilha B — Cartas, baralho, mãos e distribuição

**Responsável:** Júlio.

Tipos principais:

- `Carta`;
- `Baralho<C>`, `BaralhoPadrao<C>` e `BaralhoFactory<C>`;
- `MaoDeCartas<C>` e `MaoDeCartasPadrao<C>`;
- `ContextoDeDistribuicao<C>`;
- `EstrategiaDeDistribuicao<C>` e `DistribuicaoAlternada<C>`;
- o contexto concreto de distribuição pertence ao runtime interno do engine.

Responsabilidades:

- identidade estável de cada carta;
- encapsulamento e snapshots imutáveis;
- operações de topo/base, compra e embaralhamento;
- criação de composições diferentes por Factory Method;
- distribuição substituível por Strategy.

O motor consome exatamente `BaralhoFactory.criar()`, `Baralho.quantidade()` e
`ContextoDeDistribuicao.entregarProximaCarta()`. A Trilha A não mantém uma cópia
alternativa desses contratos.

## 4. Trilha C — Jogadores e decisão

**Responsável:** Allan.

Tipos principais:

- `Jogador` e `JogadorPadrao`;
- `Jogada`, `EtapaDeTurno` e `ContextoDeDecisao`;
- `EstrategiaDeDecisao`;
- estratégias humana, aleatória e gulosa;
- abstrações de entrada e saída.

Responsabilidades:

- separar identidade de comportamento;
- trocar humano, bot e dealer por composição;
- não colocar mãos nem fluxo da partida dentro do jogador;
- manter console fora do engine.

O motor conhece apenas `Jogador.id()` e `Jogador.nome()` para identidade, ordem e
mensagens. Ele não chama diretamente uma estratégia de decisão.

## 5. Trilha D — Regras, eventos e exceções

**Responsável:** Lívia.

Tipos reservados:

- `RegraDeValidacaoStrategy`;
- `RegraDeVitoriaStrategy`;
- `RegraDePontuacaoStrategy`;
- `PartidaListener` e futuros eventos;
- `PartidaException` e subclasses.

Estado atual: **concluída**.

- exceções de domínio implementadas;
- as três Strategies de regra têm operação definida e são obrigatórias em
  `PartidaConfig`;
- `EventoDePartida` e os seis eventos padrão existem em `api.evento`;
- Observer implementado, com o motor publicando os eventos.

A integração eliminou os hooks provisórios: vitória e pontuação, que eram métodos
protegidos do motor, passaram a Strategies da configuração. Existe uma única fonte de
variação para cada decisão.

## 6. Trilha E — Clientes, UML e relatório

**Responsável:** Raffael.

Entregas:

- aplicação cliente Trinca em pacote separado;
- cliente mínimo de Blackjack para provar extensibilidade;
- UML consolidado e relatório final;
- testes de aceitação que importem somente a API pública e `MotorDePartida`.

Os clientes não devem importar nenhum tipo interno de `engine`. Se precisarem fazê-lo,
isso revela um contrato público ausente e deve ser discutido com a trilha proprietária.

## 7. Matriz de dependências

| Consumidor | Pode depender de | Não deve depender de |
|---|---|---|
| `api` | apenas a biblioteca padrão | `engine`, jogos concretos |
| `engine` | `api` | jogos concretos |
| Trinca/Blackjack | `api`, `engine.MotorDePartida` | colaboradores internos de `engine` |
| Strategies | visões e contextos públicos | estado mutável interno |

## 8. Padrões e proprietários

| Padrão | Responsável principal | Estado |
|---|---|---|
| Template Method | A | implementado em `MotorDePartida.executar()` |
| Factory Method | B | implementado em `BaralhoFactory.criar()` |
| Strategy de decisão/distribuição | B/C | implementado |
| Strategy de regras | D | implementado |
| Observer | D | implementado |

Builder em `PartidaConfig` é apoio de construção e não deve ser contado entre os
quatro padrões GoF exigidos pela disciplina.

## 9. Checklist de integração

Antes de mesclar uma trilha:

- [ ] o diff contém apenas arquivos próprios ou mudanças conjuntas aprovadas;
- [ ] nenhuma interface de outra trilha foi preenchida unilateralmente;
- [ ] não há duplicação pública de um mesmo conceito;
- [ ] coleções expostas são snapshots imutáveis;
- [ ] `./mvnw test` passa;
- [ ] Javadoc e UML representam o código atual;
- [ ] pendências são marcadas como pendências, não como padrões implementados.

Baseline verificada nesta integração: **133 testes, zero falhas e zero erros**.
