package com.demo.backend.course;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, UUID> {
  java.util.Optional<Course> findByEnrollmentCode(String enrollmentCode);
}

