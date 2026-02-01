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

