package com.hackathon.wizards.repository;

import com.hackathon.wizards.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Integer> {
}
