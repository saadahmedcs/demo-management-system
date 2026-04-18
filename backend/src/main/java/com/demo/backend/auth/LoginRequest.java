package com.demo.backend.auth;

public record LoginRequest(String email, String password, String role) {}
