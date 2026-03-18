package com.demo.backend.course;

import com.demo.backend.slot.DemoSlot;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
public class Course {
  @Id private UUID id = UUID.randomUUID();

  @Column(nullable = false) private String name;

  @Column(nullable = true) private String code;

  @Column(nullable = true, length = 200)
  private String taEmail;

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<DemoSlot> slots = new ArrayList<>();

  public Course(String code, String name, String taEmail) {
    this.code = code;
    this.name = name;
    this.taEmail = taEmail;
  }
}

