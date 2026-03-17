package com.demo.backend.slot.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record GenerateSlotsRequest(
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    Set<DayOfWeek> daysOfWeek,
    @NotNull LocalTime dayStart,
    @NotNull LocalTime dayEnd,
    @Min(1) @Max(240) int slotMinutes,
    @Min(0) @Max(120) int breakMinutes) {}

