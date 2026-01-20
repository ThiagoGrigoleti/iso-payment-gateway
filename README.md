ISO 8583 Payment Gateway

Este projeto implementa um Gateway de Pagamentos de alta performance projetado para realizar a interoperabilidade entre sistemas modernos (API REST/JSON) e sistemas bancários legados baseados no protocolo ISO 8583 (TCP/Binário).

O sistema gerencia o ciclo de vida transacional, incluindo conversão de mensagens, persistência de dados, comunicação assíncrona via sockets e tratamento de concorrência.

Visão Geral do Projeto

O objetivo principal é demonstrar competências em engenharia de software backend para sistemas críticos, abordando:

* Protocolos de Baixo Nível: Implementação de cliente TCP para comunicação direta via Socket, sem uso de camadas HTTP para a transação bancária.
* Manipulação de Dados Binários: Conversão e empacotamento de mensagens no padrão ISO 8583 (Bitmaps e Data Elements).
* Concorrência e IO: Gerenciamento de conexões e leitura de streams de dados.
* Integridade de Dados: Persistência transacional utilizando PostgreSQL com rastreabilidade de requisição e resposta.

Arquitetura

O fluxo de dados segue o padrão:

1. Client Application: Envia requisição HTTP POST (JSON).
2. Payment Controller: Recebe e valida o payload.
3. Service Layer: Converte o DTO para entidade de banco de dados (estado PENDENTE) e transforma os dados para objeto ISO 8583.
4. TCP Client: Estabelece conexão com o servidor bancário (Mock) e trafega os bytes.
5. Database: Atualiza o estado da transação com a resposta do banco (APROVADO/RECUSADO) e armazena os metadados.

Stack Tecnológico

Linguagem: Java 17
Framework: Spring Boot 3 (Web, Data JPA)
Protocolo Bancário: j8583 (ISO 8583 Parser/Builder)
Banco de Dados: PostgreSQL 15
Containerização: Docker & Docker Compose
Testes: JUnit 5 & Mockito
Documentação: Swagger UI (OpenAPI)

Pré-requisitos

Java JDK 17+
Docker e Docker Compose instalados
Maven (opcional, wrapper incluído)

Instalação e Execução

1. Inicialização do Ambiente (Banco de Dados)
Execute o comando abaixo na raiz do projeto para subir o container do PostgreSQL:
docker-compose up -d
2. Execução da Aplicação
Utilize o Maven Wrapper para iniciar a aplicação. Este comando irá compilar o projeto, rodar os testes unitários e iniciar o servidor na porta 8080.
./mvnw spring-boot:run

Nota: A aplicação iniciará simultaneamente um Mock Server TCP na porta 9999 para simular a instituição financeira.

3. Documentação da API
Após a inicialização, a documentação Swagger estará disponível em:
http://localhost:8080/swagger-ui/index.html

Exemplo de Uso

Para realizar uma transação de teste, utilize o seguinte comando cURL:

curl -X POST http://localhost:8080/api/payments -H "Content-Type: application/json" -d "{ "cardNumber": "4758123456789010", "amount": 150.00 }"

Resposta Esperada:
Aprovado: 00