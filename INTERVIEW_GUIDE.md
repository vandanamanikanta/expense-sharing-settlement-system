# Interview Guide — Expense Sharing & Settlement System

This guide helps you explain this project confidently in interviews. Every answer uses simple language and gives a concrete example from this project.

---

## Table of Contents

1. [Project Explanations (60s and 2min)](#1-project-explanations)
2. [Your Role](#2-your-role)
3. [Architecture Questions](#3-architecture-questions)
4. [Database & Entity Questions](#4-database--entity-questions)
5. [API & REST Questions](#5-api--rest-questions)
6. [Business Logic Questions](#6-business-logic-questions)
7. [Spring Boot & Core Concepts](#7-spring-boot--core-concepts)
8. [Exception Handling & Validation](#8-exception-handling--validation)
9. [Testing](#9-testing)
10. [Challenges & Future Improvements](#10-challenges--future-improvements)

---

## 1. Project Explanations

### 60-Second Explanation

> "I built a backend REST API for splitting group expenses. Think of it like Splitwise — you create a group, add friends, then when someone pays a bill, the system automatically splits it equally and tracks who owes whom. It also lets members record settlements when they pay each other back. I used Java 17, Spring Boot, Spring Data JPA with Hibernate, and MySQL. All money calculations use BigDecimal for precision. I tested the split and balance logic with JUnit and Mockito."

### 2-Minute Explanation

> "This is an Expense Sharing & Settlement System. The problem it solves is: when a group of friends go out and one person pays the bill, how do we track who owes what?
>
> **Example:** Alice, Bob, and Charlie go for dinner. Alice pays Rs 900. The system splits it equally — Rs 300 each. So Alice is owed Rs 600 (she paid 900 but her share was only 300), while Bob and Charlie each owe Rs 300.
>
> **Architecture:** I followed a layered architecture — Controller handles HTTP requests, Service has the business logic, Repository talks to MySQL through JPA. I used DTOs to keep the API contract separate from the database entities.
>
> **Key features:** User CRUD, group management with members, adding expenses with automatic equal splitting, balance calculation, and settlement recording. If an amount doesn't divide evenly (like Rs 100 among 3 people), I distribute the extra paise to the first participants so the total always adds up exactly.
>
> **Tech stack:** Java 17, Spring Boot 3, Spring Web, Spring Data JPA, Hibernate, MySQL, Maven, Bean Validation. I handled errors with a global exception handler using @RestControllerAdvice and wrote unit tests for the split and balance logic."

---

## 2. Your Role

**Q: What was your role in this project?**

> "I was the sole backend developer. I designed the database schema, wrote all the REST APIs, implemented the business logic for expense splitting and balance calculation, set up exception handling and validation, and wrote unit tests."

---

## 3. Architecture Questions

**Q: What architecture did you follow?**

> "I followed a 3-layer architecture:
> - **Controller** — receives HTTP requests and sends responses. No business logic here.
> - **Service** — contains all business logic like splitting expenses and calculating balances.
> - **Repository** — interfaces that Spring Data JPA implements to run database queries.
>
> Example flow: When a user hits `POST /api/groups/1/expenses`, the `ExpenseController` receives the request, calls `ExpenseService.createExpense()`, which validates the data and calls `ExpenseRepository.save()` to store it in MySQL."

**Q: Why did you use this layered approach?**

> "Separation of concerns. If I want to change how balances are calculated, I only touch the Service layer. If I switch from MySQL to PostgreSQL, only the configuration changes. The Controller doesn't care about database details, and the Repository doesn't know about HTTP."

**Q: What are DTOs and why did you use them?**

> "DTO stands for Data Transfer Object. It's a simple Java class used to send/receive data through the API.
>
> Example: My `User` entity has fields that map to the database. But I don't want the client to directly see or control everything in the entity. So I created `UserRequestDto` (what the client sends — just name and email) and `UserResponseDto` (what the client receives — id, name, email). If I later add a password field to the User entity, it won't accidentally leak through the API."

---

## 4. Database & Entity Questions

**Q: What tables does your project have?**

> "Six tables:
> - `users` — stores user info (name, email)
> - `expense_groups` — stores group info (I avoided the name `groups` because GROUP is a reserved word in MySQL)
> - `group_members` — a join table for the many-to-many relationship between users and groups
> - `expenses` — stores who paid how much for what
> - `expense_splits` — stores each person's share in an expense
> - `settlements` — records when someone pays back what they owe"

**Q: Explain @ManyToOne with an example from your project.**

> "`@ManyToOne` means many rows in one table point to one row in another table.
>
> Example: Many expenses can belong to one group. In the `Expense` entity, I have:
> ```java
> @ManyToOne
> @JoinColumn(name = "group_id")
> private ExpenseGroup group;
> ```
> This creates a `group_id` foreign key column in the `expenses` table."

**Q: Explain @OneToMany with an example.**

> "`@OneToMany` is the other side. One expense has many splits:
> ```java
> @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL)
> private List<ExpenseSplit> splits;
> ```
> `mappedBy = "expense"` means the `ExpenseSplit` entity owns the relationship (it has the foreign key). `CascadeType.ALL` means when I save an Expense, its splits are automatically saved too."

**Q: Explain the @ManyToMany in your project.**

> "Users and Groups have a many-to-many relationship — a user can be in many groups, and a group can have many users. JPA handles this with a join table:
> ```java
> @ManyToMany
> @JoinTable(name = "group_members",
>     joinColumns = @JoinColumn(name = "group_id"),
>     inverseJoinColumns = @JoinColumn(name = "user_id"))
> private Set<User> members;
> ```
> This automatically creates and manages the `group_members` table with two foreign key columns."

**Q: Why did you use BigDecimal instead of double for money?**

> "`double` has floating-point precision issues. For example, `0.1 + 0.2` in double gives `0.30000000000000004`, not `0.3`. For money, even a tiny error is unacceptable. `BigDecimal` stores numbers exactly. In my project, when I split Rs 100 among 3 people, I get exactly `33.34 + 33.33 + 33.33 = 100.00`, not `99.99999...`."

---

## 5. API & REST Questions

**Q: What is REST?**

> "REST (Representational State Transfer) is a way to design web APIs. Key rules:
> - Use URLs to represent resources (`/api/users`, `/api/groups`)
> - Use HTTP methods for actions: GET (read), POST (create), PUT (update), DELETE (remove)
> - The server is stateless — each request is independent
>
> Example: `GET /api/users/1` means 'give me user with ID 1'. `POST /api/users` with a JSON body means 'create a new user'."

**Q: What HTTP status codes did you use?**

> - **200 OK** — successful read or update
> - **201 Created** — something was created (new user, expense, settlement)
> - **204 No Content** — successful delete (nothing to return)
> - **400 Bad Request** — validation failed or business rule violated
> - **404 Not Found** — user/group/expense doesn't exist
> - **409 Conflict** — duplicate email"

**Q: How did you design your API URLs?**

> "I followed REST conventions and used nested resources where it makes sense:
> - `/api/users` — top-level resource
> - `/api/groups/{groupId}/expenses` — expenses belong to a group, so they're nested under it
> - `/api/groups/{groupId}/balances` — balances are calculated per group
>
> This makes the URLs self-explanatory — you can read them and understand the relationship."

---

## 6. Business Logic Questions

**Q: Explain your equal split logic.**

> "When someone adds an expense:
> 1. I divide the total amount by the number of participants using `BigDecimal.divide()` with `RoundingMode.DOWN` and scale 2.
> 2. I multiply the base share back by the number of participants to find the total.
> 3. Any leftover (remainder) is distributed as 1 paisa extra to the first participants.
>
> Example: Rs 100 ÷ 3 = Rs 33.33 (base share). Total: 33.33 × 3 = 99.99. Remainder: 0.01. So the first person gets Rs 33.34, and the other two get Rs 33.33. Total: 33.34 + 33.33 + 33.33 = Rs 100.00 exactly."

**Q: How do you calculate balances?**

> "For each member in a group, I calculate:
> ```
> Balance = (amount paid for expenses)
>         - (share owed in expenses)
>         + (settlements paid to others)
>         - (settlements received from others)
> ```
> Positive balance = others owe you money. Negative balance = you owe money.
>
> Example: Alice pays Rs 900 for dinner, split among 3. Alice's balance = 900 (paid) - 300 (her share) = +600. Bob's balance = 0 (paid nothing) - 300 (his share) = -300. When Bob pays Alice Rs 300, Bob's balance becomes -300 + 300 = 0."

**Q: How do settlements affect balances?**

> "When Bob (payer) pays Alice (receiver) Rs 300:
> - Bob's balance goes **up** by 300 (he reduced his debt)
> - Alice's balance goes **down** by 300 (she received what she was owed)
>
> After all settlements, everyone's balance should ideally be 0."

---

## 7. Spring Boot & Core Concepts

**Q: Why did you choose Spring Boot?**

> "Spring Boot makes it easy to build production-ready applications quickly. It provides auto-configuration (I don't have to write XML), an embedded server (no need to deploy a WAR file), and starter dependencies that bundle everything I need. For example, `spring-boot-starter-data-jpa` gives me JPA + Hibernate + connection pooling in one dependency."

**Q: What is Hibernate?**

> "Hibernate is an ORM (Object-Relational Mapping) framework. It maps my Java classes to database tables automatically. Instead of writing SQL like `INSERT INTO users (name, email) VALUES ('Alice', 'alice@example.com')`, I just write `userRepository.save(user)` and Hibernate generates the SQL for me. My `User` entity with `@Entity` and `@Table(name = "users")` tells Hibernate which table it maps to."

**Q: What is Dependency Injection?**

> "Instead of creating objects myself with `new`, Spring creates them and 'injects' them where needed.
>
> Example: My `UserController` needs `UserService`. Instead of writing `new UserService(new UserRepository())`, I use constructor injection:
> ```java
> @RequiredArgsConstructor
> public class UserController {
>     private final UserService userService; // Spring injects this
> }
> ```
> Spring sees that `UserController` needs a `UserService` bean, finds it, and passes it in. This makes the code loosely coupled and easy to test."

**Q: What is @Service?**

> "`@Service` marks a class as a Spring bean in the service layer. Spring creates one instance of it and manages its lifecycle. It's where I put business logic.
>
> Example: `UserService` is annotated with `@Service`. It has methods like `createUser()`, `getUserById()`, etc. Spring creates it at startup, injects the `UserRepository` into it, and injects it into `UserController`."

**Q: What is @Repository?**

> "`@Repository` marks a class as a data access component. In my project, I use Spring Data JPA interfaces:
> ```java
> @Repository
> public interface UserRepository extends JpaRepository<User, Long> {
>     boolean existsByEmail(String email);
> }
> ```
> I just declare the interface — Spring Data JPA provides the implementation automatically. The method name `existsByEmail` is parsed by Spring to generate the SQL query `SELECT EXISTS(... WHERE email = ?)`."

**Q: What is @RestController?**

> "`@RestController` = `@Controller` + `@ResponseBody`. It marks a class as a REST API controller. Every method returns data (JSON) directly, not a view/HTML page.
>
> Example: My `UserController` has methods annotated with `@GetMapping`, `@PostMapping`, etc. Each method returns a DTO object, and Spring automatically converts it to JSON."

---

## 8. Exception Handling & Validation

**Q: How did you handle exceptions?**

> "I created custom exceptions like `UserNotFoundException`, `GroupNotFoundException`, and `ExpenseNotFoundException`. Then I created a `GlobalExceptionHandler` class annotated with `@RestControllerAdvice`. It catches exceptions from anywhere in the application and returns a consistent JSON error response:
> ```json
> {
>   "message": "User not found with id: 99",
>   "status": 404
> }
> ```
> This avoids try-catch blocks everywhere and gives the client a clean error format."

**Q: What is @RestControllerAdvice?**

> "It's a global exception handler for all REST controllers. Instead of handling errors in every controller method, I handle them in one place. Spring intercepts any exception thrown during a request and checks if my handler has a matching `@ExceptionHandler` method."

**Q: How did you handle validation?**

> "I used Bean Validation annotations on my DTOs:
> - `@NotBlank` — field can't be empty (used for name, description)
> - `@Email` — must be a valid email format
> - `@DecimalMin("0.01")` — amount must be positive
> - `@NotNull` — field can't be null
>
> In the controller, I add `@Valid` before `@RequestBody`. If validation fails, Spring throws `MethodArgumentNotValidException`, which my global handler catches and returns a 400 response with details about which fields failed."

---

## 9. Testing

**Q: What did you test?**

> "I wrote unit tests for the two most important business logic pieces:
>
> 1. **Split calculation** — tested that Rs 900 ÷ 3 = Rs 300 each, and that Rs 100 ÷ 3 = 33.34 + 33.33 + 33.33 (totals add up exactly).
> 2. **Balance calculation** — tested that after a Rs 900 expense, Alice = +600, Bob = -300, Charlie = -300. And after Bob settles Rs 300, Bob = 0, Alice = +300."

**Q: How did you test without a database?**

> "I used Mockito to mock the repository layer. Instead of connecting to a real MySQL database, I told the mock to return specific data:
> ```java
> when(expenseRepository.findByGroupId(1L)).thenReturn(List.of(expense));
> ```
> This makes tests fast (no database needed) and focused on testing only the business logic."

---

## 10. Challenges & Future Improvements

**Q: What challenges did you face?**

> 1. **Uneven split precision** — Rs 100 ÷ 3 = 33.333... I solved this by rounding down to 33.33, calculating the remainder (0.01), and giving the extra paisa to the first participant.
> 2. **GROUP is a reserved word in MySQL** — I named the table `expense_groups` instead.
> 3. **Bidirectional relationships** — I had to be careful with `@ManyToMany` to avoid infinite loops in JSON serialization. Using DTOs solved this — I convert entities to DTOs before sending them as JSON.

**Q: What improvements would you add in the future?**

> 1. **Percentage-based and custom splits** — not just equal splitting
> 2. **Authentication & authorization** — Spring Security with JWT, so users log in
> 3. **Expense categories** — food, travel, shopping, etc.
> 4. **Settlement suggestions** — automatically suggest minimum transactions to settle all debts (e.g., "Bob pays Alice Rs 300" instead of listing all debts)
> 5. **Pagination** — for groups with many expenses
> 6. **Soft deletes** — mark records as deleted instead of removing them
