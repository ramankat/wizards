package com.hackathon.wizards.repository;

import com.hackathon.wizards.entity.AlertThreshold;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertThresholdRepository extends JpaRepository<AlertThreshold, Long> {
    AlertThreshold findAllByDeviceId(Integer deviceId);
}
