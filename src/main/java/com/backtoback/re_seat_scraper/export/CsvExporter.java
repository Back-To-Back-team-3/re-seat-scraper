package com.backtoback.re_seat_scraper.export;

import com.backtoback.re_seat_scraper.domain.Game;
import com.backtoback.re_seat_scraper.repository.GameRepository;
import com.backtoback.re_seat_scraper.repository.StadiumRepository;
import com.backtoback.re_seat_scraper.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsvExporter {

    private final StadiumRepository stadiumRepo;
    private final TeamRepository teamRepo;
    private final GameRepository gameRepo;

    @Value("${scraper.output-dir}") private String outputDir;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void exportAll() throws IOException {
        exportAll(null);
    }

    public void exportAll(YearMonth targetMonth) throws IOException {
        Path dir = Paths.get(outputDir);
        Files.createDirectories(dir);

        exportStadiums(dir.resolve("stadiums.csv"));
        exportTeams(dir.resolve("teams.csv"));
        exportGames(dir.resolve("games.csv"), targetMonth);

        log.info("[EXPORT] CSV exported{} to {}",
                targetMonth != null ? " for " + targetMonth : "",
                dir.toAbsolutePath());
    }

    private void exportStadiums(Path path) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            w.write("id,name,address,total_capacity\n");
            for (var s : stadiumRepo.findAll()) {
                w.write("%d,%s,%s,%d%n".formatted(
                        s.getId(), q(s.getName()), q(s.getAddress()), s.getTotalCapacity()));
            }
        }
    }

    private void exportTeams(Path path) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            w.write("id,name,home_stadium_id\n");
            for (var t : teamRepo.findAll()) {
                w.write("%d,%s,%d%n".formatted(
                        t.getId(), q(t.getName()), t.getHomeStadium().getId()));
            }
        }
    }

    private void exportGames(Path path, YearMonth targetMonth) throws IOException {
        List<Game> games = targetMonth == null
                ? gameRepo.findAll()
                : gameRepo.findByGameAtGreaterThanEqualAndGameAtLessThanOrderByGameAtAsc(
                        targetMonth.atDay(1).atStartOfDay(),
                        targetMonth.plusMonths(1).atDay(1).atStartOfDay());

        try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            w.write("game_key,home_team_id,away_team_id,stadium_id,game_at,title\n");
            for (Game g : games) {
                w.write("%s,%d,%d,%d,%s,%s%n".formatted(
                        g.getGameKey(),
                        g.getHomeTeam().getId(),
                        g.getAwayTeam().getId(),
                        g.getStadium().getId(),
                        g.getGameAt().format(FMT),
                        q(g.getTitle())));
            }
        }
    }

    private String q(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
