package com.demo.backend.mark;

import com.demo.backend.mark.dto.MarkDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses/{courseId}/marks")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173", "http://localhost:3000"})
public class MarkController {
  private final MarkService service;

  public record SaveMarkRequest(String studentEmail, Double score, String feedback) {}

  @GetMapping
  public List<MarkDto> list(@PathVariable UUID courseId) {
    return service.listForCourse(courseId);
  }

  @PutMapping("/{slotId}")
  public MarkDto save(
      @PathVariable UUID courseId,
      @PathVariable UUID slotId,
      @RequestBody SaveMarkRequest req) {
    return service.save(courseId, slotId, req.studentEmail(), req.score(), req.feedback());
  }
}
