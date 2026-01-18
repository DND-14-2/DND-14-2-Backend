package com.example.demo.infrastructure.advice.dto;

public record ErrorResponse(
        String message,
        String timestamp
) {}
