ARG DOCKER_REGISTRY=docker.m.daocloud.io/library
FROM ${DOCKER_REGISTRY}/maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml ./
COPY .mvn ./.mvn

RUN mvn -B -ntp dependency:go-offline -DskipTests

COPY src ./src

RUN mvn -B -ntp clean package -DskipTests

FROM ${DOCKER_REGISTRY}/eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl

COPY --from=builder /app/target/city-activity-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
