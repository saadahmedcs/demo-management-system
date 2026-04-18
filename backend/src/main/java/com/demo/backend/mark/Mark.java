package com.demo.backend.mark;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "marks")
@Getter
@Setter
@NoArgsConstructor
public class Mark {
  @Id private UUID id = UUID.randomUUID();

  @Column(nullable = false) private UUID courseId;
  @Column(nullable = false) private UUID slotId;
  @Column(nullable = true, length = 200) private String studentEmail;
  @Column(nullable = true) private Double score;
  @Column(nullable = true, length = 500) private String feedback;

  public Mark(UUID courseId, UUID slotId, String studentEmail) {
    this.courseId = courseId;
    this.slotId = slotId;
    this.studentEmail = studentEmail;
  }
}
