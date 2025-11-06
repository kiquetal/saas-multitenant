package me.cresterida.admin;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.cresterida.tenant.Organization;
import me.cresterida.tenant.TenantSchemaManager;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.logging.Logger;

@Path("/admin")
@Tag(name = "Admin Operations", description = "Endpoints for tenant and organization management")
public class AdminResource {

    private final Logger LOGGER = Logger.getLogger(AdminResource.class.getName());

    @Inject
    TenantSchemaManager tenantSchemaManager;

    @POST
    @Path("/organizations")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(summary = "Create a new organization and initialize its tenant schema")
    @APIResponse(
        responseCode = "201",
        description = "Organization created successfully",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = Organization.class))
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid organization data provided"
    )
    @APIResponse(
        responseCode = "409",
        description = "Organization with the same tenant ID already exists"
    )
    public Response createOrganization(
        @RequestBody(
            required = true,
            content = @Content(schema = @Schema(implementation = OrganizationDTO.class))
        )
        OrganizationDTO organizationDTO
    ) {
        try {
            // Check if tenant already exists
            if (Organization.findByTenantId(organizationDTO.tenantId()) != null) {
                return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("Tenant ID already exists"))
                    .build();
            }

            // Create organization
            Organization organization = new Organization();
            organization.setName(organizationDTO.name());
            organization.setTenantId(organizationDTO.tenantId());
            organization.setSubscriptionPlan(organizationDTO.subscriptionPlan());
            
            // Persist organization
            organization.persist();

            // Initialize tenant schema
            tenantSchemaManager.initializeTenantSchema(organization.getTenantId());

            return Response.status(Response.Status.CREATED)
                .entity(organization)
                .build();

        } catch (Exception e) {
            LOGGER.severe("Error creating organization: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("Failed to create organization: " + e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/organizations")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List all organizations")
    @APIResponse(
        responseCode = "200",
        description = "List of all organizations",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = Organization.class))
    )
    public Response listOrganizations() {
        try {
            List<Organization> organizations = Organization.findAllOrganizations();
            return Response.ok(organizations).build();
        } catch (Exception e) {
            LOGGER.severe("Error listing organizations: " + e.getMessage());
            return Response.serverError()
                .entity(new ErrorResponse("Failed to list organizations: " + e.getMessage()))
                .build();
        }
    }

    @DELETE
    @Path("/organizations/{tenantId}")
    @Operation(summary = "Delete an organization and its tenant schema")
    @APIResponse(
        responseCode = "204",
        description = "Organization deleted successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Organization not found"
    )
    @Transactional
    public Response deleteOrganization(@PathParam("tenantId") String tenantId) {
        try {
            Organization organization = Organization.findByTenantId(tenantId);
            if (organization == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Organization not found"))
                    .build();
            }

            // Delete the organization
            organization.delete();

            // Note: You might want to add schema deletion here if required
            return Response.noContent().build();

        } catch (Exception e) {
            LOGGER.severe("Error deleting organization: " + e.getMessage());
            return Response.serverError()
                .entity(new ErrorResponse("Failed to delete organization: " + e.getMessage()))
                .build();
        }
    }
}
