package com.demo.client.api.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record GenerateSlotsRequest(
    LocalDate startDate,
    LocalDate endDate,
    Set<DayOfWeek> daysOfWeek,
    LocalTime dayStart,
    LocalTime dayEnd,
    int slotMinutes,
    int breakMinutes) {}

