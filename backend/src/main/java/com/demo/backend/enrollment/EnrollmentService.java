package com.demo.backend.enrollment;

import com.demo.backend.course.Course;
import com.demo.backend.course.CourseRepository;
import com.demo.backend.course.CourseService;
import com.demo.backend.course.dto.CourseDto;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
  private final EnrollmentRepository repo;
  private final CourseRepository courseRepo;

  @Transactional
  public CourseDto enroll(String studentEmail, String enrollmentCode) {
    Course course = courseRepo.findByEnrollmentCode(enrollmentCode)
        .orElseThrow(() -> new IllegalArgumentException("Invalid enrollment code. Please check with your TA."));
    if (!repo.existsByCourseIdAndStudentEmail(course.getId(), studentEmail)) {
      repo.save(new Enrollment(course.getId(), studentEmail));
    }
    return CourseService.toDto(course);
  }

  @Transactional(readOnly = true)
  public List<CourseDto> listCoursesForStudent(String studentEmail) {
    return repo.findByStudentEmail(studentEmail).stream()
        .map(e -> courseRepo.findById(e.getCourseId()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(CourseService::toDto)
        .toList();
  }
}
