package com.demo.backend.course;

import com.demo.backend.course.dto.CourseDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {
  private final CourseRepository repo;

  @Transactional(readOnly = true)
  public List<CourseDto> list() {
    return repo.findAll().stream().map(CourseService::toDto).toList();
  }

  @Transactional(readOnly = true)
  public CourseDto get(UUID id) {
    return toDto(getEntity(id));
  }

  @Transactional
  public CourseDto create(String code, String name, String taEmail, String enrollmentCode) {
    var course = new Course(code, name, taEmail);
    course.setEnrollmentCode(enrollmentCode == null || enrollmentCode.isBlank() ? null : enrollmentCode.trim());
    repo.save(course);
    return toDto(course);
  }

  @Transactional
  public CourseDto updateVenue(UUID id, String venue) {
    var course = getEntity(id);
    course.setVenue(venue == null || venue.isBlank() ? null : venue.trim());
    return toDto(course);
  }

  @Transactional
  public void uploadRubric(UUID id, String filename, byte[] content) {
    var course = getEntity(id);
    course.setRubricFilename(filename);
    course.setRubricContent(content);
  }

  @Transactional(readOnly = true)
  public byte[] getRubricContent(UUID id) {
    var course = getEntity(id);
    if (course.getRubricContent() == null)
      throw new IllegalArgumentException("No rubric uploaded for this course.");
    return course.getRubricContent();
  }

  @Transactional(readOnly = true)
  public Course getEntity(UUID id) {
    return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Course not found."));
  }

  public static CourseDto toDto(Course c) {
    return new CourseDto(
        c.getId(), c.getCode(), c.getName(), c.getTaEmail(),
        c.getVenue(), c.getRubricFilename(), c.getEnrollmentCode());
  }
}
