# SaaS Multi-tenant Application Build Guide

This guide explains how to build and run the SaaS multi-tenant application.

## Features

### Multi-tenancy
- Schema-based multi-tenancy using PostgreSQL
- Automatic tenant schema initialization
- Flyway migrations for both public and tenant-specific schemas

### Administrative Features
- Organization management through REST API
- Tenant provisioning and initialization
- Error handling and validation
- OpenAPI documentation

## API Documentation

The application includes Swagger UI for API documentation and testing:
- Swagger UI: http://localhost:8080/swagger-ui
- OpenAPI specification: http://localhost:8080/openapi

## Administrative Endpoints

### Organizations Management
- Create organization: POST /admin/organizations
- List organizations: GET /admin/organizations
- Delete organization: DELETE /admin/organizations/{tenantId}

## Building the Application

```bash
./mvnw clean package
```

## Running the Application

```bash
./mvnw quarkus:dev
```

## Database Migrations

The application uses Flyway for database migrations:
- Public schema migrations: `src/main/resources/db/migration/public/`
- Tenant-specific migrations: `src/main/resources/db/migration/tenants/`

## Testing

To create a new organization using curl:

```bash
curl -X POST http://localhost:8080/admin/organizations \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Test Organization",
    "tenantId": "test_org",
    "subscriptionPlan": "FREE"
  }'
```

## Error Handling

The application includes comprehensive error handling:
- Duplicate tenant detection
- Schema creation validation
- Database operation error handling
- HTTP-status appropriate responses
