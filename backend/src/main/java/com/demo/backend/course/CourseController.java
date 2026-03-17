package com.demo.backend.course;

import com.demo.backend.course.dto.CourseDto;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173", "http://localhost:3000"})
@Validated
public class CourseController {
  private final CourseService service;

  public record CreateCourseRequest(String code, @NotBlank String name) {}

  @GetMapping
  public List<CourseDto> list() {
    return service.list();
  }

  @PostMapping
  public CourseDto create(@RequestBody CreateCourseRequest req) {
    if (req == null) throw new IllegalArgumentException("Missing request body.");
    return service.create(req.code(), req.name());
  }
}

