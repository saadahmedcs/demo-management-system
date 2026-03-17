package com.demo.backend.slot;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

final class SlotGenerator {
  private SlotGenerator() {}

  record SlotSpec(LocalDate date, LocalTime startTime, LocalTime endTime) {}

  static List<SlotSpec> generate(
      LocalDate startDate,
      LocalDate endDate,
      Set<DayOfWeek> allowedDays,
      LocalTime dayStart,
      LocalTime dayEnd,
      Duration slotDuration,
      Duration breakDuration) {

    if (startDate == null
        || endDate == null
        || dayStart == null
        || dayEnd == null
        || slotDuration == null
        || breakDuration == null) {
      throw new IllegalArgumentException("Missing required input.");
    }
    if (endDate.isBefore(startDate)) throw new IllegalArgumentException("End date must be on/after start date.");
    if (!dayEnd.isAfter(dayStart)) throw new IllegalArgumentException("Day end must be after day start.");
    if (slotDuration.isNegative() || slotDuration.isZero())
      throw new IllegalArgumentException("Slot duration must be > 0.");
    if (breakDuration.isNegative()) throw new IllegalArgumentException("Break duration must be >= 0.");

    var days = allowedDays == null || allowedDays.isEmpty() ? EnumSet.allOf(DayOfWeek.class) : EnumSet.copyOf(allowedDays);
    var out = new ArrayList<SlotSpec>();

    for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
      if (!days.contains(d.getDayOfWeek())) continue;

      LocalTime t = dayStart;
      while (true) {
        LocalTime end = t.plus(slotDuration);
        if (end.isAfter(dayEnd) || end.equals(dayStart)) break;
        out.add(new SlotSpec(d, t, end));
        t = end.plus(breakDuration);
        if (!t.isBefore(dayEnd)) break;
      }
    }
    return List.copyOf(out);
  }
}

