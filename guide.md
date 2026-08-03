A dependency is an external library that your project uses. 
Without dependencies, Java only provides the standard JDK classes.

This is called a starter because it pulls in many related libraries transitively.

Without spring-boot-starter-web, annotations like:

@RestController
@RequestMapping
@GetMapping
@PostMapping
@RequestBody

won't even compile.










Controller → Handles HTTP requests and responses.
Service → Contains business rules and application logic.
Repository → Encapsulates database access through Spring Data JPA.
Model → Represents JPA entities mapped to database tables.
DTO → Defines API request and response payloads, decoupling the API from persistence.
Exception → Centralizes custom exceptions and integrates with global exception handling.
Config → Holds application configuration such as security, beans, and OpenAPI setup.


Keeping these responsibilities separate makes the codebase easier to navigate, test, and extend.





What is Spring Security?
Think of spring-boot-starter-web as building the house (REST APIs).
Think of spring-boot-starter-security as installing the locks, doors, and security system.






JPA (Jakarta Persistence API) is the Java standard for working with relational databases using:
Java objects instead of writing SQL everywhere.

INSERT INTO users(name, email)
VALUES('Abhay', 'abhay@example.com');

We, only write:
User user = new User();
user.setName("Abhay");
user.setEmail("abhay@example.com");
userRepository.save(user);

Hibernate (the default JPA implementation in Spring Boot) converts that into SQL automatically.





What is Spring Boot Actuator?
Think of Actuator as the health monitor for your application.





We make BaseResponse generic because different APIs return different types of data. Some endpoints return a UserDto, some return a WalletDto, some return a list of objects, and some return just a string or no data at all. Using a generic type parameter (<T>) allows us to keep one reusable response wrapper while preserving compile-time type safety and avoiding unsafe casting with Object.





Annotation	Purpose
@Entity	Makes this a JPA entity.
@Table	Creates the users table and enforces unique constraints.
@Id	Primary key.
@GeneratedValue	Auto-increment ID in MySQL.
@Column	Defines column constraints like nullable, length, and unique.
@Enumerated(EnumType.STRING)	Stores enum values as readable strings (CUSTOMER) instead of integers.
@PrePersist	Automatically sets createdAt before the entity is first saved.





Flyway is a database migration tool that manages schema changes using versioned SQL scripts. Instead of letting Hibernate modify the database automatically, every schema change is written as a migration (such as V1__create_users_table.sql), committed to Git, and applied consistently across development, staging, and production. Hibernate is typically configured with ddl-auto=validate so it verifies the schema without changing it. This makes database evolution predictable, reviewable, and safe.


In production, we separate these responsibilities:
Flyway → Creates and modifies the database schema.
Hibernate → Maps Java objects to database tables and validates that the schema matches.


Application Starts
        │
        ▼
Datasource connects
        │
        ▼
Flyway starts
        │
        ▼
Reads flyway_schema_history
        │
        ▼
Pending migrations?
        │
   ┌────┴────┐
   │         │
  Yes       No
   │         │
Run SQL    Skip
   │         │
   └────┬────┘
        ▼
Hibernate validates schema
        ▼
Application starts



Once a migration has been applied to a shared environment (or production), treat it as immutable. Never edit it. Create a new migration instead.



JWT: Header: Metadata: HMAC SHA-256
     Payload: Claims: Username, expiry and all data
     Signature: Created from Header + payload + secret key
     

JWTs are signed but not encrypted by default. Anyone possessing the token can decode its payload, so only non-sensitive claims like the username, user ID, or roles should be stored. Passwords and other secrets must never be included.







We don't use a permanent JWT because JWT is stateless and difficult to revoke. If compromised, a long-lived JWT gives an attacker access until expiry. We also don't use refresh tokens for every API call because refresh tokens are high-value credentials that require server-side validation and increase latency. Instead, we use short-lived JWT access tokens for fast stateless authorization and long-lived refresh tokens for session management. Refresh tokens are stored server-side, rotated, and revoked on logout. The exact expiry depends on the business risk: fintech applications prioritize security with shorter access token lifetimes, while lower-risk applications may choose longer sessions for better UX