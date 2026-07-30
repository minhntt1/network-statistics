FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S appgroup && adduser -S -G appgroup appuser

WORKDIR /app

COPY build/libs/network-statistic-0.0.1-SNAPSHOT.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]