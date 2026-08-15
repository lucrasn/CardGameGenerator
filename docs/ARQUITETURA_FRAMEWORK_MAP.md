# Projeto Final MAP — Arquitetura do Framework de Jogos de Cartas

## 1. Objetivo deste documento

Este documento estabelece o novo ponto de partida do projeto.

A equipe deve projetar primeiro um **framework reutilizável para jogos de cartas** e somente depois implementar as aplicações cliente, como Trinca e Blackjack.

A Trinca não deve definir a arquitetura do framework. Ela será utilizada posteriormente como uma primeira aplicação cliente para validar se a infraestrutura projetada é realmente reutilizável.

---

## 2. Base do projeto

O enunciado do Projeto Final exige uma infraestrutura reutilizável capaz de servir de base para diferentes jogos de cartas.

A solução deve contemplar, no mínimo:

- Cartas;
- Baralho;
- Jogadores;
- Mão de cartas;
- Regras do jogo;
- Partida.

Também deve permitir diferenças entre jogos quanto a:

- tipos de cartas;
- formas de distribuição;
- regras;
- condições de vitória;
- estratégias de tomada de decisão;
- eventos durante a partida.

A implementação deve ser acompanhada do projeto arquitetural e de testes automatizados.

---

## 3. Requisitos obrigatórios que orientam a arquitetura

O framework deverá possuir:

1. API pública claramente definida;
2. pelo menos cinco pontos de extensão;
3. separação entre código da solução e código das aplicações clientes;
4. pelo menos uma aplicação cliente;
5. interfaces e classes abstratas;
6. tratamento adequado de exceções;
7. encapsulamento das coleções internas;
8. testes automatizados;
9. documentação Javadoc da API pública;
10. diagrama de classes simplificado;
11. exemplos de utilização;
12. justificativa das decisões de projeto.

Além disso, o projeto deve justificar:

- responsabilidades das classes principais;
- relacionamentos entre classes;
- uso de herança e composição;
- baixo acoplamento;
- alta coesão;
- especialista na informação;
- pontos de extensão;
- padrões GoF utilizados;
- princípios GRASP;
- princípios SOLID.

A solução deve utilizar pelo menos quatro padrões GoF estudados na disciplina.

---

# 4. Princípio arquitetural central

## Framework primeiro, jogos depois

A ordem de desenvolvimento será:

```text
Domínio do framework
        ↓
Responsabilidades
        ↓
Variações entre jogos
        ↓
Pontos de extensão
        ↓
Arquitetura
        ↓
API pública × implementação interna
        ↓
Implementação do framework
        ↓
Testes do framework
        ↓
Aplicação cliente: Trinca
        ↓
Aplicação cliente: Blackjack
        ↓
UML e relatório final
```

A pergunta principal deixa de ser:

> "O que a Trinca precisa que o framework tenha?"

e passa a ser:

> "O que um framework reutilizável para jogos de cartas precisa oferecer para permitir diferentes jogos?"

A Trinca será utilizada posteriormente para verificar se essa arquitetura funciona.

---

# 5. Etapa 1 — Definição do domínio do framework

Antes de escrever código, a equipe deve identificar as abstrações gerais presentes em jogos de cartas.

As abstrações mínimas fornecidas pelo enunciado são:

- `Carta`
- `Baralho`
- `Jogador`
- `MaoDeCartas`
- `RegraDoJogo`
- `Partida`

A equipe poderá acrescentar outras abstrações quando houver uma necessidade arquitetural clara.

## Regra

Uma abstração só deve ser adicionada ao framework quando representar um conceito reutilizável entre diferentes jogos.

Não criar abstrações específicas da Trinca ou do Blackjack dentro do framework.

---

# 6. Etapa 2 — Responsabilidades

Para cada abstração, definir:

- o que ela representa;
- quais informações possui;
- quais operações realiza;
- quais responsabilidades NÃO possui;
- com quais outras abstrações se relaciona.

Exemplo de raciocínio:

### Carta

Deve representar uma carta.

Não deve conhecer:

- regras da Trinca;
- regras do Blackjack;
- condição de vitória;
- fluxo da partida.

### Jogador

Deve representar um participante da partida.

Não deve conter regras específicas de um jogo concreto.

### Partida

Deve coordenar uma partida de acordo com as abstrações configuradas.

Não deve conter condicionais específicas como:

```text
if jogo == Trinca
if jogo == Blackjack
```

