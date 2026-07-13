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