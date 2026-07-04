package com.backtoback.re_seat_scraper.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "scraper")
public class ScraperProperties {

    private Target target = new Target();
    private Collect collect = new Collect();
    private String outputDir = "./output";

    public List<YearMonth> targetMonths() {
        validateMonthRange(target.getStartMonth(), "수집 시작 월");
        validateMonthRange(target.getEndMonth(), "수집 종료 월");
        validateMonthRange(collect.getMonthFrom(), "수집 허용 시작 월");
        validateMonthRange(collect.getMonthTo(), "수집 허용 종료 월");
        if (collect.getMonthFrom() > collect.getMonthTo()) {
            throw new IllegalArgumentException("수집 허용 시작 월은 종료 월보다 클 수 없습니다.");
        }

        YearMonth start = YearMonth.of(target.getStartSeason(), target.getStartMonth());
        YearMonth end = YearMonth.of(target.getEndSeason(), target.getEndMonth());
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("수집 시작 연월은 종료 연월보다 늦을 수 없습니다.");
        }

        List<YearMonth> months = new ArrayList<>();
        for (YearMonth cursor = start; !cursor.isAfter(end); cursor = cursor.plusMonths(1)) {
            int month = cursor.getMonthValue();
            if (month >= collect.getMonthFrom() && month <= collect.getMonthTo()) {
                months.add(cursor);
            }
        }
        return months;
    }

    private void validateMonthRange(int month, String label) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(label + "은 1~12 사이여야 합니다.");
        }
    }

    @Getter
    @Setter
    public static class Target {
        private int startSeason = 2026;
        private int startMonth = 7;
        private int endSeason = 2026;
        private int endMonth = 7;
    }

    @Getter
    @Setter
    public static class Collect {
        private boolean enabled = true;
        private int monthFrom = 3;
        private int monthTo = 10;
    }
}
