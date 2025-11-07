# Loki Query Guide

This guide contains useful curl commands to verify Loki's functionality and query your logs.

## Health Check

Check if Loki is running:
```bash
curl -v http://localhost:3100/ready
```

## Labels

Get all label names that Loki knows about:
```bash
curl -G -s "http://localhost:3100/loki/api/v1/labels"
```

Get all values for a specific label:
```bash
curl -G -s "http://localhost:3100/loki/api/v1/label/level/values"
```

## Querying Logs

### Query logs from the last 5 minutes:

For application logs (from file):
```bash
curl -G -s "http://localhost:3100/loki/api/v1/query_range" \
  --data-urlencode 'query={job="quarkus-file"}' \
  --data-urlencode 'start='$(date -d '5 minutes ago' +%s)000000000 \
  --data-urlencode 'end='$(date +%s)000000000
```

For Docker logs:
```bash
curl -G -s "http://localhost:3100/loki/api/v1/query_range" \
  --data-urlencode 'query={job="docker"}' \
  --data-urlencode 'start='$(date -d '5 minutes ago' +%s)000000000 \
  --data-urlencode 'end='$(date +%s)000000000
```

### Query by log level:

Query ERROR logs:
```bash
curl -G -s "http://localhost:3100/loki/api/v1/query_range" \
  --data-urlencode 'query={level="ERROR"}' \
  --data-urlencode 'start='$(date -d '1 hour ago' +%s)000000000 \
  --data-urlencode 'end='$(date +%s)000000000
```

Query DEBUG logs:
```bash
curl -G -s "http://localhost:3100/loki/api/v1/query_range" \
  --data-urlencode 'query={level="DEBUG"}' \
  --data-urlencode 'start='$(date -d '1 hour ago' +%s)000000000 \
  --data-urlencode 'end='$(date +%s)000000000
```

### Query specific logger:

```bash
curl -G -s "http://localhost:3100/loki/api/v1/query_range" \
  --data-urlencode 'query={logger=~"me.cre.ten.*"}' \
  --data-urlencode 'start='$(date -d '1 hour ago' +%s)000000000 \
  --data-urlencode 'end='$(date +%s)000000000
```

### Complex Queries

Query logs with specific text:
```bash
curl -G -s "http://localhost:3100/loki/api/v1/query_range" \
  --data-urlencode 'query={job="quarkus-file"} |~ "tenant"' \
  --data-urlencode 'start='$(date -d '1 hour ago' +%s)000000000 \
  --data-urlencode 'end='$(date +%s)000000000
```

Count logs by level in 5-minute windows:
```bash
curl -G -s "http://localhost:3100/loki/api/v1/query_range" \
  --data-urlencode 'query=count_over_time({job="quarkus-file"}[5m]) by (level)' \
  --data-urlencode 'start='$(date -d '1 hour ago' +%s)000000000 \
  --data-urlencode 'end='$(date +%s)000000000
```

## Tips

- All timestamps in the queries are in nanoseconds (hence the multiplication by 1000000000)
- The `|~` operator performs regex matching on the log content
- Use `=~` for regex matching on label values
- Use `!=` or `!~` for negative matching

## Pretty Print Results

To get more readable output, pipe the curl results through jq:
```bash
curl ... | jq .
```
