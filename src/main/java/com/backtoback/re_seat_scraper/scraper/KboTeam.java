package com.backtoback.re_seat_scraper.scraper;

public enum KboTeam {
    DOOSAN("두산", "두산 베어스"),
    LG("LG", "LG 트윈스"),
    KIWOOM("키움", "키움 히어로즈"),
    SSG("SSG", "SSG 랜더스"),
    KT("KT", "KT 위즈"),
    SAMSUNG("삼성", "삼성 라이온즈"),
    NC("NC", "NC 다이노스"),
    LOTTE("롯데", "롯데 자이언츠"),
    KIA("KIA", "KIA 타이거즈"),
    HANWHA("한화", "한화 이글스");

    private final String shortName;
    private final String fullName;

    KboTeam(String shortName, String fullName) {
        this.shortName = shortName;
        this.fullName = fullName;
    }

    public String fullName() { return fullName; }

    public static KboTeam fromShort(String shortName) {
        for (KboTeam t : values()) {
            if (t.shortName.equals(shortName.trim())) return t;
        }
        throw new IllegalArgumentException("알 수 없는 팀 약칭: " + shortName);
    }
}
