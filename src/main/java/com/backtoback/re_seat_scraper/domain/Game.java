package com.backtoback.re_seat_scraper.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "games")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_key", unique = true, nullable = false, length = 30)
    private String gameKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stadium_id", nullable = false)
    private Stadium stadium;

    @Column(name = "game_at", nullable = false)
    private LocalDateTime gameAt;

    @Column(length = 255)
    private String title;

    @Builder
    public Game(String gameKey, Team homeTeam, Team awayTeam, Stadium stadium,
                LocalDateTime gameAt, String title) {
        this.gameKey = gameKey;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.stadium = stadium;
        this.gameAt = gameAt;
        this.title = title;
    }

    // 같은 경기 행은 유지하고, 일정 변경으로 달라질 수 있는 필드만 갱신한다.
    public boolean updateFrom(Game n) {
        boolean changed = !Objects.equals(gameAt, n.gameAt)
                || !Objects.equals(title, n.title);
        if (changed) {
            this.gameAt = n.gameAt;
            this.title = n.title;
        }
        return changed;
    }
}
