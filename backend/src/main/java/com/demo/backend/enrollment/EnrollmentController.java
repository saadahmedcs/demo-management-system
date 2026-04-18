package com.demo.backend.enrollment;

import com.demo.backend.course.dto.CourseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173", "http://localhost:3000"})
public class EnrollmentController {
  private final EnrollmentService service;

  public record EnrollRequest(String studentEmail, String enrollmentCode) {}

  @PostMapping("/api/enroll")
  public CourseDto enroll(@RequestBody EnrollRequest req) {
    if (req.studentEmail() == null || req.enrollmentCode() == null)
      throw new IllegalArgumentException("studentEmail and enrollmentCode are required.");
    return service.enroll(req.studentEmail(), req.enrollmentCode());
  }

  @GetMapping("/api/students/{email}/courses")
  public List<CourseDto> listCourses(@PathVariable String email) {
    return service.listCoursesForStudent(email);
  }
}
