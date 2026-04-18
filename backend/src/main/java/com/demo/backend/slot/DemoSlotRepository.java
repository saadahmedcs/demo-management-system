package com.demo.backend.slot;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemoSlotRepository extends JpaRepository<DemoSlot, UUID> {
  List<DemoSlot> findByCourseIdOrderByDateAscStartTimeAsc(UUID courseId);
  boolean existsByCourseIdAndStudentEmail(UUID courseId, String studentEmail);
}

