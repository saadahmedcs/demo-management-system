package com.demo.backend.message;

import com.demo.backend.message.dto.MessageDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {
  private final MessageRepository repo;

  @Transactional(readOnly = true)
  public List<MessageDto> listForCourse(UUID courseId) {
    return repo.findByCourseIdOrderBySentAtAsc(courseId).stream().map(MessageService::toDto).toList();
  }

  @Transactional
  public MessageDto send(UUID courseId, String senderEmail, String text) {
    if (text == null || text.isBlank())
      throw new IllegalArgumentException("Message cannot be empty.");
    var msg = new Message(courseId, senderEmail, text.trim());
    repo.save(msg);
    return toDto(msg);
  }

  static MessageDto toDto(Message m) {
    return new MessageDto(m.getId(), m.getCourseId(), m.getSenderEmail(), m.getText(), m.getSentAt());
  }
}
