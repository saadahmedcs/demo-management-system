package com.demo.client.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class DemoSlot {
  private final UUID id;
  private final LocalDate date;
  private final LocalTime startTime;
  private final LocalTime endTime;
  private String note;

  public DemoSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
    this(UUID.randomUUID(), date, startTime, endTime, null);
  }

  public DemoSlot(UUID id, LocalDate date, LocalTime startTime, LocalTime endTime, String note) {
    this.id = id;
    this.date = date;
    this.startTime = startTime;
    this.endTime = endTime;
    this.note = note;
  }

  public UUID getId() {
    return id;
  }

  public LocalDate getDate() {
    return date;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }
}

