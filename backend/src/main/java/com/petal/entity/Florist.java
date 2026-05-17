package com.petal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @OneToOne(fetch = FetchType.LAZY, optional = false)
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
    private Integer maxDailyCapacity;

    @Column(name = "delivery_coverage")
    private String deliveryCoverage;

    @Column(name = "time_slots")
    private String timeSlots;

    @Column(name = "prep_lead_time_hours")
    private Integer prepLeadTimeHours;

    @Column(name = "onboarding_complete", nullable = false)
    @Builder.Default
    private boolean onboardingComplete = false;
}
