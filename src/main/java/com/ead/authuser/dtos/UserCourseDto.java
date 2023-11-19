package com.ead.authuser.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserCourseDto(
        UUID userId,
        @NotNull
        UUID courseId
) {
}
