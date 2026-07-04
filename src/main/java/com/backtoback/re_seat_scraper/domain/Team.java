package com.backtoback.re_seat_scraper.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teams")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_stadium_id", nullable = false)
    private Stadium homeStadium;

    @Builder
    public Team(String name, Stadium homeStadium) {
        this.name = name;
        this.homeStadium = homeStadium;
    }
}
