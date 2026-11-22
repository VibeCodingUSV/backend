java
        package ro.usv.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ro.usv.backend.entity.ResourceStatus;

public record ResourceRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 1000) String description,
        @NotNull Long resourceTypeId,
        @Size(max = 150) String location,
        @NotNull ResourceStatus status,
        Boolean requiresApproval,
        @Size(max = 500) String imageUrl,
        @Size(max = 1000) String specs) {}