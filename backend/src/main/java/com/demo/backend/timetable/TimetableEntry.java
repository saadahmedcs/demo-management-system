package com.demo.backend.timetable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "timetable_entries")
@Getter
@Setter
@NoArgsConstructor
public class TimetableEntry {
  @Id private UUID id = UUID.randomUUID();

  @Column(nullable = false, length = 200)
  private String studentEmail;

  /** e.g. "MONDAY", "TUESDAY" — stored as DayOfWeek name */
  @Column(nullable = false, length = 20)
  private String dayOfWeek;

  /**
   * 0 = 08:30-09:50, 1 = 10:00-11:20, 2 = 11:30-12:50,
   * 3 = 13:00-14:20, 4 = 14:30-15:55, 5 = 16:00-17:15
   */
  @Column(nullable = false)
  private int periodIndex;

  public TimetableEntry(String studentEmail, String dayOfWeek, int periodIndex) {
    this.studentEmail = studentEmail;
    this.dayOfWeek = dayOfWeek;
    this.periodIndex = periodIndex;
  }
}
