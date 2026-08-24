FROM maven:3.10.1-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY pom.xml ./
COPY payment-service/pom.xml ./payment-service/pom.xml
COPY payment-service/src ./payment-service/src
RUN mvn -pl payment-service -am -DskipTests package

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=builder /workspace/payment-service/target/*-SNAPSHOT.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
