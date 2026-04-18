package com.demo.backend.user;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class PasswordHasher {

  private PasswordHasher() {}

  public static String sha256Hex(String plainText) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(plainText.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  public static boolean matches(String plainText, String storedHex) {
    if (storedHex == null || plainText == null) return false;
    try {
      return MessageDigest.isEqual(
          HexFormat.of().parseHex(sha256Hex(plainText)),
          HexFormat.of().parseHex(storedHex));
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
