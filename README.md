# Card Limit & Invoice

API de controle de limite e fatura de cartão de crédito com Redis para controle em tempo real.

## Stack
- Java 21 + Spring Boot 3.3
- PostgreSQL (histórico transacional e faturas)
- Redis (limite disponível em tempo real)
- Flyway (migrations)
- OpenAPI/Swagger

## Funcionalidades
- Consulta de limite disponível (Redis)
- Registro de compra com decremento de limite
- Estorno de compra
- Pagamento de fatura (total ou parcial)
- Geração automática de fatura mensal (@Scheduled)
- Histórico de lançamentos

## Como rodar

### Pré-requisitos
```bash
docker-compose up -d postgres redis
```

### Rodando a aplicação
```bash
./mvnw spring-boot:run
```

### Endpoints
Acesse a documentação em: http://localhost:8082/swagger-ui.html

## Fluxo de uma compra
```
POST /api/card/{cardId}/purchase
        ↓
   Verifica limite disponível no Redis
        ↓
   Se aprovado: decrementa Redis + persiste no PostgreSQL
        ↓
   Se negado: retorna LIMITE_INSUFICIENTE
```

## Consistência Redis x PostgreSQL
O limite no Redis é a fonte de verdade para tempo real.
Em caso de falha do Redis, um job de reconciliação recalcula
o limite a partir dos lançamentos do PostgreSQL.

## Estrutura do projeto
```
src/main/java/com/bank/cardlimitinvoice/
├── controller/
├── service/
├── domain/
│   ├── entity/
│   └── enums/
├── repository/
├── scheduler/
├── dto/
└── exception/
```
