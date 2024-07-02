package com.hackathon.wizards.repository;

import com.hackathon.wizards.entity.Reading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingRepository extends JpaRepository<Reading, Integer> {
}
