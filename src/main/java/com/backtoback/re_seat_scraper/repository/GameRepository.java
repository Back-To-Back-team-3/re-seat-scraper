package com.backtoback.re_seat_scraper.repository;

import com.backtoback.re_seat_scraper.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByGameKeyIn(List<String> keys);

    // 월별 CSV 출력 시 DB에 남아 있는 다른 월 경기까지 섞이지 않도록 범위 조회를 사용한다.
    List<Game> findByGameAtGreaterThanEqualAndGameAtLessThanOrderByGameAtAsc(
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );
}
