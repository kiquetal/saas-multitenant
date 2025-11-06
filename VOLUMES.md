# Docker Volumes and Kubernetes Storage Guide

## PostgreSQL Volume Management

### Docker Compose Volume Issue
When using PostgreSQL 18+, the data directory structure has changed to better reflect PostgreSQL's native behavior:

#### Old Structure (PostgreSQL 17 and earlier):
```
/var/lib/postgresql/data/
    ├── base/
    ├── global/
    └── ...
```

#### New Structure (PostgreSQL 18+):
```
/var/lib/postgresql/
    └── 18/
        ├── data/
        ├── base/
        └── ...
```

This change was made to:
1. Better support PostgreSQL upgrades
2. Follow the `pg_ctlcluster` directory structure
3. Enable `pg_upgrade --link` without mount point boundary issues

### Volume Mount Points
- **Incorrect**: Mounting at `/var/lib/postgresql/data`
  ```yaml
  volumes:
    - postgres_data:/var/lib/postgresql/data  # Will fail in PostgreSQL 18+
  ```
- **Correct**: Mounting at `/var/lib/postgresql`
  ```yaml
  volumes:
    - postgres_data:/var/lib/postgresql  # Works with PostgreSQL 18+
  ```

## Kubernetes Deployment Recommendations

### Storage Configuration

1. **StorageClass Selection**
```yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: postgres-sc
parameters:
  type: ssd  # Use SSD for better performance
provisioner: kubernetes.io/aws-ebs  # Example for AWS
reclaimPolicy: Retain  # Important for data persistence
```

2. **PersistentVolumeClaim**
```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: postgres-pvc
spec:
  accessModes:
    - ReadWriteOnce
  storageClassName: postgres-sc
  resources:
    requests:
      storage: 10Gi
```

3. **StatefulSet Example**
```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: postgres
spec:
  serviceName: postgres
  replicas: 1
  template:
    spec:
      containers:
      - name: postgres
        image: postgres:18
        volumeMounts:
        - name: postgres-storage
          mountPath: /var/lib/postgresql  # Correct mount point for PostgreSQL 18+
        env:
        - name: POSTGRES_DB
          valueFrom:
            configMapKeyRef:
              name: postgres-config
              key: POSTGRES_DB
  volumeClaimTemplates:
  - metadata:
      name: postgres-storage
    spec:
      accessModes: [ "ReadWriteOnce" ]
      storageClassName: postgres-sc
      resources:
        requests:
          storage: 10Gi
```

### Best Practices

1. **Data Persistence**
   - Use `StorageClass` with `reclaimPolicy: Retain`
   - Configure regular backups
   - Consider using PostgreSQL streaming replication

2. **Resource Management**
   - Set appropriate resource requests and limits
   - Monitor storage usage
   - Plan for storage scaling

3. **Backup Strategy**
   - Use tools like `pg_dump` for logical backups
   - Consider using volume snapshots
   - Implement point-in-time recovery capability

4. **Security**
   - Use Kubernetes Secrets for credentials
   - Implement network policies
   - Enable PostgreSQL SSL connections

5. **Monitoring**
   - Deploy PostgreSQL exporters for Prometheus
   - Monitor disk usage and performance
   - Set up alerts for storage thresholds

### Production Considerations

1. **High Availability**
   - Use PostgreSQL replication
   - Deploy across multiple availability zones
   - Implement proper failover mechanisms

2. **Scaling**
   - Use horizontal scaling for read replicas
   - Plan for vertical scaling of storage
   - Monitor and adjust based on usage patterns

3. **Maintenance**
   - Plan for version upgrades
   - Schedule regular maintenance windows
   - Implement proper backup verification

4. **Network**
   - Use Services for stable networking
   - Configure proper network policies
   - Consider using Service Mesh for advanced routing

Remember to adjust these recommendations based on your specific requirements and cloud provider capabilities.
