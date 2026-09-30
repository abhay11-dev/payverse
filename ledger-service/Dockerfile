FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY pom.xml ./
COPY ledger-service/pom.xml ./ledger-service/pom.xml
COPY ledger-service/src ./ledger-service/src
RUN mvn -f ledger-service/pom.xml -Dmaven.test.skip=true compile jar:jar spring-boot:repackage

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=builder /workspace/ledger-service/target/*-SNAPSHOT.jar /app/app.jar
EXPOSE 8084
ENTRYPOINT ["java","-jar","/app/app.jar"]
