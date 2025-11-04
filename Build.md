# Build and Deployment Guide

This document provides instructions on how to build, test, and deploy the Quarkus application using Docker and GitHub Actions.

## Building the Application

Before building the container image, you need to package the application using Maven.

```bash
./mvnw package
```

This command compiles the code, runs the tests, and creates a JAR file in the `target/` directory.

## GitHub Actions

You can automate the build and containerization process using GitHub Actions. Below is an example workflow that builds the application and the Docker image on every push to the `main` branch.

Create a file named `.github/workflows/build.yml` with the following content:

```yaml
name: Java CI with Maven

on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v4
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'redhat'
        cache: maven

    - name: Build with Maven
      run: ./mvnw package -DskipTests

    - name: Build the Docker image
      run: docker build -f src/main/docker/Dockerfile.jvm -t quarkus/saas-multitenant-jvm .

    # Optional: Push to a container registry like GitHub Container Registry
    # - name: Log in to GitHub Container Registry
    #   uses: docker/login-action@v3
    #   with:
    #     registry: ghcr.io
    #     username: ${{ github.actor }}
    #     password: ${{ secrets.GITHUB_TOKEN }}

    # - name: Push the Docker image
    #   run: |
    #     docker tag quarkus/saas-multitenant-jvm ghcr.io/${{ github.repository }}:latest
    #     docker push ghcr.io/${{ github.repository }}:latest
```

## Running the Container

Once the image is built, you can run it using the following command:

```bash
docker run -i --rm -p 8080:8080 quarkus/saas-multitenant-jvm
```

## Logging

The application logs to standard output by default. To persist logs, you can configure Quarkus to write to a file and mount a volume from the host.

1.  **Configure file logging:**

    Add the following lines to `src/main/resources/application.properties`:

    ```properties
    quarkus.log.file.path=/deployments/logs/app.log
    quarkus.log.file.enable=true
    ```

    After adding this, you need to rebuild the application with `./mvnw package`.

2.  **Mount a log directory:**

    Create a directory on your host machine to store the logs and run the container with a volume mount.

    ```bash
    mkdir -p logs
    docker run -d --rm -p 8080:8080 \
      -v $(pwd)/logs:/deployments/logs \
      --name my-quarkus-app \
      quarkus/saas-multitenant-jvm
    ```

    The application logs will now be available in the `logs/` directory on your host.

## Permissions

The Docker container runs with user ID `185`. When you mount a host directory as a volume, the files created by the container (like log files) will be owned by user `185`. This can lead to permission issues if you try to access these files with your regular user account on the host.

### Solution

The recommended way to handle this is to change the ownership of the host directory to match the user in the container before running it.

1.  **Create the log directory on the host:**
    ```bash
    mkdir -p logs
    ```

2.  **Change the ownership of the directory to user `185`:**
    ```bash
    sudo chown 185:185 logs
    ```

3.  **Run the container with the volume mount:**
    ```bash
    docker run -d --rm -p 8080:8080 \
      -v $(pwd)/logs:/deployments/logs \
      --name my-quarkus-app \
      quarkus/saas-multitenant-jvm
    ```

Now, the container can write to the mounted directory, and you can still read the files (though you might need `sudo` to modify or delete them, depending on your system's permissions).

## Kubernetes Logging

When deploying to Kubernetes, you should use a `PersistentVolume` (PV) and `PersistentVolumeClaim` (PVC) to manage log persistence. This is a more robust and flexible approach than host-path mounting.

The application configuration for file logging in `application.properties` remains the same:

```properties
quarkus.log.file.path=/deployments/logs/app.log
quarkus.log.file.enable=true
```

### 1. Create a PersistentVolumeClaim (PVC)

First, create a PVC to request storage from your cluster. Create a file named `pvc.yaml` with the following content:

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: quarkus-logs-pvc
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 1Gi
```

Apply this to your cluster:

```bash
kubectl apply -f pvc.yaml
```

### 2. Mount the PVC in your Deployment

Next, modify your Kubernetes `Deployment` manifest to mount the PVC into your application pod. You also need to set the `securityContext` to ensure the pod has the correct permissions to write to the volume.

Here is an example of a `deployment.yaml` file:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: quarkus-app
spec:
  replicas: 1
  selector:
    matchLabels:
      app: quarkus
  template:
    metadata:
      labels:
        app: quarkus
    spec:
      securityContext:
        runAsUser: 185
        fsGroup: 185
      containers:
      - name: quarkus-app
        image: quarkus/saas-multitenant-jvm # Replace with your image name
        ports:
        - containerPort: 8080
        volumeMounts:
        - name: quarkus-logs
          mountPath: /deployments/logs
      volumes:
      - name: quarkus-logs
        persistentVolumeClaim:
          claimName: quarkus-logs-pvc
```

In this example:

*   `securityContext` sets the user and group ID to `185` to match the user in the Docker container. This ensures the pod has permission to write to the mounted volume.
*   A volume named `quarkus-logs` is defined, which uses the `quarkus-logs-pvc` you created earlier.
*   This volume is mounted into the container at `/deployments/logs`, which is the same path you configured in `application.properties`.

Apply the deployment to your cluster:

```bash
kubectl apply -f deployment.yaml
```

Your application's logs will now be stored in the `PersistentVolume` and will persist across pod restarts.

