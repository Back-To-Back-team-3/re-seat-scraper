package com.backtoback.re_seat_scraper.export;

public final class ReSeatCsvSchema {

    public static final String STADIUMS_FILE = "stadiums.csv";
    public static final String TEAMS_FILE = "teams.csv";
    public static final String GAMES_FILE = "games.csv";

    // re-seat 본체 ERD와 import 기준에 맞춘 CSV 컬럼 순서다.
    public static final String STADIUMS_HEADER = "id,name,address,total_capacity,status";
    public static final String TEAMS_HEADER = "id,name,home_stadium_id,status";
    public static final String GAMES_HEADER = "id,home_team_id,away_team_id,stadium_id,game_at,booking_open_at,booking_close_at,booking_status,title";

    public static final String ACTIVE_STATUS = "ACTIVE";
    public static final String SCHEDULED_BOOKING_STATUS = "SCHEDULED";

    private ReSeatCsvSchema() {
    }
}
