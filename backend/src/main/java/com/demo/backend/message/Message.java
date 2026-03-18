package com.demo.backend.message;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
public class Message {
  @Id private UUID id = UUID.randomUUID();

  @Column(nullable = false)
  private UUID courseId;

  @Column(nullable = false, length = 200)
  private String senderEmail;

  @Column(nullable = false, length = 2000)
  private String text;

  @Column(nullable = false)
  private LocalDateTime sentAt = LocalDateTime.now();

  public Message(UUID courseId, String senderEmail, String text) {
    this.courseId = courseId;
    this.senderEmail = senderEmail;
    this.text = text;
  }
}
