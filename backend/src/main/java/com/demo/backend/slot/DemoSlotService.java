package com.demo.backend.slot;

import com.demo.backend.course.CourseService;
import com.demo.backend.slot.dto.DemoSlotDto;
import com.demo.backend.slot.dto.GenerateSlotsRequest;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DemoSlotService {
  private final CourseService courses;
  private final DemoSlotRepository repo;

  @Transactional(readOnly = true)
  public List<DemoSlotDto> listForCourse(UUID courseId) {
    return repo.findByCourseIdOrderByDateAscStartTimeAsc(courseId).stream().map(DemoSlotService::toDto).toList();
  }

  @Transactional
  public List<DemoSlotDto> generateForCourse(UUID courseId, GenerateSlotsRequest req) {
    var course = courses.getEntity(courseId);

    var specs =
        SlotGenerator.generate(
            req.startDate(),
            req.endDate(),
            req.daysOfWeek(),
            req.dayStart(),
            req.dayEnd(),
            Duration.ofMinutes(req.slotMinutes()),
            Duration.ofMinutes(req.breakMinutes()));

    var toSave =
        specs.stream()
            .map(s -> new DemoSlot(course, s.date(), s.startTime(), s.endTime(), null))
            .toList();

    repo.saveAll(toSave);
    return listForCourse(courseId);
  }

  @Transactional
  public DemoSlotDto bookSlot(UUID courseId, UUID slotId, String studentEmail) {
    var slot = repo.findById(slotId)
        .filter(s -> courseId.equals(s.getCourse().getId()))
        .orElseThrow(() -> new IllegalArgumentException("Slot not found."));
    if (slot.getStudentEmail() != null)
      throw new IllegalArgumentException("Slot already booked.");
    slot.setStudentEmail(studentEmail);
    return toDto(slot);
  }

  @Transactional
  public DemoSlotDto unbookSlot(UUID courseId, UUID slotId, String studentEmail) {
    var slot = repo.findById(slotId)
        .filter(s -> courseId.equals(s.getCourse().getId()))
        .orElseThrow(() -> new IllegalArgumentException("Slot not found."));
    if (!studentEmail.equals(slot.getStudentEmail()))
      throw new IllegalArgumentException("You did not book this slot.");
    slot.setStudentEmail(null);
    return toDto(slot);
  }

  @Transactional
  public void clearCourse(UUID courseId) {
    var existing = repo.findByCourseIdOrderByDateAscStartTimeAsc(courseId);
    repo.deleteAllInBatch(existing);
  }

  static DemoSlotDto toDto(DemoSlot s) {
    return new DemoSlotDto(
        s.getId(),
        s.getCourse() == null ? null : s.getCourse().getId(),
        s.getDate(),
        s.getStartTime(),
        s.getEndTime(),
        s.getNote(),
        s.getStudentEmail());
  }
}

