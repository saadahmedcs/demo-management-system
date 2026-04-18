package com.demo.backend.enrollment;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
  List<Enrollment> findByStudentEmail(String studentEmail);
  boolean existsByCourseIdAndStudentEmail(UUID courseId, String studentEmail);
}
