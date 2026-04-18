package com.demo.client.api.dto;

public record BookSlotRequest(
    String studentEmail, Integer groupMemberCount, java.util.List<String> groupMemberRollNumbers) {}
