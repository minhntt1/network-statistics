# Configuration Reference

## Spring Profiles

The application uses 6 Spring profiles that combine two dimensions:
- **Environment**: `dev` (local/direct IP) or `prd` (container/hostname)
- **Role**: `executor` (polling), `scheduler` (task orchestration), `admin` (web UI)

| Profile | Environment | Role | Web App Type | Purpose |
|---|---|---|---|---|
| `dev-executor` | Local | Executor | Servlet (Tomcat) | Run polling jobs outside container |
| `dev-scheduler` | Local | Scheduler | None (non-web) | Run Quartz scheduler outside container |
| `dev-admin` | Local | Admin | Servlet (Tomcat) | Run admin web UI outside container |
| `prd-executor` | Container | Executor | Servlet (Tomcat) | Run polling jobs inside Docker |
| `prd-scheduler` | Container | Scheduler | None (non-web) | Run Quartz scheduler inside Docker |
| `prd-admin` | Container | Admin | Servlet (Tomcat) | Run admin web UI inside Docker |

### Profile Activation

```bash
# Executor (local)
java -Dspring.profiles.active=dev-executor -jar app.jar

# Scheduler (container)
java -Dspring.profiles.active=prd-scheduler -jar app.jar
```

---

## Key Application Properties

### Database Configuration

**Quartz Scheduler Datasource** (`spring.datasource.quartz-scheduler.*`):

| Property | Description | Example Value |
|---|---|---|
| `jdbcUrl` | JDBC URL for Quartz DB | `jdbc:mysql://host:3306/quartz?connectionTimeZone=UTC` |
| `username` | DB username | `dev` |
| `password` | DB password | `abc123` |
| `autoCommit` | Auto-commit mode | `false` |
| `connectionTimeout` | Connection timeout (ms) | `120000` |
| `transactionIsolation` | TX isolation level | `2` (READ_COMMITTED) |
| `maximumPoolSize` | Max pool size | `20` |

**Application Statistic Datasource** (`spring.datasource.statistic-db.*`):

| Property | Description | Example Value |
|---|---|---|
| `jdbcUrl` | JDBC URL for app DB | `jdbc:mysql://host:3306/network_statistics?connectionTimeZone=UTC` |
| `username` | DB username | `dev` |
| `password` | DB password | `abc123` |
| `autoCommit` | Auto-commit mode | `true` (needed for ETL) |
| `connectionTimeout` | Connection timeout (ms) | `120000` |
| `maximumPoolSize` | Max pool size | `14` (matching job count) |
| `defaultFetchSize` | Streaming fetch size | `-2147483648` (MySQL streaming) |

### HikariCP Optimizations

Both datasources use HikariCP with MySQL-optimized settings:
```properties
spring.datasource.*.dataSourceProperties.cachePrepStmts=true
spring.datasource.*.dataSourceProperties.prepStmtCacheSize=250
spring.datasource.*.dataSourceProperties.prepStmtCacheSqlLimit=2048
spring.datasource.*.dataSourceProperties.useServerPrepStmts=true
spring.datasource.*.dataSourceProperties.useLocalSessionState=true
spring.datasource.*.dataSourceProperties.rewriteBatchedStatements=true
spring.datasource.*.dataSourceProperties.cacheResultSetMetadata=true
spring.datasource.*.dataSourceProperties.cacheServerConfiguration=true
spring.datasource.*.dataSourceProperties.elideSetAutoCommits=true
spring.datasource.*.dataSourceProperties.maintainTimeStats=false
```

### JPA / Hibernate

```properties
spring.jpa.statistic-db.hibernate.hbm2ddl.auto=none
spring.jpa.statistic-db.hibernate.connection.driver_class=com.mysql.cj.jdbc.Driver
spring.jpa.statistic-db.hibernate.physical_naming_strategy=org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy
```

### Logging

| Property | Description | Example |
|---|---|---|
| `logging.file.name` | Log file path (executor) | `/logging/network-statistics-log1/executor.log` |
| `logging.pattern.file` | Log pattern | `%d{...} %-5level [%thread] %logger - %msg%n` |
| `logging.logback.rollingpolicy.max-file-size` | Max log file size | `10MB` |
| `logging.logback.rollingpolicy.max-history` | Days to keep logs | `1` |
| `logging.logback.rollingpolicy.clean-history-on-start` | Clean on startup | `true` |

### Monitoring (Actuator / Micrometer / Prometheus)

```properties
management.endpoints.web.exposure.include=prometheus,health,metrics
management.metrics.tags.application=network-statistics-executor
management.metrics.export.prometheus.enabled=true
```

### Threading

```properties
spring.threads.virtual.enabled=true  # Enable Java 21 virtual threads
```

---

## Quartz Scheduler Configuration

Two sets of Quartz properties files exist under `src/main/resources/quartz/`:

### `quartz-polldata.properties`

Used by `QuartzSchedulerPollDataConfig`. Configures the polling scheduler for data ingestion jobs.

Key settings:
- `org.quartz.scheduler.instanceName` — Scheduler name for poll-data
- `org.quartz.threadPool.class` — `com.home.network.statistic.common.config.quartz.AppQuartzThreadPool`
- `org.quartz.jobStore.class` — JDBC job store for persistence
- `org.quartz.jobStore.dataSource` — Reference to Quartz datasource

### `quartz-etl.properties`

Used by `QuartzSchedulerETLConfig`. Configures the ETL scheduler for normalization jobs.

Same structure as poll-data properties, different `instanceName`.

### Custom Thread Pool

`AppQuartzThreadPool` implements Quartz's `ThreadPool` interface with:
- Fixed pool of 14 threads (shared across both schedulers)
- Backed by Java's `Executors.newFixedThreadPool(14)`
- Capacity tuned for: 14 jobs + 3 poll + 3 ETL concurrent operations

### Scheduler Data Sources

Two `QuartzDatasourceConfig` beans:
- `quartzSchedulerPollData` → Uses `quartz-polldata.properties`
- `quartzSchedulerETL` → Uses `quartz-etl.properties`

Both store job/trigger data in the shared `quartz` database.
