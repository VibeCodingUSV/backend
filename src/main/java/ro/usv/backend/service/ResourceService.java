package ro.usv.backend.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.usv.backend.dto.ResourceRequest;
import ro.usv.backend.dto.ResourceResponse;
import ro.usv.backend.entity.Resource;
import ro.usv.backend.entity.ResourceType;
import ro.usv.backend.exception.NotFoundException;
import ro.usv.backend.repository.ResourceRepository;
import ro.usv.backend.repository.ResourceTypeRepository;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceTypeRepository resourceTypeRepository;

    public ResourceService(
            ResourceRepository resourceRepository, ResourceTypeRepository resourceTypeRepository) {
        this.resourceRepository = resourceRepository;
        this.resourceTypeRepository = resourceTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> findAll() {
        return resourceRepository.findAll().stream().map(ResourceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ResourceResponse findById(Long id) {
        return ResourceResponse.from(getResource(id));
    }

    @Transactional
    public ResourceResponse create(ResourceRequest request) {
        Resource resource = new Resource();
        apply(resource, request);
        return ResourceResponse.from(resourceRepository.save(resource));
    }

    @Transactional
    public ResourceResponse update(Long id, ResourceRequest request) {
        Resource resource = getResource(id);
        apply(resource, request);
        return ResourceResponse.from(resourceRepository.save(resource));
    }

    private Resource getResource(Long id) {
        return resourceRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Resursa cu id " + id + " nu există"));
    }

    private void apply(Resource resource, ResourceRequest request) {
        ResourceType type =
                resourceTypeRepository
                        .findById(request.resourceTypeId())
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Tipul de resursă cu id " + request.resourceTypeId() + " nu există"));
        resource.setName(request.name());
        resource.setDescription(request.description());
        resource.setResourceType(type);
        resource.setLocation(request.location());
        resource.setStatus(request.status());
        resource.setRequiresApproval(Boolean.TRUE.equals(request.requiresApproval()));
    }
}