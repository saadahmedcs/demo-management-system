package com.demo.backend.message;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {
  List<Message> findByCourseIdAndSenderEmailAndRecipientEmailOrderBySentAtAsc(
      UUID courseId, String senderEmail, String recipientEmail);
}
