package com.example.demo.application.dto;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateRangeTest {
    private static final Clock CLOCK = Clock.system(ZoneId.of("Asia/Seoul"));

    @Test
    void start와_end가_모두_null이면_이번_달_1일에서_말일까지를_반환한다() {
        // given
        LocalDate today = LocalDate.now(CLOCK);
        YearMonth ym = YearMonth.from(today);

        // when
        DateRange range = DateRange.resolve(CLOCK, null, null);

        // then
        assertThat(range.start()).isEqualTo(ym.atDay(1));
        assertThat(range.end()).isEqualTo(ym.atEndOfMonth());
    }

    @Test
    void start가_null이면_end가_속한_달의_1일부터_end까지를_반환한다() {
        // given
        LocalDate end = LocalDate.of(2025, 12, 10);
        YearMonth ym = YearMonth.from(end);

        // when
        DateRange range = DateRange.resolve(CLOCK, null, end);

        // then
        assertThat(range.start()).isEqualTo(ym.atDay(1));
        assertThat(range.end()).isEqualTo(end);
    }

    @Test
    void end가_null이면_start부터_start가_속한_달의_말일까지를_반환한다() {
        // given
        LocalDate start = LocalDate.of(2025, 12, 10);
        YearMonth ym = YearMonth.from(start);

        // when
        DateRange range = DateRange.resolve(CLOCK, start, null);

        // then
        assertThat(range.start()).isEqualTo(start);
        assertThat(range.end()).isEqualTo(ym.atEndOfMonth());
    }

    @Test
    void start가_end보다_미래면_IllegalArgumentException을_던진다() {
        // given
        LocalDate start = LocalDate.of(2025, 12, 10);
        LocalDate end = LocalDate.of(2025, 12, 1);

        // when & then
        assertThatThrownBy(() -> DateRange.resolve(CLOCK, start, end))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("start는 end보다 클 수 없습니다.");
    }

    @Test
    void start와_end가_모두_있으면_그대로_DateRange를_반환한다() {
        // given
        LocalDate start = LocalDate.of(2025, 12, 1);
        LocalDate end = LocalDate.of(2025, 12, 10);

        // when
        DateRange range = DateRange.resolve(CLOCK, start, end);

        // then
        assertThat(range.start()).isEqualTo(start);
        assertThat(range.end()).isEqualTo(end);
    }
}