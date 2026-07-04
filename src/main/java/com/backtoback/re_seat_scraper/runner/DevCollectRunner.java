package com.backtoback.re_seat_scraper.runner;

import com.backtoback.re_seat_scraper.export.CsvExporter;
import com.backtoback.re_seat_scraper.service.ScheduleCollectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Slf4j
@Order(2)
@Component
@ConditionalOnProperty(prefix = "scraper.collect", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class DevCollectRunner implements ApplicationRunner {

    private static final int TARGET_SEASON = 2026;
    private static final int TARGET_MONTH = 7;

    private final ScheduleCollectService collectService;
    private final CsvExporter csvExporter;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String result = collectService.collect(TARGET_SEASON, TARGET_MONTH);
        log.info("[RUNNER] {}-{} {}", TARGET_SEASON, TARGET_MONTH, result);
        csvExporter.exportAll(YearMonth.of(TARGET_SEASON, TARGET_MONTH));
    }
}
