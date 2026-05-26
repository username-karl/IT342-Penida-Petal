package com.petal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "buyer_notifications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_buyer_notifications_saved_date_year",
                columnNames = {"user_id", "saved_date_id", "type", "notification_year"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyerNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "saved_date_id", nullable = false)
    private SavedDate savedDate;

    @Column(nullable = false, length = 100)
    private String type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Column(nullable = false)
    private Integer notificationYear;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant readAt;
}
