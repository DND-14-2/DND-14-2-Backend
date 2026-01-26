package com.example.demo.application.dto;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

public record DateRange(LocalDate start, LocalDate end) {
    private static final Clock CLOCK = Clock.system(ZoneId.of("Asia/Seoul"));

    public static DateRange resolve(LocalDate start, LocalDate end) {
        if (start == null && end == null) {
            return currentMonth();
        }
        if (start == null) {
            return monthStartTo(end);
        }
        if (end == null) {
            return startToMonthEnd(start);
        }
        return between(start, end);
    }

    /**
     * today가 속한 월 전체
     */
    private static DateRange currentMonth() {
        LocalDate today = LocalDate.now(CLOCK);
        return monthContaining(today);
    }

    /**
     * 주어진 날짜가 속한 월 전체
     */
    private static DateRange monthContaining(LocalDate date) {
        YearMonth ym = YearMonth.from(date);
        return new DateRange(ym.atDay(1), ym.atEndOfMonth());
    }

    /**
     * end가 속한 월의 1일 ~ end
     */
    private static DateRange monthStartTo(LocalDate end) {
        YearMonth ym = YearMonth.from(end);
        return new DateRange(ym.atDay(1), end);
    }

    /**
     * start ~ start가 속한 월 말일
     */
    private static DateRange startToMonthEnd(LocalDate start) {
        YearMonth ym = YearMonth.from(start);
        return new DateRange(start, ym.atEndOfMonth());
    }

    /**
     * start ~ end 둘 다 존재
     */
    private static DateRange between(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("start는 end보다 클 수 없습니다.");
        }
        return new DateRange(start, end);
    }
}