package com.demo.client.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageDto(UUID id, UUID courseId, String senderEmail, String text, LocalDateTime sentAt) {}
