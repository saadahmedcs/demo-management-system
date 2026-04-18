package com.demo.client.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record DemoSlotDto(
    UUID id,
    UUID courseId,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    String note,
    String assignmentName,
    String studentEmail,
    String studentRollNumber,
    String studentSection,
    Integer groupMemberCount,
    String groupMemberRollNumbers) {}

