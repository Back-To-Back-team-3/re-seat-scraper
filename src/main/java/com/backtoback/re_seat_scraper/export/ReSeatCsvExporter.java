package com.backtoback.re_seat_scraper.export;

import com.backtoback.re_seat_scraper.config.ScraperProperties;
import com.backtoback.re_seat_scraper.domain.Game;
import com.backtoback.re_seat_scraper.domain.Stadium;
import com.backtoback.re_seat_scraper.domain.Team;
import com.backtoback.re_seat_scraper.repository.GameRepository;
import com.backtoback.re_seat_scraper.repository.StadiumRepository;
import com.backtoback.re_seat_scraper.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReSeatCsvExporter {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StadiumRepository stadiumRepo;
    private final TeamRepository teamRepo;
    private final GameRepository gameRepo;
    private final ScraperProperties scraperProperties;

    public void exportAll() throws IOException {
        exportAll((List<YearMonth>) null);
    }

    public void exportAll(YearMonth targetMonth) throws IOException {
        exportAll(targetMonth == null ? null : List.of(targetMonth));
    }

    public void exportAll(List<YearMonth> targetMonths) throws IOException {
        Path dir = Paths.get(scraperProperties.getOutputDir());
        Files.createDirectories(dir);

        // 구장과 팀은 re-seat 기준데이터이므로 항상 전체를 출력한다.
        exportStadiums(dir.resolve(ReSeatCsvSchema.STADIUMS_FILE));
        exportTeams(dir.resolve(ReSeatCsvSchema.TEAMS_FILE));
        exportGames(dir.resolve(ReSeatCsvSchema.GAMES_FILE), targetMonths);

        log.info("[EXPORT] re-seat CSV exported for months={} to {}", targetMonths, dir.toAbsolutePath());
    }

    private void exportStadiums(Path path) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            w.write(ReSeatCsvSchema.STADIUMS_HEADER + "\n");
            for (var s : stadiumRepo.findAll().stream()
                    .sorted(Comparator.comparing(Stadium::getId))
                    .toList()) {
                w.write("%d,%s,%s,%d,%s%n".formatted(
                        s.getId(),
                        q(s.getName()),
                        q(s.getAddress()),
                        s.getTotalCapacity(),
                        ReSeatCsvSchema.ACTIVE_STATUS));
            }
        }
    }

    private void exportTeams(Path path) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            w.write(ReSeatCsvSchema.TEAMS_HEADER + "\n");
            for (var t : teamRepo.findAll().stream()
                    .sorted(Comparator.comparing(Team::getId))
                    .toList()) {
                w.write("%d,%s,%d,%s%n".formatted(
                        t.getId(),
                        q(t.getName()),
                        t.getHomeStadium().getId(),
                        ReSeatCsvSchema.ACTIVE_STATUS));
            }
        }
    }

    private void exportGames(Path path, List<YearMonth> targetMonths) throws IOException {
        List<Game> games = findGamesForExport(targetMonths);

        try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            w.write(ReSeatCsvSchema.GAMES_HEADER + "\n");
            for (Game g : games) {
                w.write("%d,%d,%d,%d,%s,%s,%s,%s,%s%n".formatted(
                        g.getId(),
                        g.getHomeTeam().getId(),
                        g.getAwayTeam().getId(),
                        g.getStadium().getId(),
                        format(g.getGameAt()),
                        format(defaultBookingOpenAt(g.getGameAt())),
                        format(g.getGameAt()),
                        ReSeatCsvSchema.SCHEDULED_BOOKING_STATUS,
                        q(g.getTitle())));
            }
        }
    }

    private List<Game> findGamesForExport(List<YearMonth> targetMonths) {
        if (targetMonths == null) {
            return gameRepo.findAll().stream()
                    .sorted(Comparator.comparing(Game::getGameAt).thenComparing(Game::getId))
                    .toList();
        }
        if (targetMonths.isEmpty()) {
            return List.of();
        }

        YearMonth start = targetMonths.get(0);
        YearMonth end = targetMonths.get(targetMonths.size() - 1);
        Set<YearMonth> selectedMonths = new HashSet<>(targetMonths);

        // 범위 조회 후 선택 월만 한 번 더 거르면, DB에 남아 있는 비수집 월이 CSV에 섞이지 않는다.
        return gameRepo.findByGameAtGreaterThanEqualAndGameAtLessThanOrderByGameAtAsc(
                        start.atDay(1).atStartOfDay(),
                        end.plusMonths(1).atDay(1).atStartOfDay())
                .stream()
                .filter(game -> selectedMonths.contains(YearMonth.from(game.getGameAt())))
                .toList();
    }

    private LocalDateTime defaultBookingOpenAt(LocalDateTime gameAt) {
        return gameAt.minusDays(7);
    }

    private String format(LocalDateTime dateTime) {
        return dateTime.format(FMT);
    }

    private String q(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
