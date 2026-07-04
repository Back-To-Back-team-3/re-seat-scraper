package com.backtoback.re_seat_scraper.seed;

import com.backtoback.re_seat_scraper.domain.*;
import com.backtoback.re_seat_scraper.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)   // 수집보다 먼저 실행
@Component
@ConditionalOnProperty(prefix = "scraper.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ReferenceDataSeeder implements ApplicationRunner {

    private final StadiumRepository stadiumRepo;
    private final TeamRepository teamRepo;

    @Override
    public void run(ApplicationArguments args) {
        if (teamRepo.count() > 0) return;

        Stadium jamsil   = stadium("서울종합운동장 야구장", "서울 송파구", 23750);
        Stadium gocheok  = stadium("고척스카이돔", "서울 구로구", 16000);
        Stadium munhak   = stadium("인천SSG랜더스필드", "인천 미추홀구", 23000);
        Stadium suwon    = stadium("수원KT위즈파크", "경기 수원시", 18700);
        Stadium daegu    = stadium("대구삼성라이온즈파크", "대구 수성구", 24000);
        Stadium changwon = stadium("창원NC파크", "경남 창원시", 17861);
        Stadium sajik    = stadium("사직야구장", "부산 동래구", 22990);
        Stadium gwangju  = stadium("광주-기아 챔피언스필드", "광주 북구", 20500);
        Stadium daejeon  = stadium("대전 한화생명 볼파크", "대전 중구", 20000);

        team("두산 베어스", jamsil);    team("LG 트윈스", jamsil);
        team("키움 히어로즈", gocheok);  team("SSG 랜더스", munhak);
        team("KT 위즈", suwon);         team("삼성 라이온즈", daegu);
        team("NC 다이노스", changwon);  team("롯데 자이언츠", sajik);
        team("KIA 타이거즈", gwangju);  team("한화 이글스", daejeon);
    }

    private Stadium stadium(String n, String a, int cap) {
        return stadiumRepo.save(Stadium.builder().name(n).address(a).totalCapacity(cap).build());
    }
    private void team(String n, Stadium s) {
        teamRepo.save(Team.builder().name(n).homeStadium(s).build());
    }
}
