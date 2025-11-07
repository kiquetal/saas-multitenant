# Fluentd Configuration for Loki Integration

This guide shows the current configuration of Fluentd to send Quarkus application logs to Loki (tested with Loki 2.9.x).

## Configuration

```ruby
# File-based logs (from shared volume)
<source>
  @type tail
  path /fluentd/log/app-quarkus.log,/fluentd/log/quarkus.log
  pos_file /tmp/quarkus.log.pos
  tag quarkus.logs
  <parse>
    @type regexp
    expression /^(?<time>\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}:\d{2},\d{3})\s+(?<container>\S+)\s+.*\s+(?<level>[A-Z]+)\s+\[(?<logger>[^\]]+)\]\s+\((?<thread>[^\)]+)\)\s+(?<message>.*)$/
    time_format %Y-%m-%d %H:%M:%S,%L
  </parse>
</source>

# HTTP input for testing
<source>
  @type http
  port 9880
  bind 0.0.0.0
  <parse>
    @type json
  </parse>
</source>

# Transform records before sending to Loki
<filter quarkus.**>
  @type record_transformer
  enable_ruby true
  auto_typecast true
  <record>
    loki_level ${record["level"]}
    loki_container ${record["container"]}
    loki_logger ${record["logger"]}
    loki_thread ${record["thread"]}
  </record>
</filter>

<match quarkus.**>
  @type loki
  url http://loki:3100
  extra_labels {"job": "quarkus-file", "app": "saas-multitenant", "env": "prod"}
  
  <label>
    loki_level
    loki_container
    loki_logger
    loki_thread
  </label>

  remove_keys loki_level,loki_container,loki_logger,loki_thread,level,container,logger,thread

  <buffer>
    @type memory
    chunk_limit_size 1m
    flush_interval 5s
    flush_at_shutdown true
    retry_max_times 5
  </buffer>
</match>

# Fluentd internal logs
<label @FLUENT_LOG>
  <match **>
    @type stdout
  </match>
</label>

# Debug output
<match **>
  @type stdout
</match>
```

## Configuration Explanation

### 1. Log Sources (`<source>`)
- **Main Log Source**:
  - Tails Quarkus application logs
  - Uses regex to parse structured log entries
  - Captures: time, container, level, logger, thread, and message
  - Important: Uses comma (,) in time format for milliseconds

- **HTTP Source**:
  - Additional input for testing
  - Accepts JSON format on port 9880

### 2. Record Transformation (`<filter>`)
- Creates Loki-specific fields with `loki_` prefix
- Transforms original log fields for Loki compatibility
- Uses Ruby processing for field extraction

### 3. Loki Output (`<match>`)
- Sends logs to Loki at http://loki:3100
- Static labels:
  - job: "quarkus-file"
  - app: "saas-multitenant"
  - env: "prod"
- Dynamic labels from transformed fields:
  - loki_level
  - loki_container
  - loki_logger
  - loki_thread
- Removes processed fields to avoid duplication
- Buffer configuration for reliability:
  - Memory-based buffering
  - 1MB chunks
  - 5-second flush interval
  - Shutdown flush enabled
  - 5 retry attempts

### 4. Debug Output
- Fluentd internal logs to stdout
- Catch-all matcher for debugging

```ruby
<source>
  @type tail
  path /fluentd/log/app-quarkus.log,/fluentd/log/quarkus.log
  pos_file /tmp/quarkus.log.pos
  tag quarkus
  <parse>
    @type regexp
    expression /^(?<time>\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}:\d{2},\d{3})\s+(?<container>\S+)\s+.*\s+(?<level>[A-Z]+)\s+\[(?<logger>[^\]]+)\]\s+\((?<thread>[^\)]+)\)\s+(?<message>.*)$/
    time_format %Y-%m-%d %H:%M:%S,%L
  </parse>
</source>
```

Key points:
- Uses `tail` input plugin to follow log files
- Regular expression captures important fields:
  - `time`: Timestamp with milliseconds
  - `container`: Container ID/hostname
  - `level`: Log level (INFO, ERROR, etc.)
  - `logger`: Logger name
  - `thread`: Thread name
  - `message`: Actual log message

### 2. Transform Records
Next, we prepare the records by adding Loki-specific fields:

```ruby
<filter quarkus.**>
  @type record_transformer
  enable_ruby true
  auto_typecast true
  <record>
    loki_level ${record["level"]}
    loki_container ${record["container"]}
    loki_logger ${record["logger"]}
    loki_thread ${record["thread"]}
  </record>
</filter>
```

Key points:
- Creates new fields with `loki_` prefix for clarity
- Uses record transformer to extract values from the original record
- Enables Ruby processing for field access
- Enables auto typecasting for proper value conversion

### 3. Send to Loki
Finally, we configure the Loki output:

```ruby
<match quarkus.**>
  @type loki
  url http://loki:3100
  extra_labels {"job": "quarkus-file", "app": "saas-multitenant", "env": "prod"}
  
  <buffer>
    @type memory
    chunk_limit_size 1m
    flush_interval 5s
    flush_at_shutdown true
    retry_max_times 5
  </buffer>
</match>
```

Key points:
- Uses `extra_labels` for static label configuration
- Configures buffering for reliable delivery
- Sets retry parameters for resilience
- Uses the transformed `loki_` prefixed fields automatically

The following configuration successfully sends logs to Loki due to several key components working together:

### 1. Source Configuration
```ruby
<source>
  @type tail
  path /fluentd/log/app-quarkus.log,/fluentd/log/quarkus.log
  pos_file /tmp/quarkus.log.pos
  tag quarkus.logs
  <parse>
    @type regexp
    expression /^(?<time>\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}:\d{2},\d{3})\s+(?<container>\S+)\s+.*\s+(?<level>[A-Z]+)\s+\[(?<logger>[^\]]+)\]\s+\((?<thread>[^\)]+)\)\s+(?<message>.*)$/
    time_format %Y-%m-%d %H:%M:%S,%L
  </parse>
</source>
```

