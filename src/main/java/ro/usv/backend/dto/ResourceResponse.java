java
        package ro.usv.backend.dto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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
        String imageUrl,
        List<String> specs,
        String availability,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    /** availability: AVAILABLE, BUSY (rezervare confirmată în acest moment) sau MAINTENANCE. */
    public static ResourceResponse from(Resource r, boolean busyNow) {
        String availability =
                r.getStatus() == ResourceStatus.MAINTENANCE ? "MAINTENANCE" : busyNow ? "BUSY" : "AVAILABLE";
        List<String> specs =
                r.getSpecs() == null || r.getSpecs().isBlank()
                        ? List.of()
                        : Arrays.stream(r.getSpecs().split("\\|")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        return new ResourceResponse(
                r.getId(),
                r.getName(),
                r.getDescription(),
                r.getResourceType().getId(),
                r.getResourceType().getName(),
                r.getLocation(),
                r.getStatus(),
                r.isRequiresApproval(),
                r.getImageUrl(),
                specs,
                availability,
                r.getCreatedAt(),
                r.getUpdatedAt());
    }
}