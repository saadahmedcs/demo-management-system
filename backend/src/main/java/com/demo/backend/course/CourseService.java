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

  @Transactional
  public CourseDto create(String code, String name) {
    var course = new Course(code, name);
    repo.save(course);
    return toDto(course);
  }

  @Transactional(readOnly = true)
  public Course getEntity(UUID id) {
    return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Course not found."));
  }

  static CourseDto toDto(Course c) {
    return new CourseDto(c.getId(), c.getCode(), c.getName());
  }
}

