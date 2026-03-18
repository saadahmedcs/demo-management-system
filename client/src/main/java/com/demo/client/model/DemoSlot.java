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
  private String studentEmail;

  public DemoSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
    this(UUID.randomUUID(), date, startTime, endTime, null, null);
  }

  public DemoSlot(UUID id, LocalDate date, LocalTime startTime, LocalTime endTime, String note) {
    this(id, date, startTime, endTime, note, null);
  }

  public DemoSlot(UUID id, LocalDate date, LocalTime startTime, LocalTime endTime, String note, String studentEmail) {
    this.id = id;
    this.date = date;
    this.startTime = startTime;
    this.endTime = endTime;
    this.note = note;
    this.studentEmail = studentEmail;
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

  public String getStudentEmail() {
    return studentEmail;
  }

  public void setStudentEmail(String studentEmail) {
    this.studentEmail = studentEmail;
  }
}

