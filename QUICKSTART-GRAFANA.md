# Quick Start Guide - Grafana Integration

## Starting the Stack

1. Start all services:
```bash
docker-compose up -d
```

2. Wait for services to be ready (check with):
```bash
docker-compose ps
```

## Access Points

- **Application**: http://localhost:8080/borges
- **Grafana**: http://localhost:3000 (admin/admin)
- **Loki API**: http://localhost:3100

## First Time Setup

1. Access Grafana at http://localhost:3000
2. Login with admin/admin
3. The Loki data source should be automatically configured
4. Import the pre-configured dashboard or create your own

## Quick Test

1. Generate some logs by calling your application:
```bash
curl http://localhost:8080/borges/hello
```

2. Check Grafana Explore section with query:
```logql
{job="quarkus-file"}
```

## Troubleshooting

If no logs appear:
1. Check Fluentd logs: `docker logs fluentd`
2. Check Loki logs: `docker logs loki`
3. Verify log files exist: `docker exec fluentd ls -la /fluentd/log/`

## Configuration Files

- Grafana datasource: `config/grafana/provisioning/datasources/loki.yml`
- Dashboard config: `config/grafana/provisioning/dashboards/dashboards.yml`
- Sample dashboard: `config/grafana/provisioning/dashboards/quarkus-logs-dashboard.json`
