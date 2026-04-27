package com.petal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "florists")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Florist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "store_name")
    private String storeName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "logo_url")
    private String logoUrl;

    private String street;

    private String city;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "max_daily_capacity")
    @Builder.Default
    private int maxDailyCapacity = 10;
}
