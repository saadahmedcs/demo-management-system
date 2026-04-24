package com.demo.backend.slot;

import com.demo.backend.course.CourseService;
import com.demo.backend.enrollment.EnrollmentRepository;
import com.demo.backend.slot.dto.DemoSlotDto;
import com.demo.backend.slot.dto.GenerateSlotsRequest;
import com.demo.backend.user.AppUser;
import com.demo.backend.user.AppUserRepository;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
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
  private final EnrollmentRepository enrollments;

  @Transactional(readOnly = true)
  public List<DemoSlotDto> listForCourse(UUID courseId) {
    return repo.findByCourseIdOrderByDateAscStartTimeAsc(courseId).stream().map(DemoSlotService::toDto).toList();
  }

  @Transactional
  public List<DemoSlotDto> generateForCourse(UUID courseId, GenerateSlotsRequest req) {
    var course = courses.getEntity(courseId);
    int configuredSize =
        Boolean.TRUE.equals(req.groupAssignment())
            ? (req.groupMemberCount() == null || req.groupMemberCount() < 1 ? 2 : req.groupMemberCount())
            : 1;

    var specs =
        SlotGenerator.generate(
            req.startDate(),
            req.endDate(),
            req.daysOfWeek(),
            req.dayStart(),
            req.dayEnd(),
            Duration.ofMinutes(req.slotMinutes()),
            Duration.ofMinutes(req.breakMinutes()));

    var toSave = new ArrayList<DemoSlot>();
    int slotsPerWindow = Boolean.TRUE.equals(req.groupAssignment()) ? configuredSize : 1;
    for (var s : specs) {
      for (int i = 0; i < slotsPerWindow; i++) {
        var slot =
            new DemoSlot(
                course,
                s.date(),
                s.startTime(),
                s.endTime(),
                null,
                req.assignmentName());
        slot.setGroupMemberCount(configuredSize);
        toSave.add(slot);
      }
    }

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
    var slot =
        repo.findById(slotId)
            .filter(s -> courseId.equals(s.getCourse().getId()))
            .orElseThrow(() -> new IllegalArgumentException("Slot not found."));
    if (slot.getStudentEmail() != null) {
      throw new IllegalArgumentException("Slot already booked.");
    }
    if (repo.existsByCourseIdAndStudentEmail(courseId, studentEmail)) {
      throw new IllegalArgumentException("You already have a booked slot in this course.");
    }

    int effectiveGroupSize =
        groupMemberCount != null && groupMemberCount > 0
            ? groupMemberCount
            : (slot.getGroupMemberCount() != null ? slot.getGroupMemberCount() : 1);

    var cleanedRolls =
        groupMemberRollNumbers == null
            ? List.<String>of()
            : groupMemberRollNumbers.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(String::trim)
                .distinct()
                .toList();

    var siblings =
        repo.findByCourse_IdAndDateAndStartTimeAndEndTimeAndAssignmentNameOrderById(
            courseId,
            slot.getDate(),
            slot.getStartTime(),
            slot.getEndTime(),
            slot.getAssignmentName());

    long vacantCount = siblings.stream().filter(s -> s.getStudentEmail() == null).count();
    if (vacantCount == 0) {
      throw new IllegalArgumentException("No open seats in this timeslot.");
    }

    if (effectiveGroupSize > 1 && vacantCount > 1 && cleanedRolls.isEmpty()) {
      throw new IllegalArgumentException("Enter at least one roll number.");
    }

    List<AppUser> peerUsers = new ArrayList<>();
    if (!cleanedRolls.isEmpty()) {
      LinkedHashSet<String> seenPeerEmails = new LinkedHashSet<>();
      for (String roll : cleanedRolls) {
        AppUser u = resolveEnrolledUserByRoll(courseId, roll);
        if (studentEmail.equals(u.getEmail())) {
          continue;
        }
        if (seenPeerEmails.add(u.getEmail())) {
          peerUsers.add(u);
        }
      }
    }

    var vacantOrdered =
        siblings.stream()
            .filter(s -> s.getStudentEmail() == null)
            .sorted(Comparator.comparing(DemoSlot::getId))
            .toList();

    boolean clickedIsVacant = vacantOrdered.stream().anyMatch(s -> s.getId().equals(slotId));
    if (!clickedIsVacant) {
      throw new IllegalArgumentException("Slot already booked.");
    }

    var otherVacant =
        vacantOrdered.stream().filter(s -> !s.getId().equals(slotId)).toList();

    if (peerUsers.size() > otherVacant.size()) {
      throw new IllegalArgumentException(
          "Too many roll numbers for the open seats in this timeslot.");
    }

    for (AppUser peer : peerUsers) {
      if (repo.existsByCourseIdAndStudentEmail(courseId, peer.getEmail())) {
        throw new IllegalArgumentException(
            "Roll number \""
                + peer.getRollNumber()
                + "\" already has a booked slot in this course.");
      }
    }

    if (groupMemberCount != null && groupMemberCount > 0) {
      slot.setGroupMemberCount(groupMemberCount);
    } else if (slot.getGroupMemberCount() == null) {
      slot.setGroupMemberCount(1);
    }

    slot.setStudentEmail(studentEmail);
    var user = users.findById(studentEmail).orElse(null);
    if (user != null) {
      slot.setStudentRollNumber(user.getRollNumber());
      slot.setStudentSection(user.getSection());
    }
    if (!cleanedRolls.isEmpty()) {
      slot.setGroupMemberRollNumbers(String.join(", ", cleanedRolls));
    }

    for (int i = 0; i < peerUsers.size(); i++) {
      DemoSlot peerSlot = otherVacant.get(i);
      AppUser peer = peerUsers.get(i);
      peerSlot.setStudentEmail(peer.getEmail());
      peerSlot.setStudentRollNumber(peer.getRollNumber());
      peerSlot.setStudentSection(peer.getSection());
    }

    return toDto(slot);
  }

  private AppUser resolveEnrolledUserByRoll(UUID courseId, String roll) {
    var matches = users.findByRollNumberEqualsIgnoreCase(roll.trim());
    if (matches.isEmpty()) {
      throw new IllegalArgumentException(
          "Roll number \"" + roll + "\" does not match any registered account.");
    }
    if (matches.size() > 1) {
      throw new IllegalArgumentException(
          "Roll number \"" + roll + "\" matches more than one account; contact your TA.");
    }
    AppUser u = matches.get(0);
    if (!enrollments.existsByCourseIdAndStudentEmail(courseId, u.getEmail())) {
      throw new IllegalArgumentException(
          "Roll number \"" + roll + "\" is not enrolled in this course.");
    }
    return u;
  }

  @Transactional
  public DemoSlotDto unbookSlot(UUID courseId, UUID slotId, String studentEmail) {
    var slot =
        repo.findById(slotId)
            .filter(s -> courseId.equals(s.getCourse().getId()))
            .orElseThrow(() -> new IllegalArgumentException("Slot not found."));
    if (!studentEmail.equals(slot.getStudentEmail())) {
      throw new IllegalArgumentException("You did not book this slot.");
    }
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
