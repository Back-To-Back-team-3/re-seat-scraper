package com.backtoback.re_seat_scraper.scraper;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.time.YearMonth;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class KboScheduleParserTest {

    @Test
    void should_returnGames_when_parsingScheduledHtml() {
        try (Playwright pw = Playwright.create()) {
            Browser browser = pw.chromium().launch();
            Page page = browser.newPage();
            String path = new File("src/test/resources/scheduled-games.html").getAbsolutePath();
            page.navigate("file:///" + path.replace("\\", "/"));

            List<Locator> rows = page.locator("#tblScheduleList > tbody tr").all();
            List<RawGame> games = new KboScheduleParser().parse(rows, 2026);

            assertThat(games).isNotEmpty();
            assertThat(games)
                    .allMatch(game -> YearMonth.from(game.date()).equals(YearMonth.of(2026, 7)));
            games.forEach(System.out::println);
            browser.close();
        }
    }
}
