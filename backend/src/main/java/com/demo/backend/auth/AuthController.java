package com.demo.backend.auth;

import com.demo.backend.user.UserRoleService.UserRoleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173", "http://localhost:3000"})
public class AuthController {

  private final AuthService authService;

  @PostMapping("/create-account")
  @ResponseStatus(HttpStatus.CREATED)
  public void createAccount(@RequestBody CreateAccountRequest req) {
    authService.createAccount(
        req.email(), req.name(), req.rollNumber(), req.section(), req.password(), req.confirmPassword());
  }

  @PostMapping("/login")
  public UserRoleDto login(@RequestBody LoginRequest req) {
    return authService.login(req.email(), req.password(), req.role());
  }
}
