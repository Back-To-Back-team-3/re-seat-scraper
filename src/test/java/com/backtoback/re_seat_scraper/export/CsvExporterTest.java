package com.backtoback.re_seat_scraper.export;

import com.backtoback.re_seat_scraper.config.ScraperProperties;
import com.backtoback.re_seat_scraper.domain.Game;
import com.backtoback.re_seat_scraper.domain.Stadium;
import com.backtoback.re_seat_scraper.domain.Team;
import com.backtoback.re_seat_scraper.repository.GameRepository;
import com.backtoback.re_seat_scraper.repository.StadiumRepository;
import com.backtoback.re_seat_scraper.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CsvExporterTest {

    @TempDir
    Path tempDir;

    @Test
    void should_exportOnlySelectedMonths_when_databaseHasOtherMonthGames() throws Exception {
        StadiumRepository stadiumRepo = mock(StadiumRepository.class);
        TeamRepository teamRepo = mock(TeamRepository.class);
        GameRepository gameRepo = mock(GameRepository.class);
        ScraperProperties properties = new ScraperProperties();
        properties.setOutputDir(tempDir.toString());

        Stadium stadium = stadium(1L);
        Team home = team(10L, "홈팀", stadium);
        Team away = team(20L, "원정팀", stadium);

        Game julyGame = game("20260701-A-B-1", home, away, stadium, LocalDateTime.of(2026, 7, 1, 18, 30));
        Game augustGame = game("20260801-A-B-1", home, away, stadium, LocalDateTime.of(2026, 8, 1, 18, 30));
        Game septemberGame = game("20260901-A-B-1", home, away, stadium, LocalDateTime.of(2026, 9, 1, 18, 30));

        when(stadiumRepo.findAll()).thenReturn(List.of(stadium));
        when(teamRepo.findAll()).thenReturn(List.of(home, away));
        when(gameRepo.findByGameAtGreaterThanEqualAndGameAtLessThanOrderByGameAtAsc(
                YearMonth.of(2026, 7).atDay(1).atStartOfDay(),
                YearMonth.of(2026, 9).plusMonths(1).atDay(1).atStartOfDay()
        )).thenReturn(List.of(julyGame, augustGame, septemberGame));

        CsvExporter exporter = new CsvExporter(stadiumRepo, teamRepo, gameRepo, properties);

        exporter.exportAll(List.of(YearMonth.of(2026, 7), YearMonth.of(2026, 9)));

        String gamesCsv = Files.readString(tempDir.resolve("games.csv"));
        assertThat(gamesCsv)
                .contains("20260701-A-B-1")
                .contains("20260901-A-B-1")
                .doesNotContain("20260801-A-B-1");
    }

    private Stadium stadium(Long id) {
        Stadium stadium = Stadium.builder()
                .name("테스트구장")
                .address("서울")
                .totalCapacity(10000)
                .build();
        ReflectionTestUtils.setField(stadium, "id", id);
        return stadium;
    }

    private Team team(Long id, String name, Stadium stadium) {
        Team team = Team.builder()
                .name(name)
                .homeStadium(stadium)
                .build();
        ReflectionTestUtils.setField(team, "id", id);
        return team;
    }

    private Game game(String gameKey, Team home, Team away, Stadium stadium, LocalDateTime gameAt) {
        return Game.builder()
                .gameKey(gameKey)
                .homeTeam(home)
                .awayTeam(away)
                .stadium(stadium)
                .gameAt(gameAt)
                .title(away.getName() + " vs " + home.getName())
                .build();
    }
}
