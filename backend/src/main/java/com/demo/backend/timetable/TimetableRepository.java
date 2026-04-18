package com.demo.backend.timetable;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimetableRepository extends JpaRepository<TimetableEntry, UUID> {
  List<TimetableEntry> findByStudentEmail(String studentEmail);
  void deleteByStudentEmail(String studentEmail);
}
