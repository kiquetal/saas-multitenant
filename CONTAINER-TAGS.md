# Container Image Tags and Usage Guide

## Available Image Tags

The CI/CD pipeline creates multiple tags for easy identification and deployment:

### Tag Format Examples (for November 6, 2025):
- `ghcr.io/your-username/saas-multitenant:latest` - Latest build from main branch
- `ghcr.io/your-username/saas-multitenant:20251106` - Date-based tag (YYYYMMDD)
- `ghcr.io/your-username/saas-multitenant:20251106-143022` - Date-time tag (YYYYMMDD-HHMMSS)
- `ghcr.io/your-username/saas-multitenant:20251106-a1b2c3d4` - Date + short SHA
- `ghcr.io/your-username/saas-multitenant:main-20251106` - Branch + date

### Platform Support
All images are built for multiple architectures:
- `linux/amd64` (Intel/AMD 64-bit)
- `linux/arm64` (ARM 64-bit, Apple Silicon, AWS Graviton)

## Usage Examples

### Docker Compose with Date-based Tag
```yaml
services:
  app:
    image: ghcr.io/your-username/saas-multitenant:20251106
    container_name: saas-multitenant-app
    ports:
      - "8080:8080"
    environment:
      - QUARKUS_PROFILE=prod
    depends_on:
      - db

  db:
    image: postgres:18
    container_name: saas-multitenant-db
    environment:
      - POSTGRES_DB=quarkus
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
    volumes:
      - postgres_data:/var/lib/postgresql

volumes:
  postgres_data:
```

### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: saas-multitenant
spec:
  replicas: 3
  selector:
    matchLabels:
      app: saas-multitenant
  template:
    metadata:
      labels:
        app: saas-multitenant
    spec:
      containers:
      - name: app
        image: ghcr.io/your-username/saas-multitenant:20251106-143022
        ports:
        - containerPort: 8080
        env:
        - name: QUARKUS_PROFILE
          value: "prod"
```

### Docker Run Command
```bash
# Run with specific date tag
docker run -p 8080:8080 \
  -e QUARKUS_PROFILE=prod \
  ghcr.io/your-username/saas-multitenant:20251106

# Run with latest
docker run -p 8080:8080 \
  -e QUARKUS_PROFILE=prod \
  ghcr.io/your-username/saas-multitenant:latest
```

## Tag Selection Strategy

### Production Deployments
- Use **date-time tags** for production: `20251106-143022`
- These provide exact build identification and rollback capability

### Development/Testing
- Use **date tags** for daily builds: `20251106`
- Use **latest** for continuous integration

### Rollback Strategy
- Keep multiple date-based tags for easy rollback
- Use branch-date tags for feature branch deployments

## Build Information

Each image contains metadata labels:
- `build.date` - Build timestamp
- `build.platforms` - Supported architectures
- `org.opencontainers.image.*` - Standard OCI labels

View image metadata:
```bash
docker inspect ghcr.io/your-username/saas-multitenant:20251106
```

## Best Practices

1. **Always pin to specific tags in production**
   ```yaml
   # Good
   image: ghcr.io/your-username/saas-multitenant:20251106-143022
   
   # Avoid in production
   image: ghcr.io/your-username/saas-multitenant:latest
   ```

2. **Use date tags for scheduled deployments**
   ```bash
   # Deploy today's build
   DEPLOY_TAG=$(date +%Y%m%d)
   docker-compose -f docker-compose.prod.yml up -d
   ```

3. **Implement blue-green deployments with tagged images**
   ```bash
   # Current production
   docker service update --image ghcr.io/your-username/saas-multitenant:20251106-143022 app
   ```

4. **Monitor image sizes and vulnerabilities**
   ```bash
   # Check image size
   docker images ghcr.io/your-username/saas-multitenant
   
   # Scan for vulnerabilities (if using Docker Scout)
   docker scout cves ghcr.io/your-username/saas-multitenant:20251106
   ```
