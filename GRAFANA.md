# Grafana Configuration for Quarkus Multi-tenant Logs

This guide explains how to use Grafana to visualize and query your Quarkus application logs from Loki.

## Getting Started

### Access Grafana
- URL: http://localhost:3000
- Username: `admin`
- Password: `admin`

### Loki Data Source
The Loki data source is automatically configured and available at startup.

## LogQL Queries for Your Application

Based on your Fluentd configuration, your logs have the following labels:
- `job`: "quarkus-file"
- `app`: "saas-multitenant" 
- `env`: "prod"
- `loki_level`: Log level (INFO, ERROR, etc.)
- `loki_container`: Container ID
- `loki_logger`: Logger name
- `loki_thread`: Thread name

### Basic Queries

#### All Application Logs
```logql
{job="quarkus-file"}
```

#### Filter by Log Level
```logql
{job="quarkus-file", loki_level="INFO"}
```

```logql
{job="quarkus-file", loki_level="ERROR"}
```

#### Filter by Specific Logger
```logql
{job="quarkus-file", loki_logger="me.cre.ten.CurrentTenantResolver"}
```

```logql
{job="quarkus-file", loki_logger="me.cre.GreetingResource"}
```

#### Filter by Thread
```logql
{job="quarkus-file", loki_thread="executor-thread-1"}
```

### Advanced Queries with Text Filtering

#### Search for Tenant-related Logs
```logql
{job="quarkus-file"} |~ "tenant"
```

#### Search for Specific Entity IDs
```logql
{job="quarkus-file"} |~ "ID: [0-9]+"
```

#### Filter Out Debug Logs
```logql
{job="quarkus-file"} != "DEBUG"
```

#### Case-insensitive Search
```logql
{job="quarkus-file"} |~ "(?i)error"
```

### Metrics and Aggregation Queries

#### Count Logs by Level (5-minute windows)
```logql
count_over_time({job="quarkus-file"}[5m]) by (loki_level)
```

#### Rate of Log Messages per Second
```logql
rate({job="quarkus-file"}[1m])
```

#### Error Rate Percentage
```logql
sum(rate({job="quarkus-file", loki_level="ERROR"}[5m])) / 
sum(rate({job="quarkus-file"}[5m])) * 100
```

#### Top 10 Most Active Loggers
```logql
topk(10, count_over_time({job="quarkus-file"}[1h]) by (loki_logger))
```

#### Log Volume by Container
```logql
sum by (loki_container) (count_over_time({job="quarkus-file"}[1h]))
```

### Multi-tenant Specific Queries

#### All Tenant Resolution Activities
```logql
{job="quarkus-file", loki_logger="me.cre.ten.CurrentTenantResolver"}
```

#### Entity Persistence Activities
```logql
{job="quarkus-file"} |~ "Persisted entity"
```

#### Database-related Logs
```logql
{job="quarkus-file"} |~ "(?i)(database|jdbc|postgresql)"
```

### Time-based Queries

#### Logs from Last Hour with Errors
```logql
{job="quarkus-file", loki_level="ERROR"}[1h]
```

#### Recent High-frequency Logs (last 15 minutes)
```logql
{job="quarkus-file"}[15m] | rate > 10
```

### Performance Monitoring Queries

#### Thread Activity Analysis
```logql
sum by (loki_thread) (count_over_time({job="quarkus-file"}[10m]))
```

#### Application Startup Logs
```logql
{job="quarkus-file"} |~ "(?i)(starting|started|initialized)"
```

#### Memory or Performance Issues
```logql
{job="quarkus-file"} |~ "(?i)(memory|performance|slow|timeout)"
```

## Creating Dashboards

### Log Volume Dashboard
1. Create a new dashboard
2. Add a panel with query: `sum(rate({job="quarkus-file"}[1m]))`
3. Set visualization to "Time series"

### Error Rate Dashboard
1. Add panel with query: `sum(rate({job="quarkus-file", loki_level="ERROR"}[5m]))`
2. Set visualization to "Stat" or "Time series"
3. Add threshold alerts

### Log Distribution by Level
1. Add panel with query: `count_over_time({job="quarkus-file"}[5m]) by (loki_level)`
2. Set visualization to "Pie chart" or "Bar chart"

### Live Log Stream
1. Add panel with query: `{job="quarkus-file"}`
2. Set visualization to "Logs"
3. Enable auto-refresh for live monitoring

## Alerting Queries

### High Error Rate Alert
```logql
sum(rate({job="quarkus-file", loki_level="ERROR"}[5m])) > 0.1
```

### No Logs Received Alert
```logql
absent_over_time({job="quarkus-file"}[5m])
```

### Specific Error Pattern Alert
```logql
count_over_time({job="quarkus-file"} |~ "(?i)(exception|failed|timeout)"[5m]) > 5
```

## Tips for Effective Log Analysis

1. **Use Label Filters First**: Always start with label filters for better performance
2. **Combine Multiple Filters**: Use multiple labels to narrow down results
3. **Regular Expressions**: Use `|~` for regex matching in log content
4. **Time Windows**: Use appropriate time windows for aggregation queries
5. **Save Frequent Queries**: Save commonly used queries as dashboard panels

## Troubleshooting

### No Data in Grafana
1. Check if Loki data source is configured correctly
2. Verify Fluentd is sending logs to Loki
3. Check time range in Grafana queries
4. Confirm labels match your Fluentd configuration

### Performance Issues
1. Use more specific label filters
2. Reduce time range for heavy queries
3. Avoid too many regex operations
4. Use aggregation functions for large datasets

## Configuration Files Created

- **Loki Data Source**: `/config/grafana/provisioning/datasources/loki.yml`
- **Dashboard Provider**: `/config/grafana/provisioning/dashboards/dashboards.yml`
- **Docker Compose**: Updated with Grafana service on port 3000
