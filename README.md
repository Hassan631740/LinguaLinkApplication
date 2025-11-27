# LinguaLink - Spring Boot Starter

This generated starter project is configured with:
- Java 21, Spring Boot 3.3.x
- JPA/Hibernate, MySQL
- Flyway DB migrations (see src/main/resources/db/migration)
- Spring Security (minimal stub)
- JJWT dependencies (implement JWT logic in code)

## Quick start
1. Install Java 21 and MySQL.
2. Create database: `CREATE DATABASE lingualink;`
3. Update `src/main/resources/application.yml` if needed.
4. Build: `mvn -DskipTests package`
5. Run: `java -jar target/lingualink-0.0.1-SNAPSHOT.jar`

Flyway runs migrations at startup to create schema and sample data.
