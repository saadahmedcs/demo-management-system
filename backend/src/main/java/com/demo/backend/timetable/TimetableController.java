package com.demo.backend.timetable;

import com.demo.backend.timetable.dto.TimetableEntryDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173", "http://localhost:3000"})
public class TimetableController {
  private final TimetableService service;

  @GetMapping("/{email}")
  public List<TimetableEntryDto> list(@PathVariable String email) {
    return service.list(email);
  }

  @PutMapping("/{email}")
  public List<TimetableEntryDto> save(@PathVariable String email,
      @RequestBody List<TimetableEntryDto> entries) {
    return service.save(email, entries);
  }
}
