package com.demo.backend.message.dto;

public record SendMessageRequest(String senderEmail, String recipientEmail, String text) {}
