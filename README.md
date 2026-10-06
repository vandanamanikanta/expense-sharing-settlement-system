# Expense Sharing & Settlement System

A backend REST API for splitting group expenses equally among members, tracking balances, and recording settlements. Built with **Java 17** and **Spring Boot 3**.

---

## Features

- **User Management** — Create, read, update, delete users
- **Group Management** — Create groups, add/remove members
- **Expense Tracking** — Add expenses to a group, split equally among participants
- **Smart Splitting** — Handles uneven divisions (e.g., Rs 100 ÷ 3) so all shares add up exactly
- **Balance Calculation** — Shows who owes whom in a group
- **Settlements** — Record payments between members, which update balances

---

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 17 | Programming language |
| Spring Boot 3.3 | Application framework |
| Spring Web | REST API |
| Spring Data JPA | Database access layer |
| Hibernate | ORM (Object-Relational Mapping) |
| MySQL | Relational database |
| Maven | Build tool and dependency management |
| Bean Validation | Input validation (@NotBlank, @Email, etc.) |
| Lombok | Reduces boilerplate (getters, setters, constructors) |
| JUnit 5 + Mockito | Unit testing |

---

## Architecture

```
Controller  →  Service  →  Repository  →  MySQL
(REST API)    (Business     (Database
               Logic)        Queries)
```

### Package Structure

```
com.example.expensesharing
├── controller/      # REST endpoints — receives HTTP requests, returns responses
├── service/         # Business logic — splitting, balances, validations
├── repository/      # Data access — JPA interfaces for database operations
├── entity/          # JPA entities — maps Java objects to database tables
├── dto/             # Data Transfer Objects — request/response formats for API
├── exception/       # Custom exceptions and global error handler
└── config/          # Configuration — sample data initializer
```

---

## Database Design

```
┌──────────┐       ┌───────────────┐       ┌──────────┐
│  users   │──M:N──│ group_members │──M:N──│ expense_ │
│          │       │  (join table) │       │  groups  │
└──────────┘       └───────────────┘       └──────────┘
     │                                          │
     │ 1:M                                 1:M  │
     ▼                                          ▼
┌──────────────┐                         ┌──────────────┐
│   expenses   │──1:M──▶│expense_splits│  │ settlements  │
│  (paidBy)    │        └──────────────┘  │(payer,       │
└──────────────┘                          │ receiver)    │
                                          └──────────────┘
```

### Tables

| Table | Key Columns |
|---|---|
| `users` | id, name, email (unique) |
| `expense_groups` | id, name, created_at |
| `group_members` | group_id, user_id (join table) |
| `expenses` | id, description, amount, paid_by_id, group_id, created_at |
| `expense_splits` | id, expense_id, user_id, share_amount |
| `settlements` | id, group_id, payer_id, receiver_id, amount, settlement_date |

> All money fields use `BigDecimal` (never float/double) for precision.

---

## API Endpoints

### Users

| Method | URL | Description | Status |
|--------|-----|-------------|--------|
| POST | `/api/users` | Create a user | 201 |
| GET | `/api/users` | Get all users | 200 |
| GET | `/api/users/{id}` | Get user by ID | 200 |
| PUT | `/api/users/{id}` | Update a user | 200 |
| DELETE | `/api/users/{id}` | Delete a user | 204 |

### Groups

| Method | URL | Description | Status |
|--------|-----|-------------|--------|
| POST | `/api/groups` | Create a group | 201 |
| GET | `/api/groups` | Get all groups | 200 |
| GET | `/api/groups/{id}` | Get group by ID | 200 |
| POST | `/api/groups/{groupId}/members/{userId}` | Add member | 200 |
| DELETE | `/api/groups/{groupId}/members/{userId}` | Remove member | 200 |

### Expenses

| Method | URL | Description | Status |
|--------|-----|-------------|--------|
| POST | `/api/groups/{groupId}/expenses` | Add expense | 201 |
| GET | `/api/groups/{groupId}/expenses` | List group expenses | 200 |
| GET | `/api/expenses/{expenseId}` | Get expense by ID | 200 |
| DELETE | `/api/expenses/{expenseId}` | Delete expense | 204 |

### Balances & Settlements

| Method | URL | Description | Status |
|--------|-----|-------------|--------|
| GET | `/api/groups/{groupId}/balances` | Get member balances | 200 |
| POST | `/api/groups/{groupId}/settlements` | Record settlement | 201 |
| GET | `/api/groups/{groupId}/settlements` | List settlements | 200 |

---

## MySQL Setup

1. Install MySQL and make sure it is running on port 3306.
2. The application will auto-create the database `expense_sharing` on startup.
3. If your MySQL credentials are different from `root`/`root`, update `src/main/resources/application.properties`:

```properties
spring.datasource.username=your_username
spring.datasource.password=your_password
```

---

## How to Run

```bash
# 1. Clone or download the project
# 2. Open a terminal in the project root directory
# 3. Build and run
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.

On first startup, sample data is loaded automatically:
- Users: Alice, Bob, Charlie
- Group: "Friends Dinner" (all three members)
- Expense: "Dinner" Rs 900 paid by Alice, split equally

---

## Example Requests & Responses (Postman)

### 1. Create a User

**POST** `http://localhost:8080/api/users`

Request Body:
```json
{
  "name": "Dave",
  "email": "dave@example.com"
}
```

Response (201):
```json
{
  "id": 4,
  "name": "Dave",
  "email": "dave@example.com"
}
```

### 2. Create a Group

**POST** `http://localhost:8080/api/groups`

Request Body:
```json
{
  "name": "Road Trip"
}
```

Response (201):
```json
{
  "id": 2,
  "name": "Road Trip",
  "createdAt": "2024-01-15T10:30:00",
  "members": []
}
```

