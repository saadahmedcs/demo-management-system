package com.demo.backend.course.dto;

import java.util.UUID;

public record CourseDto(UUID id, String code, String name, String taEmail, String venue, String rubricFilename, String enrollmentCode) {}

