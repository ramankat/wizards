package com.hackathon.wizards.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "reading_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
//@Audited
@Builder
public class ReadingAud {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id" ,nullable = false)
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
