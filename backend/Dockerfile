FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S laboriq && adduser -S laboriq -G laboriq

COPY --from=build /workspace/target/laboriq-0.0.1-SNAPSHOT.jar /app/laboriq.jar

USER laboriq

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/laboriq.jar"]
