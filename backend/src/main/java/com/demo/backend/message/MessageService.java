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
  public List<MessageDto> listForCourseDm(UUID courseId, String viewerEmail, String peerEmail) {
    if (viewerEmail == null || viewerEmail.isBlank() || peerEmail == null || peerEmail.isBlank()) {
      throw new IllegalArgumentException("viewerEmail and peerEmail are required.");
    }
    var sent =
        repo.findByCourseIdAndSenderEmailAndRecipientEmailOrderBySentAtAsc(
            courseId, viewerEmail.trim(), peerEmail.trim());
    var received =
        repo.findByCourseIdAndSenderEmailAndRecipientEmailOrderBySentAtAsc(
            courseId, peerEmail.trim(), viewerEmail.trim());
    return java.util.stream.Stream.concat(sent.stream(), received.stream())
        .sorted(java.util.Comparator.comparing(Message::getSentAt))
        .map(MessageService::toDto)
        .toList();
  }

  @Transactional
  public MessageDto send(UUID courseId, String senderEmail, String recipientEmail, String text) {
    if (text == null || text.isBlank())
      throw new IllegalArgumentException("Message cannot be empty.");
    if (recipientEmail == null || recipientEmail.isBlank())
      throw new IllegalArgumentException("recipientEmail is required.");
    var msg = new Message(courseId, senderEmail, recipientEmail.trim(), text.trim());
    repo.save(msg);
    return toDto(msg);
  }

  static MessageDto toDto(Message m) {
    return new MessageDto(
        m.getId(), m.getCourseId(), m.getSenderEmail(), m.getRecipientEmail(), m.getText(), m.getSentAt());
  }
}
