package com.hackathon.wizards.service;

import com.hackathon.wizards.dto.ReadingRequest;
import com.hackathon.wizards.entity.Reading;
import com.hackathon.wizards.repository.ReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReadingServiceImpl implements ReadingService{

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
}
