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
  private String assignmentName;
  private String studentEmail;
  private String studentRollNumber;
  private String studentSection;
  private Integer groupMemberCount;
  private String groupMemberRollNumbers;

  public DemoSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
    this(UUID.randomUUID(), date, startTime, endTime, null, "General", null, null, null, null, null);
  }

  public DemoSlot(UUID id, LocalDate date, LocalTime startTime, LocalTime endTime, String note) {
    this(id, date, startTime, endTime, note, "General", null, null, null, null, null);
  }

  public DemoSlot(
      UUID id,
      LocalDate date,
      LocalTime startTime,
      LocalTime endTime,
      String note,
      String assignmentName,
      String studentEmail,
      String studentRollNumber,
      String studentSection,
      Integer groupMemberCount,
      String groupMemberRollNumbers) {
    this.id = id;
    this.date = date;
    this.startTime = startTime;
    this.endTime = endTime;
    this.note = note;
    this.assignmentName = assignmentName;
    this.studentEmail = studentEmail;
    this.studentRollNumber = studentRollNumber;
    this.studentSection = studentSection;
    this.groupMemberCount = groupMemberCount;
    this.groupMemberRollNumbers = groupMemberRollNumbers;
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

  public String getStudentRollNumber() {
    return studentRollNumber;
  }

  public String getAssignmentName() {
    return assignmentName;
  }

  public String getStudentSection() {
    return studentSection;
  }

  public Integer getGroupMemberCount() {
    return groupMemberCount;
  }

  public String getGroupMemberRollNumbers() {
    return groupMemberRollNumbers;
  }
}

