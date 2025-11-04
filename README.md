# saas-multitenant

## Objective
This project aims to explore and demonstrate best practices for building a SaaS (Software as a Service) multi-tenant application using Quarkus. It focuses on understanding architectural patterns, data isolation, and common challenges in multi-tenant environments.

## Table of Contents
- [Running the application in dev mode](#running-the-application-in-dev-mode)
- [Packaging and running the application](#packaging-and-running-the-application)
- [Creating a native executable](#creating-a-native-executable)
- [Related Guides](#related-guides)
- [Provided Code](#provided-code)

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/saas-multitenant-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Advanced Native Compilation Considerations

When building native executables, especially in containerized environments, you might need more control over the build process.

### Custom Native Builder Image

By default, Quarkus uses a specific builder image for native compilation when using container-based builds. You can override this to use a custom or different version of the builder image. This is useful if you need specific tools or a different environment for your build.

To do this, you need to enable the container build and specify the image in your `application.properties`:

```properties
# Enable container-based native build
quarkus.native.container-build=true
# Specify the custom builder image
quarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-mandrel:23.0-java17
```

This allows you to control the exact environment for your native compilation.

### Handling Reflection for Native Images

GraalVM's native image generation involves static analysis to determine which classes and methods are reachable. Code that uses reflection, like JPA entities, might not be automatically detected, leading to `ClassNotFoundException` or similar errors at runtime.

To solve this, you need to explicitly tell GraalVM which classes need to be registered for reflection. Quarkus provides an annotation for this.

For example, if you have a JPA entity `MyEntity.java`, you should annotate it like this:

```java
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@Entity
public class MyEntity extends PanacheEntity {
    // ... your entity fields and methods
}
```

By adding `@RegisterForReflection` to your entities and other classes that require reflection, you ensure they are correctly included in the native executable.

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- Flyway ([guide](https://quarkus.io/guides/flyway)): Handle your database schema migrations
- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it
- Hibernate ORM with Panache ([guide](https://quarkus.io/guides/hibernate-orm-panache)): Simplify your persistence code for Hibernate ORM via the active record or the repository pattern
- JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC

## Provided Code

### Hibernate ORM

Create your first JPA entity

[Related guide section...](https://quarkus.io/guides/hibernate-orm)

[Related Hibernate with Panache section...](https://quarkus.io/guides/hibernate-orm-panache)


### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
