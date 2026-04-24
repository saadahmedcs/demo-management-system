package com.demo.backend.slot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemoSlotRepository extends JpaRepository<DemoSlot, UUID> {
  List<DemoSlot> findByCourseIdOrderByDateAscStartTimeAsc(UUID courseId);

  boolean existsByCourseIdAndStudentEmail(UUID courseId, String studentEmail);

  /** All physical rows for the same course timeslot window (group slots share this key). */
  List<DemoSlot> findByCourse_IdAndDateAndStartTimeAndEndTimeAndAssignmentNameOrderById(
      UUID courseId,
      LocalDate date,
      LocalTime startTime,
      LocalTime endTime,
      String assignmentName);
}

