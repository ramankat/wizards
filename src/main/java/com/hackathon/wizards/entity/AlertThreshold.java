package com.hackathon.wizards.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Table(name = "alert_threshold")
@Data
public class AlertThreshold {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", unique = true, nullable = false)
    private Integer deviceId;

    @Column(name = "aqi_threshold_value", precision = 10)
    private Double aqiThresholdValue;

    @Column(name = "humidity_threshold_value", precision = 10)
    private Double humidityThresholdValue;

    @Column(name = "pressure_threshold_value", precision = 10)
    private Double pressureThresholdValue;

    @Column(name = "temperature_threshold_value", precision = 10)
    private Double temperatureThresholdValue;

    @CreationTimestamp
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

}