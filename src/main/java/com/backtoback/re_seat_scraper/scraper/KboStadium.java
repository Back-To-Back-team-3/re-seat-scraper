package com.backtoback.re_seat_scraper.scraper;
import java.util.Optional;

public enum KboStadium {
    JAMSIL("잠실", "서울종합운동장 야구장"),
    GOCHEOK("고척", "고척스카이돔"),
    MUNHAK("문학", "인천SSG랜더스필드"),
    SUWON("수원", "수원KT위즈파크"),
    DAEGU("대구", "대구삼성라이온즈파크"),
    CHANGWON("창원", "창원NC파크"),
    SAJIK("사직", "사직야구장"),
    GWANGJU("광주", "광주-기아 챔피언스필드"),
    DAEJEON("대전", "대전 한화생명 볼파크");

    private final String shortName;
    private final String fullName;

    KboStadium(String shortName, String fullName) {
        this.shortName = shortName;
        this.fullName = fullName;
    }

    public static Optional<String> resolve(String shortName) {
        for (KboStadium s : values()) {
            if (s.shortName.equals(shortName.trim())) return Optional.of(s.fullName);
        }
        return Optional.empty();
    }
}
