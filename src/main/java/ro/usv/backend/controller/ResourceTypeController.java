package ro.usv.backend.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.usv.backend.dto.ResourceTypeResponse;
import ro.usv.backend.repository.ResourceTypeRepository;

@RestController
@RequestMapping("/api/resource-types")
public class ResourceTypeController {

    private final ResourceTypeRepository resourceTypeRepository;

    public ResourceTypeController(ResourceTypeRepository resourceTypeRepository) {
        this.resourceTypeRepository = resourceTypeRepository;
    }

    @GetMapping
    public List<ResourceTypeResponse> getAll() {
        return resourceTypeRepository.findAll().stream().map(ResourceTypeResponse::from).toList();
    }
}