Key points:
- Correct time format with comma (`,`) for milliseconds
- Proper regex capturing of all necessary fields
- Unique tag for routing (`quarkus.logs`)

### 2. Loki Output Configuration - Important Label Handling

```ruby
<match quarkus.**>
  @type loki
  url http://loki:3100
  # Static labels in extra_labels
  extra_labels {"job": "quarkus-file", "app": "saas-multitenant", "env": "prod"}
  
  # Dynamic record fields to be used as labels
  label_keys level,container,logger,thread
  
  remove_keys level,container,logger,thread

  <buffer>
    @type memory
    chunk_limit_size 1m
    flush_interval 5s
    flush_at_shutdown true
    retry_max_times 5
  </buffer>
</match>
```

Critical components:
1. **Label Definition**: All labels (static and dynamic) must be defined in `extra_labels`
   - Static values: directly specified (`"job": "quarkus-file"`)
   - Dynamic values: using record interpolation (`"level": "${record['level']}"`)
2. **Remove Keys**: Prevents duplicate data in log messages
3. **Buffer Configuration**: Ensures reliable delivery to Loki

### Why Labels Matter in Loki

1. **Label Visibility**:
   - Only labels defined in `extra_labels` appear in Loki's label index
   - Labels defined in the `<label>` section are treated as log content, not actual Loki labels
   - This is why running `curl "http://localhost:3100/loki/api/v1/labels"` only shows static labels

2. **Querying Implications**:
   - Labels are indexed and efficiently queryable
   - Non-label fields require full text search (slower)
   - Proper label configuration is crucial for performance

### Common Pitfalls Avoided

1. **Label Definition Method**:
   - ❌ Using `<label>` section alone doesn't create queryable Loki labels
   - ✅ Using `extra_labels` properly registers labels with Loki

2. **Dynamic Values**:
   - ❌ Direct record access doesn't work in `extra_labels`
   - ✅ Use `${record['field']}` syntax for dynamic values

3. **Time Parsing**:
   - Correct millisecond format (,%L) matches Quarkus logs
   - Proper time parsing is crucial for log ordering

4. **Configuration Order**:
   - Loki match before catch-all stdout match
   - Prevents logs from being intercepted before reaching Loki

## Troubleshooting Common Issues

### 1. Label Visibility
If only static labels are visible:
```bash
curl -G "http://localhost:3100/loki/api/v1/labels" | jq
# Shows only: ["app", "env", "job"]
```
Solution:
- Ensure the record transformer is creating proper JSON
- Verify the parser is correctly processing the JSON
- Check that label_keys includes all desired fields

### 2. Invalid Time Format
If you see "invalid time format" errors:
- Verify the time_format matches your log format exactly
- For Quarkus logs, use: `%Y-%m-%d %H:%M:%S,%L`
- The comma (,) is important for millisecond parsing

### 3. Missing Labels Error
If you see "at least one label pair is required":
- Ensure label_keys is properly configured
- Verify the record transformer is creating the fields
- Check that the fields exist in the record

## Verifying the Setup

### 1. Check Fluentd Processing
Success indicators in Fluentd logs:
```
sending X bytes to loki
POST request was responded to with status code 204
```

### 2. Query Labels
```bash
# List all labels
curl -G "http://localhost:3100/loki/api/v1/labels" | jq

# Check specific label values
curl -G "http://localhost:3100/loki/api/v1/label/level/values" | jq
curl -G "http://localhost:3100/loki/api/v1/label/logger/values" | jq
```

## Querying Logs

### Basic Queries

Get all logs:
```bash
curl -G -s "http://localhost:3100/loki/api/v1/query_range" \
  --data-urlencode 'query={job="quarkus-file"}' \
  --data-urlencode "start=$(date -d '1 hour ago' +%s)000000000" \
  --data-urlencode "end=$(date +%s)000000000" | jq
```

Filter by level:
```bash
curl -G -s "http://localhost:3100/loki/api/v1/query_range" \
  --data-urlencode 'query={job="quarkus-file",level="INFO"}' \
  --data-urlencode "start=$(date -d '1 hour ago' +%s)000000000" \
  --data-urlencode "end=$(date +%s)000000000" | jq
```

### Advanced Queries

Search for specific text:
```bash
curl -G -s "http://localhost:3100/loki/api/v1/query_range" \
  --data-urlencode 'query={job="quarkus-file"} |~ "tenant"' \
  --data-urlencode "start=$(date -d '1 hour ago' +%s)000000000" \
  --data-urlencode "end=$(date +%s)000000000" | jq
```

Count by log level:
```bash
curl -G -s "http://localhost:3100/loki/api/v1/query_range" \
  --data-urlencode 'query=count_over_time({job="quarkus-file"}[5m]) by (level)' \
  --data-urlencode "start=$(date -d '1 hour ago' +%s)000000000" \
  --data-urlencode "end=$(date +%s)000000000" | jq
```

## Verification

Check available labels:
```bash
curl -G "http://localhost:3100/loki/api/v1/labels" | jq
```

Check values for a specific label:
```bash
curl -G "http://localhost:3100/loki/api/v1/label/level/values" | jq
```

## Debugging Tips

1. Check Fluentd logs for successful sending:
   - Look for "sending X bytes to loki"
   - Status code 204 indicates success

2. Verify label presence:
   - All streams must have at least one label
   - Check available labels using the labels API

3. Time range issues:
   - Ensure query time range includes when logs were sent
   - Use nanosecond precision (multiply by 1000000000)
