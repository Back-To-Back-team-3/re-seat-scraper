package com.backtoback.re_seat_scraper.scraper.parser;

import com.backtoback.re_seat_scraper.scraper.dto.RawGame;
import com.backtoback.re_seat_scraper.scraper.enums.KboTeam;
import com.microsoft.playwright.Locator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class KboScheduleParser {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * KBO 일정 테이블 행을 RawGame으로 변환한다.
     * KBO 테이블은 하루의 첫 행에만 날짜가 있으므로 이후 행은 직전 날짜를 이어서 사용한다.
     */
    public List<RawGame> parse(List<Locator> rows, int season) {
        Map<String, Integer> countMap = new HashMap<>();
        List<RawGame> result = new ArrayList<>();
        LocalDate current = null;

        for (Locator row : rows) {
            Locator play = row.locator("td.play");
            if (play.count() == 0) continue;

            Locator day = row.locator("td.day");
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
            int count = countMap.merge(base, 1, Integer::sum); // 더블헤더는 날짜와 팀이 같으므로 순번을 붙인다.

            List<Locator> rest = row.locator("td:not([class])").all();
            String stadium = rest.get(3).innerText().trim();

            result.add(new RawGame(current, time, away, home, stadium, base + "-" + count));
        }
        return result;
    }
}
