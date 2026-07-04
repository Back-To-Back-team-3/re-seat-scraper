package com.backtoback.re_seat_scraper.runner;

import com.backtoback.re_seat_scraper.config.ScraperProperties;
import com.backtoback.re_seat_scraper.export.ReSeatCsvExporter;
import com.backtoback.re_seat_scraper.service.ScheduleCollectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

@Slf4j
@Order(2)
@Component
@ConditionalOnProperty(prefix = "scraper.collect", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class DevCollectRunner implements ApplicationRunner {

    private final ScraperProperties scraperProperties;
    private final ScheduleCollectService collectService;
    private final ReSeatCsvExporter csvExporter;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<YearMonth> targetMonths = scraperProperties.targetMonths();

        // 설정된 시작/종료 연월 사이에서 수집 대상 월만 순서대로 실행한다.
        for (YearMonth targetMonth : targetMonths) {
            String result = collectService.collect(targetMonth);
            log.info("[RUNNER] targetMonth={} {}", targetMonth, result);
        }
        csvExporter.exportAll(targetMonths);
    }
}
