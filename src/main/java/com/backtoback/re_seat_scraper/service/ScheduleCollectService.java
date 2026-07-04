package com.backtoback.re_seat_scraper.service;

import com.backtoback.re_seat_scraper.domain.Game;
import com.backtoback.re_seat_scraper.domain.Stadium;
import com.backtoback.re_seat_scraper.domain.Team;
import com.backtoback.re_seat_scraper.repository.GameRepository;
import com.backtoback.re_seat_scraper.repository.StadiumRepository;
import com.backtoback.re_seat_scraper.repository.TeamRepository;
import com.backtoback.re_seat_scraper.scraper.KboScheduleParser;
import com.backtoback.re_seat_scraper.scraper.KboStadium;
import com.backtoback.re_seat_scraper.scraper.RawGame;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleCollectService {

    private final KboScheduleParser parser;
    private final TeamRepository teamRepo;
    private final StadiumRepository stadiumRepo;
    private final GameRepository gameRepo;

    @Value("${kbo.url}") private String url;
    @Value("${kbo.selectors.year}") private String yearSelector;
    @Value("${kbo.selectors.month}") private String monthSelector;
    @Value("${kbo.selectors.series}") private String seriesSelector;
    @Value("${kbo.selectors.games-table}") private String gamesTableSelector;
    @Value("${kbo.series.regular-season}") private String regularSeasonSeries;

    @Transactional
    public String collect(int season, int month) {
        return collect(YearMonth.of(season, month));
    }

    @Transactional
    public String collect(YearMonth target) {
        List<RawGame> raws = scrape(target);
        log.info("[COLLECT] targetMonth={} rawCount={}", target, raws.size());

        List<Game> games = raws.stream()
                .map(this::toGame)
                .filter(Objects::nonNull)
                .toList();

        return upsert(games);
    }

    private List<RawGame> scrape(YearMonth target) {
        try (Playwright pw = Playwright.create();
             Browser browser = pw.chromium().launch()) {
            Page page = browser.newPage();
            page.navigate(url);

            page.selectOption(yearSelector, String.valueOf(target.getYear()));
            page.waitForTimeout(400);
            page.selectOption(monthSelector, "%02d".formatted(target.getMonthValue()));
            page.waitForTimeout(400);
            page.selectOption(seriesSelector, regularSeasonSeries);
            page.waitForTimeout(600);

            List<Locator> rows = page.locator(gamesTableSelector + " tr").all();
            List<RawGame> rawGames = parser.parse(rows, target.getYear()).stream()
                    // KBO 페이지가 선택 변경 전 행을 유지할 수 있으므로 요청한 월만 저장한다.
                    .filter(raw -> YearMonth.from(raw.date()).equals(target))
                    .toList();
            return rawGames;
        }
    }

    private Game toGame(RawGame r) {
        Team home = teamRepo.findByName(r.homeTeamName()).orElse(null);
        Team away = teamRepo.findByName(r.awayTeamName()).orElse(null);
        if (home == null || away == null) {
            log.warn("[SKIP] unmapped team: {} / {}", r.awayTeamName(), r.homeTeamName());
            return null;
        }

        Stadium stadium = KboStadium.resolve(r.stadiumShortName())
                .flatMap(stadiumRepo::findByName)
                .orElse(home.getHomeStadium());

        LocalDateTime gameAt = LocalDateTime.of(r.date(),
                r.time() != null ? r.time() : LocalTime.of(18, 30));

        return Game.builder()
                .gameKey(r.gameKey())
                .homeTeam(home)
                .awayTeam(away)
                .stadium(stadium)
                .gameAt(gameAt)
                .title(away.getName() + " vs " + home.getName())
                .build();
    }

    private String upsert(List<Game> games) {
        if (games.isEmpty()) {
            return "collected=0 saved=0 modified=0";
        }

        Map<String, Game> existing = gameRepo
                .findByGameKeyIn(games.stream().map(Game::getGameKey).toList())
                .stream()
                .collect(Collectors.toMap(Game::getGameKey, g -> g));

        int saved = 0;
        int modified = 0;
        for (Game g : games) {
            Game old = existing.get(g.getGameKey());
            if (old == null) {
                gameRepo.save(g);
                saved++;
            } else if (old.updateFrom(g)) {
                modified++;
            }
        }
        return "collected=%d saved=%d modified=%d".formatted(games.size(), saved, modified);
    }
}
