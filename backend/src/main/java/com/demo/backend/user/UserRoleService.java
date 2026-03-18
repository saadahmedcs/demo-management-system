package com.demo.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRoleService {
  private final UserRoleRepository repo;

  public record UserRoleDto(String email, String role) {}
  public record RegisterRequest(String email, String role) {}

  /**
   * Registers an email with a role on first login.
   * Subsequent logins with the same role succeed silently.
   * A conflicting role throws IllegalArgumentException.
   */
  @Transactional
  public UserRoleDto register(String email, String role) {
    if (email == null || email.isBlank()) throw new IllegalArgumentException("Email required.");
    if (!role.equals("STUDENT") && !role.equals("TA"))
      throw new IllegalArgumentException("Role must be STUDENT or TA.");

    return repo.findById(email)
        .map(existing -> {
          if (!existing.getRole().equals(role)) {
            String friendly = existing.getRole().equals("TA") ? "Teaching Assistant" : "Student";
            throw new IllegalArgumentException(
                "This email is already registered as " + friendly + ".");
          }
          return new UserRoleDto(existing.getEmail(), existing.getRole());
        })
        .orElseGet(() -> {
          var saved = repo.save(new UserRole(email, role));
          return new UserRoleDto(saved.getEmail(), saved.getRole());
        });
  }
}
