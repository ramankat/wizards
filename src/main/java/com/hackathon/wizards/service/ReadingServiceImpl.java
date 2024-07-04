package com.hackathon.wizards.service;

import com.hackathon.wizards.dto.ReadingRequest;
import com.hackathon.wizards.entity.Reading;
import com.hackathon.wizards.repository.ReadingRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class ReadingServiceImpl implements ReadingService {

    @Autowired
    private ReadingRepository readingRepository;

    @Override
    public void saveReading(ReadingRequest readingRequest) {
        Reading reading = Reading.builder()
                .deviceId(readingRequest.getDeviceId())
                .aqi(readingRequest.getAqi())
                .pressure(readingRequest.getPressure())
                .temperature(readingRequest.getTemperature())
                .longitude(readingRequest.getLongitude())
                .latitude(readingRequest.getLatitude())
                .timestamp(LocalDateTime.now())
                .build();
        readingRepository.save(reading);
    }

    @Override
    public List<Reading> getAllReadings() {
        try {
            log.info("getting all reading from db");
            List<Reading> allReadings = readingRepository.findAll();
            log.info("{} reading retrieved", allReadings.size());
            return allReadings;
        } catch (Exception ex) {
            log.error("Error {} occurred while fetching all readings", ex.getMessage());
            throw ex;
        }
    }

    @Override
    public Reading getReadingDetail(Integer id) {
        try {
            log.info("getting reading data for device id {}", id);
            Optional<Reading> readingDetail = readingRepository.findById(id);
            if (readingDetail.isPresent()) {
                return readingDetail.get();
            }
        } catch (Exception ex) {
            log.error("Error {} occurred while fetching reading detail", ex.getMessage());
            throw ex;
        }
        return new Reading();
    }
}
