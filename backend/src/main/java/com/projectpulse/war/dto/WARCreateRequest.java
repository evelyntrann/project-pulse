package com.projectpulse.war.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record WARCreateRequest(@NotNull LocalDate weekStartDate) {}
