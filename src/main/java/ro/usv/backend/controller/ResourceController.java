java package ro.usv.backend.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ro.usv.backend.dto.ResourceRequest;
import ro.usv.backend.dto.ResourceResponse;
import ro.usv.backend.service.ResourceService;
import ro.usv.backend.service.UserService;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final UserService userService;

    public ResourceController(ResourceService resourceService, UserService userService) {
        this.resourceService = resourceService;
        this.userService = userService;
    }

    @GetMapping
    public List<ResourceResponse> getAll() {
        return resourceService.findAll();
    }

    @GetMapping("/{id}")
    public ResourceResponse getById(@PathVariable Long id) {
        return resourceService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponse create(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody ResourceRequest request) {
        return resourceService.create(userService.requireAdmin(userId), request);
    }

    @PutMapping("/{id}")
    public ResourceResponse update(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id,
            @Valid @RequestBody ResourceRequest request) {
        return resourceService.update(userService.requireAdmin(userId), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archive(
            @RequestHeader(value = "X-User-Id", required = false) Long userId, @PathVariable Long id) {
        resourceService.archive(userService.requireAdmin(userId), id);
    }
}