package com.demo.client.api.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record GenerateSlotsRequest(
    String assignmentName,
    Boolean groupAssignment,
    Integer groupMemberCount,
    LocalDate startDate,
    LocalDate endDate,
    Set<DayOfWeek> daysOfWeek,
    LocalTime dayStart,
    LocalTime dayEnd,
    int slotMinutes,
    int breakMinutes) {}

