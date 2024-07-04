package com.hackathon.wizards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Entity
@Table(name = "reading")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Audited
@Builder
public class Reading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", unique = true ,nullable = false)
    private Integer deviceId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "aqi")
    private Integer aqi;

    @Column(name = "temperature", precision = 5)
    private Double temperature;

    @Column(name = "pressure", precision = 6)
    private Double pressure;

    @Column(name = "humidity", precision = 6)
    private Double humidity;

    @Column(name = "latitude", precision = 9)
    private Double latitude;

    @Column(name = "longitude", precision = 9)
    private Double longitude;

    @Column(name = "sos_alert")
    private Boolean sosAlert;

    @Column(name = "altitude")
    private Double altitude;

    @CreationTimestamp
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

}
