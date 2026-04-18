package com.demo.backend.mark;

import com.demo.backend.mark.dto.MarkDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarkService {
  private final MarkRepository repo;

  @Transactional(readOnly = true)
  public List<MarkDto> listForCourse(UUID courseId) {
    return repo.findByCourseId(courseId).stream().map(MarkService::toDto).toList();
  }

  @Transactional
  public MarkDto save(UUID courseId, UUID slotId, String studentEmail, Double score, String feedback) {
    var mark = repo.findBySlotId(slotId).orElseGet(() -> new Mark(courseId, slotId, studentEmail));
    mark.setScore(score);
    mark.setFeedback(feedback);
    if (studentEmail != null) mark.setStudentEmail(studentEmail);
    repo.save(mark);
    return toDto(mark);
  }

  static MarkDto toDto(Mark m) {
    return new MarkDto(
        m.getId(), m.getCourseId(), m.getSlotId(),
        m.getStudentEmail(), m.getScore(), m.getFeedback());
  }
}
