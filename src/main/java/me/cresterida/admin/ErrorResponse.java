package me.cresterida.admin;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Error response")
public record ErrorResponse(
    @Schema(description = "Error message", example = "Failed to create organization")
    String message
) {}
