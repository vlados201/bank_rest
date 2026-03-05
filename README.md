# Система управления банковскими картами

---

## Инструкция запуска
1. Если БД запускается из Docker то:

 - В docker-compose.yml в environments указать данные для подключения к бд

Пример
```text
services:
  postgres:
    environment:
      POSTGRES_DB: bankrestservice
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: 123
```
 - docker-compose up --build

2. В application.yml в datasource указать данные для подключения к бд

Пример
```text
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bankrestservice
    username: postgres
    password: 123
```
3. mvn spring-boot:run

Swagger: http://localhost:8080/swagger-ui.html

### Данные для входа под учеткой админа: 
admin

222333

---

## Технологический стек

- Java 17
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Liquibase
- Docker / Docker Compose
- Swagger / OpenAPI
- JUnit 5 + Mockito
- Maven

---

## Аутентификация и авторизация

Используется **JWT-аутентификация**.

Роли пользователей:

- **ADMIN**
- **USER**

### ADMIN может

- создавать карты
- активировать / блокировать карты
- удалять карты
- управлять пользователями
- просматривать все карты в системе

### USER может

- просматривать свои карты
- фильтровать и получать карты с пагинацией
- смотреть баланс карты
- переводить деньги между своими картами
- отправлять запрос на блокировку карты

---

## Атрибуты банковской карты

Каждая карта содержит:

| Поле          | Описание |
|---------------|-----|
| id            | идентификатор |
| encryptedNumber | зашифрованный номер карты |
| maskedNumber  | маскированный номер (**** **** **** 1234) |
| owner         | владелец |
| expiryDate    | срок действия |
| status        | статус карты |
| balance       | баланс |

---

# Статусы карты

```text
ACTIVE — карта активна  
BLOCK_REQUESTED — пользователь запросил блокировку  
BLOCKED — карта заблокирована администратором  
EXPIRED — срок действия карты истёк
```

---

## Безопасность

В системе реализованы следующие механизмы безопасности:

- JWT аутентификация
- Role-based access control
- Шифрование номера карты (AES)
- Маскирование номера карты
- Валидация входных данных
- Ограничение доступа к API

---

## API Возможности
Карты
- создание карты 
- изменение статуса карты
- удаление карты
- просмотр карт
- фильтрация
- пагинация

Переводы
- перевод средств между своими картами

Пользователи
- регистрация
- управление пользователями

---

## Пагинация

Пример параметров

page=0

size=10

sort=id,desc

Пример ответов
```text
{
  "content": [],
  "totalElements": 15,
  "totalPages": 2,
  "size": 10,
  "number": 0
}
```

---

## Шифрование данных
Номер карты:
- хранится в зашифрованном виде
- шифрование: AES/GCM
- отображается только маска

Пример отображения:

```text
**** **** **** 1234
```
---

## Работа c БД
- PostgreSQL
- Spring Data JPA
- Liquibase для миграций

---

## Swagger документация

http://localhost:8080/swagger-ui.html

OpenAPI спецификация и дополнительные описания API в docs/openapi.yaml

---

## Тестирование 
Используются:
- JUnit 5
- Mockito
- AssertJ

Покрываются:
- CardService
- TransferService
- UserService
- CryptoService
