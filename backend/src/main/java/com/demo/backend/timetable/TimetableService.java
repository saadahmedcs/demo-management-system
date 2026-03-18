package com.demo.backend.timetable;

import com.demo.backend.timetable.dto.TimetableEntryDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TimetableService {
  private final TimetableRepository repo;

  @Transactional(readOnly = true)
  public List<TimetableEntryDto> list(String studentEmail) {
    return repo.findByStudentEmail(studentEmail).stream()
        .map(e -> new TimetableEntryDto(e.getDayOfWeek(), e.getPeriodIndex()))
        .toList();
  }

  @Transactional
  public List<TimetableEntryDto> save(String studentEmail, List<TimetableEntryDto> entries) {
    repo.deleteByStudentEmail(studentEmail);
    var toSave = entries.stream()
        .map(e -> new TimetableEntry(studentEmail, e.dayOfWeek(), e.periodIndex()))
        .toList();
    repo.saveAll(toSave);
    return entries;
  }
}
