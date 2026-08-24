FROM maven:3.10.1-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY pom.xml ./
COPY wallet-service/pom.xml ./wallet-service/pom.xml
COPY wallet-service/src ./wallet-service/src
RUN mvn -pl wallet-service -am -DskipTests package

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=builder /workspace/wallet-service/target/*-SNAPSHOT.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
