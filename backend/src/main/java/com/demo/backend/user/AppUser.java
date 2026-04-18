package com.demo.backend.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

  @Id private String email;

  @Column(nullable = false)
  private String name;

  @Column(name = "roll_number", nullable = false)
  private String rollNumber;

  @Column(nullable = true, length = 30)
  private String section;

  @Column(name = "password_hash", nullable = false, length = 64)
  private String passwordHash;

  public AppUser(String email, String name, String rollNumber, String section, String passwordHash) {
    this.email = email;
    this.name = name;
    this.rollNumber = rollNumber;
    this.section = section;
    this.passwordHash = passwordHash;
  }
}
