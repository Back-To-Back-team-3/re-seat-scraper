package com.backtoback.re_seat_scraper.scraper.parser;

import com.backtoback.re_seat_scraper.scraper.dto.RawGame;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class KboScheduleParserTest {

    @Test
    void should_returnGames_when_parsingScheduledHtml() {
        try (Playwright pw = Playwright.create()) {
            Browser browser = pw.chromium().launch();
            List<Locator> rows = rowsFromFixture(browser, "scheduled-games.html");
            List<RawGame> games = new KboScheduleParser().parse(rows, 2026);

            assertThat(games).isNotEmpty();
            assertThat(games)
                    .allMatch(game -> YearMonth.from(game.date()).equals(YearMonth.of(2026, 7)));
            games.forEach(System.out::println);
            browser.close();
        }
    }

    @Test
    void should_suffixGameKey_when_parsingDoubleHeader() {
        try (Playwright pw = Playwright.create()) {
            Browser browser = pw.chromium().launch();
            List<Locator> rows = rowsFromFixture(browser, "double-header-games.html");
            List<RawGame> games = new KboScheduleParser().parse(rows, 2026);

            assertThat(games).hasSize(2);
            assertThat(games)
                    .extracting(RawGame::gameKey)
                    .containsExactly("20260712-LG-두산-1", "20260712-LG-두산-2");
            assertThat(games)
                    .extracting(RawGame::time)
                    .containsExactly(LocalTime.of(14, 0), LocalTime.of(18, 0));
            browser.close();
        }
    }

    private List<Locator> rowsFromFixture(Browser browser, String fixtureName) {
        Page page = browser.newPage();
        String path = new File("src/test/resources/" + fixtureName).getAbsolutePath();
        page.navigate("file:///" + path.replace("\\", "/"));
        return page.locator("#tblScheduleList > tbody tr").all();
    }
}