### 3. Add Member to Group

**POST** `http://localhost:8080/api/groups/1/members/1`

No request body needed. Response (200):
```json
{
  "id": 1,
  "name": "Friends Dinner",
  "createdAt": "2024-01-15T10:00:00",
  "members": [
    { "id": 1, "name": "Alice", "email": "alice@example.com" }
  ]
}
```

### 4. Add an Expense

**POST** `http://localhost:8080/api/groups/1/expenses`

Request Body:
```json
{
  "description": "Dinner",
  "amount": 900.00,
  "paidByUserId": 1,
  "participantIds": [1, 2, 3]
}
```

Response (201):
```json
{
  "id": 1,
  "description": "Dinner",
  "amount": 900.00,
  "paidBy": { "id": 1, "name": "Alice", "email": "alice@example.com" },
  "groupId": 1,
  "createdAt": "2024-01-15T11:00:00",
  "splits": [
    { "id": 1, "user": { "id": 1, "name": "Alice", "email": "alice@example.com" }, "shareAmount": 300.00 },
    { "id": 2, "user": { "id": 2, "name": "Bob", "email": "bob@example.com" }, "shareAmount": 300.00 },
    { "id": 3, "user": { "id": 3, "name": "Charlie", "email": "charlie@example.com" }, "shareAmount": 300.00 }
  ]
}
```

### 5. Check Balances

**GET** `http://localhost:8080/api/groups/1/balances`

Response (200):
```json
[
  { "userId": 1, "userName": "Alice", "balance": 600.00 },
  { "userId": 2, "userName": "Bob", "balance": -300.00 },
  { "userId": 3, "userName": "Charlie", "balance": -300.00 }
]
```

> Alice is owed Rs 600 (+600). Bob and Charlie each owe Rs 300 (-300).

### 6. Record a Settlement

**POST** `http://localhost:8080/api/groups/1/settlements`

Request Body:
```json
{
  "payerId": 2,
  "receiverId": 1,
  "amount": 300.00
}
```

Response (201):
```json
{
  "id": 1,
  "groupId": 1,
  "payer": { "id": 2, "name": "Bob", "email": "bob@example.com" },
  "receiver": { "id": 1, "name": "Alice", "email": "alice@example.com" },
  "amount": 300.00,
  "settlementDate": "2024-01-15T12:00:00"
}
```

After this settlement, checking balances again shows Bob at 0.

### 7. Validation Error Example

**POST** `http://localhost:8080/api/users` with empty body `{}`

Response (400):
```json
{
  "message": "Validation failed",
  "status": 400,
  "errors": {
    "name": "Name is required",
    "email": "Email is required"
  }
}
```

### 8. Not Found Error Example

**GET** `http://localhost:8080/api/users/999`

Response (404):
```json
{
  "message": "User not found with id: 999",
  "status": 404
}
```

---

## Running Tests

```bash
mvn test
```

Tests cover:
- Equal split (even: 900 ÷ 3 = 300 each)
- Equal split (uneven: 100 ÷ 3 = 33.34 + 33.33 + 33.33)
- Balance calculation after expenses
- Balance calculation after settlements
- Balance sum-to-zero invariant
- Non-member payer validation

---

## Postman Instructions

1. Open Postman and create a new Collection called "Expense Sharing".
2. Set the base URL to `http://localhost:8080`.
3. For POST/PUT requests, set the `Content-Type` header to `application/json` (Postman does this automatically when you select "raw → JSON" body).
4. Test the APIs in this order:
   - Create users → Create group → Add members → Add expense → Check balances → Record settlement → Check balances again

---

## Project Structure

```
expense-sharing/
├── pom.xml
├── README.md
├── INTERVIEW_GUIDE.md
└── src/
    ├── main/
    │   ├── java/com/example/expensesharing/
    │   │   ├── ExpenseSharingApplication.java
    │   │   ├── config/
    │   │   │   └── DataInitializer.java
    │   │   ├── controller/
    │   │   │   ├── UserController.java
    │   │   │   ├── GroupController.java
    │   │   │   ├── ExpenseController.java
    │   │   │   └── SettlementController.java
    │   │   ├── dto/
    │   │   │   ├── UserRequestDto.java
    │   │   │   ├── UserResponseDto.java
    │   │   │   ├── GroupRequestDto.java
    │   │   │   ├── GroupResponseDto.java
    │   │   │   ├── ExpenseRequestDto.java
    │   │   │   ├── ExpenseResponseDto.java
    │   │   │   ├── SettlementRequestDto.java
    │   │   │   ├── SettlementResponseDto.java
    │   │   │   └── BalanceDto.java
    │   │   ├── entity/
    │   │   │   ├── User.java
    │   │   │   ├── ExpenseGroup.java
    │   │   │   ├── Expense.java
    │   │   │   ├── ExpenseSplit.java
    │   │   │   └── Settlement.java
    │   │   ├── exception/
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   ├── ResourceConflictException.java
    │   │   │   ├── UserNotFoundException.java
    │   │   │   ├── GroupNotFoundException.java
    │   │   │   ├── ExpenseNotFoundException.java
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── repository/
    │   │   │   ├── UserRepository.java
    │   │   │   ├── ExpenseGroupRepository.java
    │   │   │   ├── ExpenseRepository.java
    │   │   │   └── SettlementRepository.java
    │   │   └── service/
    │   │       ├── UserService.java
    │   │       ├── GroupService.java
    │   │       ├── ExpenseService.java
    │   │       ├── SettlementService.java
    │   │       └── BalanceService.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/example/expensesharing/service/
            ├── ExpenseServiceTest.java
            └── BalanceServiceTest.java
```
