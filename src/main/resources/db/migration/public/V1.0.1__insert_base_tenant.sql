INSERT INTO organizations(
    id,
    name,
    tenant_id,
    subscription_plan
) VALUES (
    nextval('organization_seq'),
    'Base Tenant',
    'base',
    'FREE'
)

