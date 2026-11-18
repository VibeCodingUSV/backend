package ro.usv.backend.dto;

import java.time.LocalDateTime;
import ro.usv.backend.entity.Resource;
import ro.usv.backend.entity.ResourceStatus;

public record ResourceResponse(
        Long id,
        String name,
        String description,
        Long resourceTypeId,
        String resourceTypeName,
        String location,
        ResourceStatus status,
        boolean requiresApproval,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static ResourceResponse from(Resource resource) {
        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getDescription(),
                resource.getResourceType().getId(),
                resource.getResourceType().getName(),
                resource.getLocation(),
                resource.getStatus(),
                resource.isRequiresApproval(),
                resource.getCreatedAt(),
                resource.getUpdatedAt());
    }
}