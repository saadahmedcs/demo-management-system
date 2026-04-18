package com.demo.client.api.dto;

public record SendMessageRequest(String senderEmail, String recipientEmail, String text) {}
