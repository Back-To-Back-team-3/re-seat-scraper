package com.backtoback.re_seat_scraper.repository;

import com.backtoback.re_seat_scraper.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByGameKeyIn(List<String> keys);
    List<Game> findByGameAtGreaterThanEqualAndGameAtLessThanOrderByGameAtAsc(
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );
}
