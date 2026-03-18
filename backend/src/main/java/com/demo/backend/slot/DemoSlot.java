package com.demo.backend.slot;

import com.demo.backend.course.Course;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "demo_slots")
@Getter
@Setter
@NoArgsConstructor
public class DemoSlot {
  @Id private UUID id = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @Column(nullable = false) private LocalDate date;

  @Column(nullable = false) private LocalTime startTime;

  @Column(nullable = false) private LocalTime endTime;

  @Column(nullable = true, length = 2000)
  private String note;

  @Column(nullable = true, length = 200)
  private String studentEmail;

  public DemoSlot(Course course, LocalDate date, LocalTime startTime, LocalTime endTime, String note) {
    this.course = course;
    this.date = date;
    this.startTime = startTime;
    this.endTime = endTime;
    this.note = note;
  }
}