Essas decisões seriam sinais de acoplamento indevido.

---

# 7. Etapa 3 — Identificação das variações

Depois de definir as responsabilidades, identificar quais comportamentos podem variar entre jogos.

O enunciado indica como exemplos:

- tipos de cartas;
- distribuição;
- regras;
- vitória;
- estratégia de decisão;
- eventos.

Para cada variação, responder:

1. O que varia?
2. Por que varia?
3. Quem deve ser responsável por isso?
4. Como um novo jogo poderia fornecer outro comportamento?
5. Isso constitui um ponto de extensão?

---

# 8. Etapa 4 — Pontos de extensão

O projeto exige pelo menos cinco pontos de extensão.

Os candidatos devem ser identificados a partir da análise do domínio, e não escolhidos apenas para cumprir numericamente o requisito.

Para cada ponto de extensão, registrar:

| Campo | Descrição |
|---|---|
| Ponto de extensão | Nome do comportamento variável |
| Problema | O que pode mudar entre jogos |
| Abstração | Interface/classe abstrata responsável |
| Implementação padrão | Se existir |
| Exemplo | Como Trinca/Blackjack poderiam variar |
| Justificativa | Por que a extensão é necessária |

Possíveis candidatos iniciais, a validar pela equipe:

- tipo de carta;
- distribuição;
- regras de validação;
- condição de vitória;
- pontuação;
- estratégia de decisão do jogador;
- eventos.

Esses são apenas candidatos. A decisão final deve ocorrer depois da análise das responsabilidades.

---

# 9. Etapa 5 — Arquitetura antes dos padrões

Os padrões GoF não devem ser escolhidos previamente apenas para atingir quatro padrões.

A ordem será:

```text
Problema arquitetural
        ↓
Comportamento que varia
        ↓
Ponto de extensão
        ↓
Solução arquitetural
        ↓
Padrão GoF adequado, se houver
```

Para cada padrão utilizado, documentar:

- onde foi aplicado;
- qual problema resolve;
- por que é adequado;
- qual seria o problema sem ele;
- como contribui para baixo acoplamento/alta coesão;
- qual trecho do UML demonstra sua aplicação.

---

# 10. Etapa 6 — API pública e implementação interna

Somente depois de definir a arquitetura será decidida a fronteira entre `api` e `core`.

## API pública

Deve conter somente aquilo que um desenvolvedor precisa conhecer para utilizar ou estender o framework.

Pergunta-guia:

> "Um desenvolvedor criando um novo jogo precisa conhecer este contrato?"

Se sim, ele pode fazer parte da API pública.

## Core

Deve conter detalhes de implementação que não precisam ser conhecidos pelos jogos concretos.

Pergunta-guia:

> "Este elemento pode mudar internamente sem exigir alteração nos jogos clientes?"

Se sim, ele provavelmente deve permanecer interno.

Não colocar classes em `api` apenas porque a Trinca precisa delas.

---

# 11. Etapa 7 — Implementação do framework

Depois do contrato arquitetural estar definido e acordado:

- implementar as abstrações;
- implementar os pontos de extensão;
- implementar as relações entre os componentes;
- manter clientes separados;
- garantir encapsulamento;
- tratar exceções;
- documentar a API pública;
- criar testes automatizados.

O framework não deve conter regras específicas da Trinca ou Blackjack.

---

# 12. Etapa 8 — Validação com a Trinca

Somente depois que o framework estiver funcional, implementar a Trinca como cliente.

A Trinca deverá:

- depender da API pública;
- implementar suas regras específicas;
- utilizar os pontos de extensão;
- não importar classes internas do `core`;
- não modificar o framework para resolver regras específicas do jogo.

As regras fornecidas pela equipe para a Trinca serão utilizadas nesta etapa como especificação da aplicação cliente.

## Teste arquitetural

Durante a implementação da Trinca, qualquer dificuldade deve ser classificada:

### Lacuna genérica

A capacidade é útil para diferentes jogos.

→ avaliar alteração no framework.

### Necessidade específica

A capacidade existe apenas por causa das regras da Trinca.

→ manter na aplicação cliente.

Esse processo evita transformar o framework em uma implementação específica da Trinca.

---

# 13. Etapa 9 — Blackjack

Depois da Trinca, implementar Blackjack utilizando a mesma API.

Objetivo:

```text
Framework
   ├── Trinca
   └── Blackjack
```

