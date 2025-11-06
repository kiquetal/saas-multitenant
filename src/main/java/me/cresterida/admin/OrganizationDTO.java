package me.cresterida.admin;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Organization creation request")
public record OrganizationDTO(
    @Schema(description = "Organization name", example = "ACME Corp")
    String name,
    
    @Schema(description = "Unique tenant identifier", example = "acme")
    String tenantId,
    
    @Schema(description = "Subscription plan type", example = "FREE")
    String subscriptionPlan
) {}
