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
      run: ./mvnw package

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
