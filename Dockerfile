FROM eclipse-temurin:21-jre-slim

RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

COPY build/libs/network-statistic-0.0.1-SNAPSHOT.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]