package com.demo.backend.enrollment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "enrollments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "student_email"}))
@Getter
@Setter
@NoArgsConstructor
public class Enrollment {
  @Id private UUID id = UUID.randomUUID();

  @Column(name = "course_id", nullable = false) private UUID courseId;

  @Column(name = "student_email", nullable = false, length = 200) private String studentEmail;

  public Enrollment(UUID courseId, String studentEmail) {
    this.courseId = courseId;
    this.studentEmail = studentEmail;
  }
}
