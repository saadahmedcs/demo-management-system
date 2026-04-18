package com.demo.backend.mark.dto;

import java.util.UUID;

public record MarkDto(
    UUID id,
    UUID courseId,
    UUID slotId,
    String studentEmail,
    Double score,
    String feedback) {}
