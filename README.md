# Connector Service

A Kotlin service responsible for all industrial protocol connectivity — OPC-UA and MQTT. Collects raw data events from physical sources and publishes them to Kafka for downstream processing by the Core Platform.

---

## Table of Contents

- [Local Development Infrastructure](#local-development-infrastructure)
    - [Prerequisites](#prerequisites)
    - [Stack Overview](#stack-overview)
    - [Configuration](#configuration)
    - [Starting the Stack](#starting-the-stack)
    - [Stopping the Stack](#stopping-the-stack)
    - [Resetting Volumes](#resetting-volumes)
    - [Connecting to Services](#connecting-to-services)
- [Running the Connector Service Locally](#running-the-connector-service-locally)

---

## Local Development Infrastructure

The `docker/` directory contains a Docker Compose configuration that runs all required infrastructure components locally. This allows both `connector-service` and `core-platform` to be developed and tested against real dependencies without mocking.

> **Note:** The connector-service itself is **not** part of the Docker Compose stack. You run it locally via Gradle or your IDE while the infrastructure stack is running in the background. This gives you fast hot-reload and full debugger access during development.

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) or Docker Engine + Docker Compose plugin
- Ports `80`, `1883`, `5432`, `5433`, `8080`, `9001`, `9092`, `19092` free on your machine

---

### Stack Overview

| Service            | Image                         | Purpose                                      | Ports                         |
|--------------------|-------------------------------|----------------------------------------------|-------------------------------|
| `redpanda`         | redpandadata/redpanda         | Kafka-compatible event streaming             | `9092` (internal), `19092` (host) |
| `redpanda-console` | redpandadata/console          | Redpanda web UI for topic inspection         | `8080`                        |
| `postgres`         | postgres:16-alpine            | Relational storage — pipeline config, assets | `5432`                        |
| `timescaledb`      | timescale/timescaledb-pg16    | Time-series storage — processed sensor data  | `5433`                        |
| `mosquitto`        | eclipse-mosquitto:2           | MQTT broker for local device simulation      | `1883`, `9001` (WebSocket)    |
| `nginx`            | nginx:alpine                  | Reverse proxy — routes API and WebSocket traffic | `80`                      |

All services run on an isolated Docker network `iiot-net` and communicate with each other by container name.

---

### Configuration

The stack is configured via a `.env` file. Before starting for the first time:

```bash
cp docker/.env.example.example docker/.env.example
```

Then open `docker/.env` and set values. The defaults in `.env.example` are safe for local development.

> `.env` is gitignored and must never be committed. `.env.example` is the committed reference, always keep it up to date when adding new variables.

---

### Starting the Stack

```bash
docker-compose -f docker/docker-compose.yml up -d
```

Check that all containers are healthy:

```bash
docker-compose -f docker/docker-compose.yml ps
```

All services should show `healthy` status. Redpanda takes the longest — allow up to 30 seconds on first start.

Follow logs for the whole stack:

```bash
docker-compose -f docker/docker-compose.yml logs -f
```

Follow logs for a specific service:

```bash
docker-compose -f docker/docker-compose.yml logs -f redpanda
```

---

### Stopping the Stack

Stop containers without removing data:

```bash
docker-compose -f docker/docker-compose.yml down
```

---

### Resetting Volumes

> ⚠️ This permanently deletes all local data — Kafka messages, PostgreSQL tables, TimescaleDB records.

```bash
docker-compose -f docker/docker-compose.yml down -v
```

---

### Connecting to Services

#### Redpanda Console (Kafka UI)

Open in browser: [http://localhost:8080](http://localhost:8080)

Use the Console to browse topics, inspect messages, and monitor consumer group lag.

Produce a test message via CLI:

```bash
docker exec -it redpanda rpk topic produce raw-events --brokers localhost:9092
```

Consume messages from a topic:

```bash
docker exec -it redpanda rpk topic consume raw-events --brokers localhost:9092
```

---

#### PostgreSQL

| Property | Value            |
|----------|------------------|
| Host     | `localhost`      |
| Port     | `5432`           |
| Database | `iiot_config`    |
| Username | value from `.env` `POSTGRES_USER` |
| Password | value from `.env` `POSTGRES_PASSWORD` |

Connect via psql:

```bash
docker exec -it postgres psql -U iiot -d iiot_config
```

---

#### TimescaleDB

| Property | Value               |
|----------|---------------------|
| Host     | `localhost`         |
| Port     | `5433`              |
| Database | `iiot_timeseries`   |
| Username | value from `.env` `TIMESCALEDB_USER` |
| Password | value from `.env` `TIMESCALEDB_PASSWORD` |

Connect via psql:

```bash
docker exec -it timescaledb psql -U iiot -d iiot_timeseries
```

Verify TimescaleDB extension is active:

```sql
SELECT default_version, installed_version FROM pg_available_extensions WHERE name = 'timescaledb';
```

---

#### Mosquitto (MQTT Broker)

| Property | Value       |
|----------|-------------|
| Host     | `localhost` |
| Port     | `1883`      |

Mosquitto runs with anonymous access enabled. This matches the production edge deployment model where the broker is inside the customer's private OT network and not exposed externally. If a customer deployment requires MQTT authentication, configure `password_file` in `docker/config/mosquitto.conf`.

Publish a test message:

```bash
mosquitto_pub -h localhost -p 1883 -t "test/sensor" -m '{"value": 42}'
```

Subscribe to a topic:

```bash
mosquitto_sub -h localhost -p 1883 -t "test/#"
```

Alternatively use [MQTT Explorer](https://mqtt-explorer.com/) — a GUI client useful during development.

---

#### Nginx (Reverse Proxy)

| Path         | Routes to                      |
|--------------|--------------------------------|
| `/api/`      | Core Platform REST API         |
| `/ws/`       | Core Platform WebSocket        |
| `/redpanda/` | Redpanda Console UI            |
| `/health`    | Nginx health check (`ok`)      |

> Core Platform is not part of this stack — Nginx will return `502` for `/api/` and `/ws/` until `core-platform` is started locally.

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