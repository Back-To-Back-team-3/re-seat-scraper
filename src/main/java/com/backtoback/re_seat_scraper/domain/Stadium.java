package com.backtoback.re_seat_scraper.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stadiums")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stadium {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(name = "total_capacity", nullable = false)
    private Integer totalCapacity;

    @Builder
    public Stadium(String name, String address, Integer totalCapacity) {
        this.name = name;
        this.address = address;
        this.totalCapacity = totalCapacity;
    }
}