package ro.usv.backend.dto;

import ro.usv.backend.entity.ResourceType;

public record ResourceTypeResponse(Long id, String name, String description) {

    public static ResourceTypeResponse from(ResourceType type) {
        return new ResourceTypeResponse(type.getId(), type.getName(), type.getDescription());
    }
}