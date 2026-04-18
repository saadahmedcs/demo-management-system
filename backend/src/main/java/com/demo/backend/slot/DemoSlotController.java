package com.demo.backend.slot;

import com.demo.backend.slot.dto.BookSlotRequest;
import com.demo.backend.slot.dto.DemoSlotDto;
import com.demo.backend.slot.dto.GenerateSlotsRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses/{courseId}/slots")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173", "http://localhost:3000"})
public class DemoSlotController {
  private final DemoSlotService service;

  @GetMapping
  public List<DemoSlotDto> list(@PathVariable UUID courseId) {
    return service.listForCourse(courseId);
  }

  @PostMapping("/generate")
  public List<DemoSlotDto> generate(@PathVariable UUID courseId, @Valid @RequestBody GenerateSlotsRequest req) {
    if (req == null) throw new IllegalArgumentException("Missing request body.");
    return service.generateForCourse(courseId, req);
  }

  @DeleteMapping
  public void clear(@PathVariable UUID courseId) {
    service.clearCourse(courseId);
  }

  @PostMapping("/{slotId}/book")
  public DemoSlotDto book(@PathVariable UUID courseId, @PathVariable UUID slotId,
      @RequestBody BookSlotRequest req) {
    return service.bookSlot(
        courseId, slotId, req.studentEmail(), req.groupMemberCount(), req.groupMemberRollNumbers());
  }

  @PostMapping("/{slotId}/unbook")
  public DemoSlotDto unbook(@PathVariable UUID courseId, @PathVariable UUID slotId,
      @RequestBody BookSlotRequest req) {
    return service.unbookSlot(courseId, slotId, req.studentEmail());
  }
}
