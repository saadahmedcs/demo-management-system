package com.demo.backend.slot;

import com.demo.backend.course.CourseService;
import com.demo.backend.slot.dto.DemoSlotDto;
import com.demo.backend.slot.dto.GenerateSlotsRequest;
import com.demo.backend.user.AppUserRepository;
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
  private final AppUserRepository users;

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
            .map(
                s -> {
                  var slot =
                      new DemoSlot(
                        course,
                        s.date(),
                        s.startTime(),
                        s.endTime(),
                        null,
                        req.assignmentName());
                  int configuredSize =
                      Boolean.TRUE.equals(req.groupAssignment())
                          ? (req.groupMemberCount() == null || req.groupMemberCount() < 1 ? 2 : req.groupMemberCount())
                          : 1;
                  slot.setGroupMemberCount(configuredSize);
                  return slot;
                })
            .toList();

    repo.saveAll(toSave);
    return listForCourse(courseId);
  }

  @Transactional
  public DemoSlotDto bookSlot(
      UUID courseId,
      UUID slotId,
      String studentEmail,
      Integer groupMemberCount,
      List<String> groupMemberRollNumbers) {
    var slot = repo.findById(slotId)
        .filter(s -> courseId.equals(s.getCourse().getId()))
        .orElseThrow(() -> new IllegalArgumentException("Slot not found."));
    if (slot.getStudentEmail() != null)
      throw new IllegalArgumentException("Slot already booked.");
    if (repo.existsByCourseIdAndStudentEmail(courseId, studentEmail))
      throw new IllegalArgumentException("You already have a booked slot in this course.");
    slot.setStudentEmail(studentEmail);
    if (groupMemberCount != null && groupMemberCount > 0) {
      slot.setGroupMemberCount(groupMemberCount);
    } else if (slot.getGroupMemberCount() == null) {
      slot.setGroupMemberCount(1);
    }
    var user = users.findById(studentEmail).orElse(null);
    if (user != null) {
      slot.setStudentRollNumber(user.getRollNumber());
      slot.setStudentSection(user.getSection());
    }
    if (groupMemberRollNumbers != null && !groupMemberRollNumbers.isEmpty()) {
      var cleaned =
          groupMemberRollNumbers.stream()
              .filter(v -> v != null && !v.isBlank())
              .map(String::trim)
              .distinct()
              .toList();
      if (!cleaned.isEmpty()) {
        slot.setGroupMemberRollNumbers(String.join(", ", cleaned));
      }
    }
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
    slot.setStudentRollNumber(null);
    slot.setStudentSection(null);
    slot.setGroupMemberRollNumbers(null);
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
        s.getAssignmentName(),
        s.getStudentEmail(),
        s.getStudentRollNumber(),
        s.getStudentSection(),
        s.getGroupMemberCount(),
        s.getGroupMemberRollNumbers());
  }
}

