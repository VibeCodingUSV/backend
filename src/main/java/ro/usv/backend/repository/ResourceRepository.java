package ro.usv.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.usv.backend.entity.Resource;

public interface ResourceRepository extends JpaRepository<Resource, Long> {}