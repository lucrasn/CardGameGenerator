# Mini Framework para Jogos de Cartas (CardGame Framework)

## 1. Visão Geral do Projeto
Este repositório destina-se à concepção, ao projeto arquitetural e à implementação de uma infraestrutura reutilizável (mini framework) orientada a objetos em Java para a modelagem e execução de variados jogos de cartas (como Blackjack, Uno, Truco e Poker).

O objetivo fundamental da solução é encapsular as invariantes do domínio de jogos de cartas — como o gerenciamento do fluxo da partida, a orquestração de turnos e o ciclo de vida dos componentes de execução — fornecendo pontos de extensão (hot-spots) declarativos para que aplicações clientes possam implementar regras e mecânicas específicas sem modificar a estrutura central do framework.

Proposta desenvolvida para a disciplina de **Métodos Avançados de Programação (MAP)** do curso de Ciência da Computação da Universidade Estadual da Paraíba (UEPB).

## 2. Diretrizes Arquiteturais do Projeto
A modelagem da infraestrutura será orientada pelos seguintes requisitos formais de design orientado a objetos:

* **Inversão de Controle (IoC):** O motor abstrato público do framework reterá o fluxo de execução principal da aplicação, invocando o código específico do jogo por meio de operações protegidas e abstrações públicas.
* **Segregação de Responsabilidades:** Separação estrita entre o código de produção do framework, as abstrações públicas de extensão e as aplicações clientes demonstrativas.
* **Aplicação de Padrões de Projeto:** Emprego sistemático de padrões GoF (*Gang of Four*) e princípios GRASP para garantir alta coesão, baixo acoplamento e estrito cumprimento do Princípio do Aberto/Fechado (*Open-Closed Principle*).
* **Robustez e Qualidade de Código:** Encapsulamento rigoroso das coleções internas de dados, tratamento formal de exceções de domínio e suíte de testes automatizados para validação dos componentes.

## 3. Tecnologias e Ferramentas
* **Linguagem Primária:** Java 26
* **Gerenciador de Build e Dependências:** Apache Maven
* **Suíte de Testes Automatizados:** JUnit 5
* **Controle de Versão:** Git / GitHub

## 4. Próximas Etapas de Desenvolvimento
1. Elaboração do projeto arquitetural e especificação formal dos pontos de extensão (hot-spots) e componentes imutáveis (frozen-spots).
2. Construção do diagrama de classes simplificado e especificação das abstrações públicas (interfaces e classes abstratas).
3. Implementação do motor de execução central (Core) e da API de extensão.
4. Desenvolvimento dos testes unitários e de integração.
5. Construção de aplicações clientes (jogos concretos) para validação do reuso e extensibilidade.

## 5. Licença
Este projeto é distribuído sob a [Licença MIT](LICENSE).
