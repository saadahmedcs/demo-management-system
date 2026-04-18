package com.demo.backend.mark;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarkRepository extends JpaRepository<Mark, UUID> {
  List<Mark> findByCourseId(UUID courseId);
  Optional<Mark> findBySlotId(UUID slotId);
}
