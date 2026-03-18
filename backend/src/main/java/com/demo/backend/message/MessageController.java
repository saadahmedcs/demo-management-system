package com.demo.backend.message;

import com.demo.backend.message.dto.MessageDto;
import com.demo.backend.message.dto.SendMessageRequest;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses/{courseId}/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173", "http://localhost:3000"})
public class MessageController {
  private final MessageService service;

  @GetMapping
  public List<MessageDto> list(@PathVariable UUID courseId) {
    return service.listForCourse(courseId);
  }

  @PostMapping
  public MessageDto send(@PathVariable UUID courseId, @RequestBody SendMessageRequest req) {
    return service.send(courseId, req.senderEmail(), req.text());
  }
}
