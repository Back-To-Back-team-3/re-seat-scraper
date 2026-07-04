package com.backtoback.re_seat_scraper.config;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class ScraperPropertiesTest {

    @Test
    void should_returnSingleMonth_when_startAndEndAreSameMonth() {
        ScraperProperties properties = new ScraperProperties();

        properties.getTarget().setStartSeason(2026);
        properties.getTarget().setStartMonth(7);
        properties.getTarget().setEndSeason(2026);
        properties.getTarget().setEndMonth(7);

        assertThat(properties.targetMonths())
                .containsExactly(YearMonth.of(2026, 7));
    }

    @Test
    void should_returnSeasonMonthsForEachYear_when_rangeSpansMultipleYears() {
        ScraperProperties properties = new ScraperProperties();

        properties.getTarget().setStartSeason(2025);
        properties.getTarget().setStartMonth(3);
        properties.getTarget().setEndSeason(2026);
        properties.getTarget().setEndMonth(10);

        assertThat(properties.targetMonths())
                .containsExactly(
                        YearMonth.of(2025, 3),
                        YearMonth.of(2025, 4),
                        YearMonth.of(2025, 5),
                        YearMonth.of(2025, 6),
                        YearMonth.of(2025, 7),
                        YearMonth.of(2025, 8),
                        YearMonth.of(2025, 9),
                        YearMonth.of(2025, 10),
                        YearMonth.of(2026, 3),
                        YearMonth.of(2026, 4),
                        YearMonth.of(2026, 5),
                        YearMonth.of(2026, 6),
                        YearMonth.of(2026, 7),
                        YearMonth.of(2026, 8),
                        YearMonth.of(2026, 9),
                        YearMonth.of(2026, 10));
    }
}
