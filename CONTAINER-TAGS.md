# Container Tags & Usage

## Tag Examples (Nov 6, 2025)
- `ghcr.io/username/saas-multitenant:latest`
- `ghcr.io/username/saas-multitenant:20251106` 
- `ghcr.io/username/saas-multitenant:20251106-143022`
- `ghcr.io/username/saas-multitenant:20251106-a1b2c3d4`

**Platforms:** linux/amd64, linux/arm64

## Quick Usage

### Docker Compose
```yaml
services:
  app:
    image: ghcr.io/username/saas-multitenant:20251106
    ports: ["8080:8080"]
    environment: [QUARKUS_PROFILE=prod]
  db:
    image: postgres:18
    environment: [POSTGRES_DB=quarkus, POSTGRES_USER=postgres, POSTGRES_PASSWORD=postgres]
    volumes: [postgres_data:/var/lib/postgresql]
volumes:
  postgres_data:
```

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: saas-multitenant
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: app
        image: ghcr.io/username/saas-multitenant:20251106-143022
        ports: [containerPort: 8080]
        env: [name: QUARKUS_PROFILE, value: prod]
```

### Docker Run
```bash
docker run -p 8080:8080 -e QUARKUS_PROFILE=prod ghcr.io/username/saas-multitenant:20251106
```

## Best Practices
- **Production:** Use date-time tags (`20251106-143022`)
- **Development:** Use date tags (`20251106`) or `latest`
- **Rollback:** Keep multiple tagged versions
- **View metadata:** `docker inspect <image>:tag`
