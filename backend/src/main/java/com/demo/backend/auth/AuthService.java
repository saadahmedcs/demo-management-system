package com.demo.backend.auth;

import com.demo.backend.user.AppUser;
import com.demo.backend.user.AppUserRepository;
import com.demo.backend.user.PasswordHasher;
import com.demo.backend.user.UserRoleService;
import com.demo.backend.user.UserRoleService.UserRoleDto;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private static final Pattern EMAIL_PATTERN = Pattern.compile("i\\d{6}@nu\\.edu\\.pk");
  private static final int MIN_PASSWORD_LENGTH = 8;

  private final AppUserRepository appUserRepository;
  private final UserRoleService userRoleService;

  @Transactional
  public void createAccount(
      String email, String name, String rollNumber, String section, String password, String confirmPassword) {
    if (email == null || email.isBlank()) throw new IllegalArgumentException("Email required.");
    email = email.trim().toLowerCase();
    if (!EMAIL_PATTERN.matcher(email).matches()) {
      throw new IllegalArgumentException(
          "Enter a valid university email (e.g. i220928@nu.edu.pk).");
    }
    if (name == null || name.isBlank()) throw new IllegalArgumentException("Name required.");
    if (rollNumber == null || rollNumber.isBlank()) {
      throw new IllegalArgumentException("Roll number required.");
    }
    name = name.trim();
    rollNumber = rollNumber.trim();
    section = section == null ? null : section.trim();
    if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
      throw new IllegalArgumentException("Password must be at least 8 characters.");
    }
    if (confirmPassword == null || !password.equals(confirmPassword)) {
      throw new IllegalArgumentException("Passwords do not match.");
    }
    if (appUserRepository.existsById(email)) {
      throw new IllegalArgumentException("An account with this email already exists.");
    }

    String hash = PasswordHasher.sha256Hex(password);
    appUserRepository.save(new AppUser(email, name, rollNumber, section, hash));
  }

  @Transactional
  public UserRoleDto login(String email, String password, String role) {
    if (email == null || email.isBlank()) throw new IllegalArgumentException("Email required.");
    email = email.trim().toLowerCase();
    if (!EMAIL_PATTERN.matcher(email).matches()) {
      throw new IllegalArgumentException(
          "Enter a valid university email (e.g. i220928@nu.edu.pk).");
    }
    if (password == null || password.isBlank()) {
      throw new IllegalArgumentException("Password required.");
    }

    var user =
        appUserRepository
            .findById(email)
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
    if (!PasswordHasher.matches(password, user.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid email or password.");
    }

    return userRoleService.register(email, role);
  }
}
