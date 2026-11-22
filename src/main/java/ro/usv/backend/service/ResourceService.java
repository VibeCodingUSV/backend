java package ro.usv.backend.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.usv.backend.dto.ResourceRequest;
import ro.usv.backend.dto.ResourceResponse;
import ro.usv.backend.entity.Resource;
import ro.usv.backend.entity.ResourceStatus;
import ro.usv.backend.entity.ResourceType;
import ro.usv.backend.entity.User;
import ro.usv.backend.exception.NotFoundException;
import ro.usv.backend.repository.ReservationRepository;
import ro.usv.backend.repository.ResourceRepository;
import ro.usv.backend.repository.ResourceTypeRepository;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    private final ReservationRepository reservationRepository;
    private final AuditService auditService;

    public ResourceService(
            ResourceRepository resourceRepository,
            ResourceTypeRepository resourceTypeRepository,
            ReservationRepository reservationRepository,
            AuditService auditService) {
        this.resourceRepository = resourceRepository;
        this.resourceTypeRepository = resourceTypeRepository;
        this.reservationRepository = reservationRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> findAll() {
        Set<Long> busy = busyIds();
        return resourceRepository.findAll().stream()
                .filter(r -> r.getStatus() != ResourceStatus.ARCHIVED)
                .sorted(Comparator.comparing(Resource::getId))
                .map(r -> ResourceResponse.from(r, busy.contains(r.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ResourceResponse findById(Long id) {
        return toResponse(getResource(id));
    }

    @Transactional
    public ResourceResponse create(User actor, ResourceRequest request) {
        Resource resource = new Resource();
        apply(resource, request);
        Resource saved = resourceRepository.save(resource);
        auditService.log(actor, "RESOURCE_CREATED", "RESOURCE", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    @Transactional
    public ResourceResponse update(User actor, Long id, ResourceRequest request) {
        Resource resource = getResource(id);
        apply(resource, request);
        Resource saved = resourceRepository.save(resource);
        auditService.log(actor, "RESOURCE_UPDATED", "RESOURCE", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    @Transactional
    public void archive(User actor, Long id) {
        Resource resource = getResource(id);
        resource.setStatus(ResourceStatus.ARCHIVED);
        resourceRepository.save(resource);
        auditService.log(actor, "RESOURCE_ARCHIVED", "RESOURCE", id, resource.getName());
    }

    private Set<Long> busyIds() {
        return new HashSet<>(reservationRepository.findBusyResourceIds(LocalDateTime.now()));
    }

    private ResourceResponse toResponse(Resource resource) {
        return ResourceResponse.from(resource, busyIds().contains(resource.getId()));
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
        resource.setImageUrl(blankToNull(request.imageUrl()));
        resource.setSpecs(blankToNull(request.specs()));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}