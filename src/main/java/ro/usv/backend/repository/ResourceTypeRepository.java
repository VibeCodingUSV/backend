package ro.usv.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.usv.backend.entity.ResourceType;

public interface ResourceTypeRepository extends JpaRepository<ResourceType, Long> {}