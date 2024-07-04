package com.hackathon.wizards.repository;

import com.hackathon.wizards.entity.Alert;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findAllByCreatedAtGreaterThan(LocalDateTime createdAt);
}
