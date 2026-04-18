package com.demo.backend.auth;

public record CreateAccountRequest(
    String email, String name, String rollNumber, String section, String password, String confirmPassword) {}
