package com.backtoback.re_seat_scraper.scraper;
import java.time.LocalDate;
import java.time.LocalTime;

public record RawGame(
        LocalDate date, LocalTime time,
        String awayTeamName, String homeTeamName,
        String stadiumShortName, String gameKey
) {}
