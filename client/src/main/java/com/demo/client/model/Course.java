package com.demo.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Course {
  private final UUID id;
  private String code;
  private String name;
  private String taEmail;
  private String venue;
  private String rubricFilename;
  private String enrollmentCode;
  private final List<DemoSlot> slots = new ArrayList<>();

  public Course(UUID id, String code, String name) {
    this(id, code, name, null, null, null, null);
  }

  public Course(UUID id, String code, String name, String taEmail) {
    this(id, code, name, taEmail, null, null, null);
  }

  public Course(UUID id, String code, String name, String taEmail, String venue, String rubricFilename) {
    this(id, code, name, taEmail, venue, rubricFilename, null);
  }

  public Course(UUID id, String code, String name, String taEmail, String venue, String rubricFilename, String enrollmentCode) {
    this.id = id;
    this.code = code;
    this.name = name;
    this.taEmail = taEmail;
    this.venue = venue;
    this.rubricFilename = rubricFilename;
    this.enrollmentCode = enrollmentCode;
  }

  public UUID getId() { return id; }
  public String getCode() { return code; }
  public void setCode(String code) { this.code = code; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getTaEmail() { return taEmail; }
  public String getVenue() { return venue; }
  public void setVenue(String venue) { this.venue = venue; }
  public String getRubricFilename() { return rubricFilename; }
  public void setRubricFilename(String rubricFilename) { this.rubricFilename = rubricFilename; }
  public String getEnrollmentCode() { return enrollmentCode; }
  public List<DemoSlot> getSlots() { return slots; }

  public String displayName() {
    if (code == null || code.isBlank()) return name;
    if (name == null || name.isBlank()) return code;
    return code + " — " + name;
  }
}
