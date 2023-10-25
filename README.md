# T2-Modulith

This project is currently **under active development**.

The T2-Modulith is an implementation of the T2-Project as a monolith, with the goal, to keep the modularity. It uses [Spring Modulith](https://spring.io/projects/spring-modulith) to verify the modular arrangements.

**To-dos:**
- move UI component into modulith
- replace saga pattern with a single transaction

## Build & Run

```sh
./mvnw clean install -DskipTests
docker build -t t2project/modulith:main .
docker compose up
```
