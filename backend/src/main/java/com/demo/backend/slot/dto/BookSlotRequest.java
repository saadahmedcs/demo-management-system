package com.demo.backend.slot.dto;

public record BookSlotRequest(
    String studentEmail, Integer groupMemberCount, java.util.List<String> groupMemberRollNumbers) {}
