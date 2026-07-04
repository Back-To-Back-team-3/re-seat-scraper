package com.backtoback.re_seat_scraper.service;

import com.backtoback.re_seat_scraper.domain.*;
import com.backtoback.re_seat_scraper.repository.*;
import com.backtoback.re_seat_scraper.scraper.*;
import com.microsoft.playwright.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
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
    @Value("${kbo.selectors.year}") private String yearSel;
    @Value("${kbo.selectors.month}") private String monthSel;
    @Value("${kbo.selectors.series}") private String seriesSel;
    @Value("${kbo.selectors.games-table}") private String tableSel;

    @Transactional
    public String collect(int season, int month) {
        YearMonth target = YearMonth.of(season, month);
        List<RawGame> raws;
        try (Playwright pw = Playwright.create()) {
            Browser browser = pw.chromium().launch();
            Page page = browser.newPage();
            page.navigate(url);
            page.selectOption(yearSel, String.valueOf(season));         page.waitForTimeout(400);
            page.selectOption(monthSel, String.format("%02d", month));  page.waitForTimeout(400);
            page.selectOption(seriesSel, "0,9,6");                      page.waitForTimeout(600);

            List<Locator> rows = page.locator(tableSel + " tr").all();
            raws = parser.parse(rows, season).stream()
                    .filter(raw -> YearMonth.from(raw.date()).equals(target))
                    .toList();
            browser.close();
        }
        log.info("[COLLECT] season={} month={} rawCount={}", season, month, raws.size());

        List<Game> games = raws.stream().map(this::toGame).filter(Objects::nonNull).toList();
        return upsert(games);
    }

    private Game toGame(RawGame r) {
        Team home = teamRepo.findByName(r.homeTeamName()).orElse(null);
        Team away = teamRepo.findByName(r.awayTeamName()).orElse(null);
        if (home == null || away == null) {
            log.warn("[SKIP] 미매핑 팀: {} / {}", r.awayTeamName(), r.homeTeamName());
            return null;
        }
        Stadium stadium = KboStadium.resolve(r.stadiumShortName())
                .flatMap(stadiumRepo::findByName)
                .orElse(home.getHomeStadium());

        LocalDateTime gameAt = LocalDateTime.of(r.date(),
                r.time() != null ? r.time() : LocalTime.of(18, 30));

        return Game.builder()
                .gameKey(r.gameKey())
                .homeTeam(home).awayTeam(away).stadium(stadium)
                .gameAt(gameAt)
                .title(away.getName() + " vs " + home.getName())
                .build();
    }

    private String upsert(List<Game> games) {
        Map<String, Game> existing = gameRepo
                .findByGameKeyIn(games.stream().map(Game::getGameKey).toList())
                .stream().collect(Collectors.toMap(Game::getGameKey, g -> g));

        int saved = 0, modified = 0;
        for (Game g : games) {
            Game old = existing.get(g.getGameKey());
            if (old == null) { gameRepo.save(g); saved++; }
            else if (old.updateFrom(g)) modified++;
        }
        return "collected=%d saved=%d modified=%d".formatted(games.size(), saved, modified);
    }
}
