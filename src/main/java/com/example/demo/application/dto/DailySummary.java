package com.example.demo.application.dto;

import java.time.LocalDate;

public record DailySummary(
    LocalDate date,
    long incomeTotal,
    long expenseTotal
) {
    public static DailySummary of(LocalDate date, DailySumRow row) {
        long income = row.incomeTotal();
        long expense = row.expenseTotal();
        return new DailySummary(date, income, expense);
    }

    public static DailySummary of(LocalDate date) {
        return new DailySummary(date, 0L, 0L);
    }
}
