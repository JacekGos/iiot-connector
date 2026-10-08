# Connector Service

A Kotlin service responsible for all industrial protocol connectivity — OPC-UA and MQTT. Collects raw data events from physical sources and publishes them to Kafka for downstream processing by the Core Platform.

---

## Table of Contents

- [Local Development Infrastructure](#local-development-infrastructure)
- [Running the Connector Service Locally](#running-the-connector-service-locally)

---

## Local Development Infrastructure

The local Docker Compose stack (Redpanda, PostgreSQL, TimescaleDB, Mosquitto, Nginx) and its
documentation live in the **core-platform** repo, which owns the PostgreSQL/TimescaleDB usage the
stack serves — `connector-service` only talks to Kafka. See the *Local Development Infrastructure*
section of [`iiot-core-platform`](https://github.com/JacekGos/iiot-core-platform#local-development-infrastructure)
and its `docker/` directory.

Start that stack before running the connector service locally.

---

## Running the Connector Service Locally

With the infrastructure stack running, start the connector service from the project root:

```bash
./gradlew bootRun
```

Or run `ConnectorServiceApplication` directly from IntelliJ.

The service expects the following to be reachable:
- Redpanda at `localhost:19092` (external Kafka port)
- Core Platform at `http://localhost:8081` (for startup config fetch — must be running separately)

Local overrides can be set in `src/main/resources/application-local.yml`.
