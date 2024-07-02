package com.hackathon.wizards.repository;

import com.hackathon.wizards.entity.HeatMapData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeatMapDataRepository extends JpaRepository<HeatMapData, Integer> {
}
