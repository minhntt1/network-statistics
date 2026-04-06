# Description
A project used to ingest and analyze data from internal network devices, featuring clients and access points' traffic, client disconnect/connect events and router's reboot events.

![screenshot](docs/dashboard.png)

## Tech stack
- Backend: Spring framework, Spring JDBC Template, Quartz Scheduler, SNMP4J, Swagger
- Frontend: Grafana
- DB: MySQL

## Project structure
![](docs/package-structure.png)

## High level design
![](docs/HLD.png)

# Profile info
- dev-executor: used to run executor instances outside container environment with direct IP (ex: 192.168.100.1)  
- dev-scheduler: used to run scheduler instances outside container environment with direct IP (ex: 192.168.100.1)
- prd-executor: used to run executor instances inside container environment using container hostname (ex: mysql)
- prd-scheduler:  used to run scheduler instances inside container environment using container hostname (ex: mysql)

# Create archive, staging, ingestion table
```sql
CREATE TABLE `<data>_stg` (
                           `id` int NOT NULL AUTO_INCREMENT,
                           `poll_time` datetime NOT NULL,
                           `raw_data` json DEFAULT NULL COMMENT 'use json to handle schema change',
                           PRIMARY KEY (`id`,`poll_time`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
create table <data>_archive like <data>_stg;
create table <data>_stg_ingest like <data>_stg;
alter table <data>_archive partition by range (year(poll_time))
(
    partition p2025 values less than(2025) engine = innodb,
    partition p2026 values less than(2026) engine = innodb,
    partition p2027 values less than(2027) engine = innodb,
    partition p2028 values less than(2028) engine = innodb,
    partition p2029 values less than(2029) engine = innodb,
    partition p2030 values less than(2030) engine = innodb,
    partition p9999 values less than(9999) engine = innodb
);
```

# Common tasks
Run unit tests
```
gradle test
```

Run spring application from IntellJ
```
gradle bootRun
```

Build and skip test
```
gradle clean build -x test
```

# Run on dev pofile - local
Run executor  from java command line
```
java '-Dspring.profiles.active=dev-executor' -jar ./build/libs/network-statistic-0.0.1-SNAPSHOT.jar
```

Run dev scheduler from java cmd
```
java '-Dspring.profiles.active=dev-scheduler' -jar ./build/libs/network-statistic-0.0.1-SNAPSHOT.jar
```

# Run inside container
Run executor  from java command line
```
java '-Dspring.profiles.active=prd-executor' -jar ./build/libs/network-statistic-0.0.1-SNAPSHOT.jar
```

Run scheduler from java cmd
```
java '-Dspring.profiles.active=prd-scheduler' -jar ./build/libs/network-statistic-0.0.1-SNAPSHOT.jar
```

Run executor  from bash shell
```
java -Xmx256m -Dspring.profiles.active=prd-executor -jar network-statistic-0.0.1-SNAPSHOT.jar
```

Run executor  from bash shell
```
java -Xmx256m -Dspring.profiles.active=prd-scheduler -jar network-statistic-0.0.1-SNAPSHOT.jar
```

