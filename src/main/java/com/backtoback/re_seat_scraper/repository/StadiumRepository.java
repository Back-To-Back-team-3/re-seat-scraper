package com.backtoback.re_seat_scraper.repository;
import com.backtoback.re_seat_scraper.domain.Stadium;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StadiumRepository extends JpaRepository<Stadium, Long> {
    Optional<Stadium> findByName(String name);
}
