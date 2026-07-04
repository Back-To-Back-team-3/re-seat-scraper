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

class ReSeatCsvExporterTest {

    @TempDir
    Path tempDir;

    @Test
    void should_exportCsvContractHeadersAndStableReferenceOrder() throws Exception {
        StadiumRepository stadiumRepo = mock(StadiumRepository.class);
        TeamRepository teamRepo = mock(TeamRepository.class);
        GameRepository gameRepo = mock(GameRepository.class);
        ScraperProperties properties = new ScraperProperties();
        properties.setOutputDir(tempDir.toString());

        Stadium firstStadium = stadium(1L, "첫 번째 구장");
        Stadium secondStadium = stadium(2L, "두 번째 구장");
        Team firstTeam = team(10L, "첫 번째 팀", firstStadium);
        Team secondTeam = team(20L, "두 번째 팀", secondStadium);

        when(stadiumRepo.findAll()).thenReturn(List.of(secondStadium, firstStadium));
        when(teamRepo.findAll()).thenReturn(List.of(secondTeam, firstTeam));

        ReSeatCsvExporter exporter = new ReSeatCsvExporter(stadiumRepo, teamRepo, gameRepo, properties);

        exporter.exportAll(List.of());

        List<String> stadiumLines = Files.readAllLines(tempDir.resolve(ReSeatCsvSchema.STADIUMS_FILE));
        List<String> teamLines = Files.readAllLines(tempDir.resolve(ReSeatCsvSchema.TEAMS_FILE));
        List<String> gameLines = Files.readAllLines(tempDir.resolve(ReSeatCsvSchema.GAMES_FILE));

        assertThat(stadiumLines.get(0)).isEqualTo(ReSeatCsvSchema.STADIUMS_HEADER);
        assertThat(teamLines.get(0)).isEqualTo(ReSeatCsvSchema.TEAMS_HEADER);
        assertThat(stadiumLines).extracting(line -> line.split(",", 2)[0])
                .containsExactly(ReSeatCsvSchema.STADIUMS_HEADER.split(",", 2)[0], "1", "2");
        assertThat(teamLines).extracting(line -> line.split(",", 2)[0])
                .containsExactly(ReSeatCsvSchema.TEAMS_HEADER.split(",", 2)[0], "10", "20");
        assertThat(gameLines).containsExactly(ReSeatCsvSchema.GAMES_HEADER);
    }

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

        Game julyGame = game(100L, "20260701-A-B-1", home, away, stadium, LocalDateTime.of(2026, 7, 1, 18, 30));
        Game augustGame = game(200L, "20260801-A-B-1", home, away, stadium, LocalDateTime.of(2026, 8, 1, 18, 30));
        Game septemberGame = game(300L, "20260901-A-B-1", home, away, stadium, LocalDateTime.of(2026, 9, 1, 18, 30));

        when(stadiumRepo.findAll()).thenReturn(List.of(stadium));
        when(teamRepo.findAll()).thenReturn(List.of(home, away));
        when(gameRepo.findByGameAtGreaterThanEqualAndGameAtLessThanOrderByGameAtAsc(
                YearMonth.of(2026, 7).atDay(1).atStartOfDay(),
                YearMonth.of(2026, 9).plusMonths(1).atDay(1).atStartOfDay()
        )).thenReturn(List.of(julyGame, augustGame, septemberGame));

        ReSeatCsvExporter exporter = new ReSeatCsvExporter(stadiumRepo, teamRepo, gameRepo, properties);

        exporter.exportAll(List.of(YearMonth.of(2026, 7), YearMonth.of(2026, 9)));

        String gamesCsv = Files.readString(tempDir.resolve("games.csv"));
        assertThat(gamesCsv)
                .contains(ReSeatCsvSchema.GAMES_HEADER)
                .contains("100,10,20,1,2026-07-01 18:30:00,2026-06-24 18:30:00,2026-07-01 18:30:00,SCHEDULED")
                .contains("300,10,20,1,2026-09-01 18:30:00,2026-08-25 18:30:00,2026-09-01 18:30:00,SCHEDULED")
                .doesNotContain("2026-08-01 18:30:00")
                .doesNotContain("20260701-A-B-1");
    }

    private Stadium stadium(Long id) {
        return stadium(id, "테스트구장");
    }

    private Stadium stadium(Long id, String name) {
        Stadium stadium = Stadium.builder()
                .name(name)
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

    private Game game(Long id, String gameKey, Team home, Team away, Stadium stadium, LocalDateTime gameAt) {
        Game game = Game.builder()
                .gameKey(gameKey)
                .homeTeam(home)
                .awayTeam(away)
                .stadium(stadium)
                .gameAt(gameAt)
                .title(away.getName() + " vs " + home.getName())
                .build();
        ReflectionTestUtils.setField(game, "id", id);
        return game;
    }
}
