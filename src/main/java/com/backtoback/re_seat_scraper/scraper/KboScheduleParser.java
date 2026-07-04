package com.backtoback.re_seat_scraper.scraper;

import com.microsoft.playwright.Locator;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class KboScheduleParser {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public List<RawGame> parse(List<Locator> rows, int season) {
        Map<String, Integer> countMap = new HashMap<>();
        List<RawGame> result = new ArrayList<>();
        LocalDate current = null;

        for (Locator row : rows) {
            Locator play = row.locator("td.play");
            if (play.count() == 0) continue;                        // 규칙2: 빈 행 스킵

            Locator day = row.locator("td.day");                    // 규칙1: 날짜 carry-over
            if (day.count() > 0) {
                String[] md = day.innerText().substring(0, 5).trim().split("\\.");
                current = LocalDate.of(season, Integer.parseInt(md[0]), Integer.parseInt(md[1]));
            }
            if (current == null) continue;

            LocalTime time = LocalTime.parse(row.locator("td.time").innerText().trim());
            List<String> teams = play.locator("> span").allInnerTexts();
            String away = KboTeam.fromShort(teams.get(0).trim()).fullName();
            String home = KboTeam.fromShort(teams.get(1).trim()).fullName();

            String base = current.format(FMT) + "-" + teams.get(0).trim() + "-" + teams.get(1).trim();
            int count = countMap.merge(base, 1, Integer::sum);      // 규칙3: 더블헤더

            List<Locator> rest = row.locator("td:not([class])").all();
            String stadium = rest.get(3).innerText().trim();

            result.add(new RawGame(current, time, away, home, stadium, base + "-" + count));
        }
        return result;
    }
}
