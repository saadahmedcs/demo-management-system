package com.demo.backend.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_roles")
@Getter
@Setter
@NoArgsConstructor
public class UserRole {
  /** Email is the primary key — one role per email address. */
  @Id private String email;

  @Column(nullable = false)
  private String role; // "STUDENT" or "TA"

  public UserRole(String email, String role) {
    this.email = email;
    this.role = role;
  }
}
