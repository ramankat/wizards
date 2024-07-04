package com.hackathon.wizards.repository;

import com.hackathon.wizards.entity.Reading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadingRepository extends JpaRepository<Reading, Long> {
    Reading findAllByDeviceId(Integer deviceId);
}
