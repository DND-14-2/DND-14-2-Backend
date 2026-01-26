package com.example.demo.application;

import com.example.demo.application.dto.DateRange;
import com.example.demo.util.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateRangeResolverTest extends AbstractIntegrationTest {
    private final DateRangeResolver resolver = new DateRangeResolver();
    private static final ZoneId CLOCK = ZoneId.of("Asia/Seoul");

    @Test
    void start와_end가_모두_null이면_이번_달_1일에서_말일까지를_반환한다() {
        // given
        LocalDate today = LocalDate.now(CLOCK);
        YearMonth ym = YearMonth.from(today);

        // when
        DateRange range = DateRange.resolve(null, null);

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
        DateRange range = DateRange.resolve(null, end);

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
        DateRange range = DateRange.resolve(start, null);

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
        assertThatThrownBy(() -> DateRange.resolve(start, end))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("start는 end보다 클 수 없습니다.");
    }

    @Test
    void start와_end가_모두_있으면_그대로_DateRange를_반환한다() {
        // given
        LocalDate start = LocalDate.of(2025, 12, 1);
        LocalDate end = LocalDate.of(2025, 12, 10);

        // when
        DateRange range = DateRange.resolve(start, end);

        // then
        assertThat(range.start()).isEqualTo(start);
        assertThat(range.end()).isEqualTo(end);
    }

    @Test
    void resolveDate에_date가_있으면_그대로_반환한다() {
        // given
        LocalDate date = LocalDate.of(2026, 1, 24);

        // when
        LocalDate result = resolver.resolveDate(date);

        // then
        assertThat(result).isEqualTo(date);
    }

    @Test
    void resolveDate에_date가_null이면_오늘_날짜를_반환한다() {
        // when
        LocalDate result = resolver.resolveDate(null);

        // then
        assertThat(result).isEqualTo(LocalDate.now(CLOCK));
    }
}