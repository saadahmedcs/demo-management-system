package com.demo.backend.user;

import com.demo.backend.user.UserRoleService.RegisterRequest;
import com.demo.backend.user.UserRoleService.UserRoleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173", "http://localhost:3000"})
public class UserRoleController {
  private final UserRoleService service;

  @PostMapping("/register")
  public UserRoleDto register(@RequestBody RegisterRequest req) {
    return service.register(req.email(), req.role());
  }
}
