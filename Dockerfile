FROM openjdk:17-jdk-slim

RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

COPY build/libs/notes-api-*.jar app.jar

EXPOSE 8080

USER appuser

CMD ["java", "-jar", "app.jar"]
