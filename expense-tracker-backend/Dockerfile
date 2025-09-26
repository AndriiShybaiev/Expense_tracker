# Stage 1: compile Maven + Java 21 -> jar
FROM maven:3.9.9-eclipse-temurin-21 AS build

#passing JWT variables as build args
ARG JWT_SECRET
ARG JWT_EXPIRATION

WORKDIR /app
COPY pom.xml .
COPY src ./src

#creating jwt.properties from variables
RUN echo "jwt.secret=${JWT_SECRET}" > src/main/resources/jwt.properties \
    && echo "jwt.expiration=${JWT_EXPIRATION}" >> src/main/resources/jwt.properties

RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
