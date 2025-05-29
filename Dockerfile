FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN
COPY . .
RUN GITHUB_ACTOR=${GITHUB_ACTOR} GITHUB_TOKEN=${GITHUB_TOKEN} ./mvnw clean install -Dmaven.test.skip=true

FROM eclipse-temurin:21-jdk
COPY --from=builder /app/target/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]