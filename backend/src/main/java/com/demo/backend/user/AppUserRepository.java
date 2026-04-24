package com.demo.backend.user;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, String> {

  List<AppUser> findByRollNumberEqualsIgnoreCase(String rollNumber);
}