A implementação do segundo jogo deve demonstrar que o framework suporta comportamentos suficientemente diferentes sem precisar receber lógica específica de Blackjack.

Se for necessário alterar repetidamente o framework para criar o segundo jogo, a arquitetura deverá ser revisada.

---

# 14. Etapa 10 — Testes

Os testes deverão existir em dois níveis:

## Framework

Testar as responsabilidades e comportamentos gerais da infraestrutura.

## Aplicações clientes

Testar a integração entre o jogo concreto e o framework.

A Trinca deve possuir testes que demonstrem, entre outros aspectos:

- início da partida;
- execução de turnos;
- aplicação das regras;
- vitória/empate;
- tratamento de jogadas inválidas.

Os testes devem demonstrar que a aplicação consegue utilizar o framework sem acessar seus detalhes internos.

---

# 15. Etapa 11 — UML

O UML final deve representar a arquitetura efetivamente construída.

Deve apresentar:

- classes;
- interfaces;
- herança;
- implementação;
- associação;
- composição/agregação;
- multiplicidades.

O diagrama deve destacar, quando possível:

- componentes reutilizáveis;
- pontos de extensão;
- relações entre API e implementação;
- aplicação dos padrões.

Não finalizar o UML antes do congelamento da arquitetura.

---

# 16. Etapa 12 — Relatório

O relatório deve consolidar:

1. descrição geral da arquitetura;
2. responsabilidades das principais classes;
3. relacionamentos;
4. decisões de herança e composição;
5. baixo acoplamento;
6. alta coesão;
7. GRASP;
8. SOLID;
9. padrões GoF;
10. pontos de extensão;
11. componentes reutilizáveis;
12. separação entre framework e aplicações;
13. justificativa das decisões.

O relatório deve explicar não apenas "o que foi feito", mas principalmente **por que a arquitetura foi construída dessa maneira**.

---

# 17. Critério principal de sucesso

A arquitetura será considerada adequada quando for possível demonstrar:

```text
                 FRAMEWORK
                     │
          ┌──────────┴──────────┐
          │                     │
       TRINCA              BLACKJACK
          │                     │
          └────── usam a mesma ─┘
                 API pública
```

Sem que o framework precise conhecer regras específicas de nenhum dos dois jogos.

---

# 18. Estado inicial da documentação

## Decisões já estabelecidas pelo enunciado

- Framework reutilizável para jogos de cartas.
- Pelo menos cinco pontos de extensão.
- Separação entre framework e aplicações clientes.
- Interfaces e classes abstratas.
- Testes automatizados.
- API pública.
- Javadoc.
- Pelo menos quatro padrões GoF.
- Justificativas de GRASP e SOLID.
- UML.
- Pelo menos uma aplicação cliente.
- Demonstração de outro jogo utilizando a mesma biblioteca.

## Decisões que ainda precisam ser tomadas pela equipe

- responsabilidades exatas de cada abstração;
- quais são os pontos de extensão definitivos;
- quais contratos pertencem à API pública;
- quais implementações permanecem no `core`;
- padrões GoF efetivamente utilizados;
- relacionamentos e multiplicidades;
- fluxo geral da partida;
- estratégia de eventos;
- estratégia de decisão dos jogadores;
- estrutura final dos pacotes.

---

# 19. Regra para as próximas decisões

Toda decisão arquitetural deverá ser avaliada nesta ordem:

1. **É necessária para o domínio geral de jogos de cartas?**
2. **É reutilizável por diferentes jogos?**
3. **É uma variação que precisa ser extensível?**
4. **Qual responsabilidade deve possuir essa decisão?**
5. **Qual contrato precisa ser público?**
6. **O que pode permanecer interno?**
7. **Qual padrão, se algum, resolve o problema?**
8. **Como a decisão será demonstrada no UML e no relatório?**

Somente depois disso a decisão deve ser implementada.

---

# 20. Próximo passo

**Não implementar código ainda.**

O próximo passo é construir a primeira versão do **modelo conceitual do framework**, começando pelas seis abstrações mínimas:

```text
Carta
Baralho
Jogador
MaoDeCartas
RegraDoJogo
Partida
```

Para cada uma, definir:

- responsabilidade;
- dados;
- operações;
- relacionamentos;
- o que ela não deve conhecer.

Depois disso, identificaremos os comportamentos que variam e, só então, os pontos de extensão.
