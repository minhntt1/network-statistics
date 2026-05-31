# Deployment Guide

## Prerequisites

- **Java 21** (JDK)
- **MySQL 8+** (two databases: `network_statistics` and `quartz`)
- **Gradle** (or use the bundled `gradlew` wrapper)
- **Git** (for version control)

---

## Local Development Setup

### 1. Clone and Build

```bash
git clone <repo-url>
cd network-statistics
./gradlew clean build -x test
```

### 2. Create Databases

```sql
CREATE DATABASE network_statistics;
CREATE DATABASE quartz;
```

### 3. Configure Database Connection

Edit the appropriate properties file for your profile (e.g., `src/main/resources/application-dev-executor.properties`):

```properties
spring.datasource.quartz-scheduler.jdbcUrl=jdbc:mysql://localhost:3306/quartz?connectionTimeZone=UTC
spring.datasource.quartz-scheduler.username=root
spring.datasource.quartz-scheduler.password=yourpassword

spring.datasource.statistic-db.jdbcUrl=jdbc:mysql://localhost:3306/network_statistics?connectionTimeZone=UTC
spring.datasource.statistic-db.username=root
spring.datasource.statistic-db.password=yourpassword
```

### 4. Create Staging/Archive Tables

Run the SQL template from the [Database Schema](database-schema.md) to create required tables for each device type.

### 5. Run Locally

**Executor** (runs polling jobs):
```bash
java -Dspring.profiles.active=dev-executor -jar ./build/libs/network-statistic-0.0.1-SNAPSHOT.jar
```

**Scheduler** (runs Quartz to trigger jobs):
```bash
java -Dspring.profiles.active=dev-scheduler -jar ./build/libs/network-statistic-0.0.1-SNAPSHOT.jar
```

**Admin** (web UI for managing jobs and viewing data):
```bash
java -Dspring.profiles.active=dev-admin -jar ./build/libs/network-statistic-0.0.1-SNAPSHOT.jar
```

---

## Container Deployment

### 1. Build the Docker Image

To build the JAR (without tests):
```bash
./gradlew clean build -x test
```

Then build the Docker image using a Dockerfile:
```dockerfile
FROM openjdk:21-jdk-slim
COPY build/libs/network-statistic-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
docker build -t network-statistics:latest .
```

### 2. Run in Container

Use container hostnames for database connections (configured in prd-* profiles):

```bash
# Executor
docker run -d \
  -e SPRING_PROFILES_ACTIVE=prd-executor \
  -v /path/to/logs:/logging \
  --name netstat-executor \
  network-statistics:latest

# Scheduler
docker run -d \
  -e SPRING_PROFILES_ACTIVE=prd-scheduler \
  --name netstat-scheduler \
  network-statistics:latest

# Admin
docker run -d \
  -e SPRING_PROFILES_ACTIVE=prd-admin \
  -p 8080:8080 \
  --name netstat-admin \
  network-statistics:latest
```

### 3. Container Networking

All container instances must be able to reach the MySQL database. Use Docker networking:

```bash
docker network create netstat-net
# Add containers to the network
```

---

## Production Deployment Workflow

### Development Flow

1. **Create a feature branch** from `main`
2. **Implement changes** following the [Development Procedures](../readme.md#development-procedures-for-new-device-modelsversions)
3. **Test locally** using a dev database (`dev-*` profiles)
4. **Run unit tests**: `./gradlew test`

### Staging / Dev Environment

1. Deploy to a dev environment that mirrors production
2. Use `prd-*` profiles with dev infrastructure
3. Monitor logs and data outputs for correctness

### Production Deployment

1. After dev validation passes, deploy to production
2. Monitor initial outputs post-deployment
3. Verify data ingestion, normalization, and visualization

### Rollback

1. Revert to previous JAR version
2. Restart the application with the previous version
3. No data migration needed (schema changes are additive)

### Monitoring

- **Prometheus metrics** exposed at `/actuator/prometheus` (executor/admin profiles)
- **Health checks** at `/actuator/health`
- **Log files** in `/logging/` directory (configured in properties)
- **Grafana dashboards** for data visualization

---

## Common Commands

```bash
# Run unit tests
./gradlew test

# Build without tests
./gradlew clean build -x test

# Run from command line (any profile)
java -Dspring.profiles.active=<profile> -jar build/libs/network-statistic-0.0.1-SNAPSHOT.jar

# Run with reduced memory (256MB)
java -Xmx256m -Dspring.profiles.active=prd-executor -jar network-statistic-0.0.1-SNAPSHOT.jar

# Run in IntelliJ
./gradlew bootRun
```
