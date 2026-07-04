package com.backtoback.re_seat_scraper.runner;

import com.backtoback.re_seat_scraper.config.ScraperProperties;
import com.backtoback.re_seat_scraper.export.ReSeatCsvExporter;
import com.backtoback.re_seat_scraper.repository.StadiumRepository;
import com.backtoback.re_seat_scraper.repository.TeamRepository;
import com.backtoback.re_seat_scraper.seed.ReferenceDataSeeder;
import com.backtoback.re_seat_scraper.service.ScheduleCollectService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RunnerConditionTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Test
    void should_createBothRunners_when_seedAndCollectAreEnabled() {
        contextRunner
                .withPropertyValues(
                        "scraper.seed.enabled=true",
                        "scraper.collect.enabled=true"
                )
                .run(context -> assertThat(context)
                        .hasSingleBean(ReferenceDataSeeder.class)
                        .hasSingleBean(DevCollectRunner.class));
    }

    @Test
    void should_disableSeederOnly_when_seedIsDisabled() {
        contextRunner
                .withPropertyValues(
                        "scraper.seed.enabled=false",
                        "scraper.collect.enabled=true"
                )
                .run(context -> assertThat(context)
                        .doesNotHaveBean(ReferenceDataSeeder.class)
                        .hasSingleBean(DevCollectRunner.class));
    }

    @Test
    void should_disableCollectorOnly_when_collectIsDisabled() {
        contextRunner
                .withPropertyValues(
                        "scraper.seed.enabled=true",
                        "scraper.collect.enabled=false"
                )
                .run(context -> assertThat(context)
                        .hasSingleBean(ReferenceDataSeeder.class)
                        .doesNotHaveBean(DevCollectRunner.class));
    }

    @Configuration
    @Import({
            ReferenceDataSeeder.class,
            DevCollectRunner.class,
            ScraperProperties.class
    })
    static class TestConfig {

        @Bean
        StadiumRepository stadiumRepository() {
            return mock(StadiumRepository.class);
        }

        @Bean
        TeamRepository teamRepository() {
            return mock(TeamRepository.class);
        }

        @Bean
        ScheduleCollectService scheduleCollectService() {
            return mock(ScheduleCollectService.class);
        }

        @Bean
        ReSeatCsvExporter csvExporter() {
            return mock(ReSeatCsvExporter.class);
        }
    }
}
