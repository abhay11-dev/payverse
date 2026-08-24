FROM maven:3.10.1-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY pom.xml ./
COPY notification-service/pom.xml ./notification-service/pom.xml
COPY notification-service/src ./notification-service/src
RUN mvn -pl notification-service -am -DskipTests package

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=builder /workspace/notification-service/target/*-SNAPSHOT.jar /app/app.jar
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]
