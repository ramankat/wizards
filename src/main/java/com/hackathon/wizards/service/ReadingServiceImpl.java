package com.hackathon.wizards.service;

import com.hackathon.wizards.dto.ReadingRequest;
import com.hackathon.wizards.entity.Reading;
import com.hackathon.wizards.repository.ReadingRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import com.hackathon.wizards.entity.*;
import com.hackathon.wizards.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ReadingServiceImpl implements ReadingService {

    @Autowired
    private ReadingRepository readingRepository;

    @Autowired
    private AlertThresholdRepository alertThresholdRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private HeatMapDataRepository heatMapDataRepository;

    @Autowired
    private AlertAttributeMappingRepository alertAttributeMappingRepository;


    @Override
    @Transactional
    public void saveReading(ReadingRequest readingRequest) {
        Reading existingReading = readingRepository.findAllByDeviceId(readingRequest.getDeviceId());
        if(Objects.isNull(existingReading)){
            existingReading = new Reading();

        }
        existingReading.setDeviceId(readingRequest.getDeviceId());
        existingReading.setAqi(readingRequest.getAqi());
        existingReading.setPressure(readingRequest.getPressure());
        existingReading.setTemperature(readingRequest.getTemperature());
        existingReading.setHumidity(readingRequest.getHumidity());
        existingReading.setLongitude(readingRequest.getLongitude());
        existingReading.setLatitude(readingRequest.getLatitude());
        existingReading.setSosAlert(readingRequest.getSosAlert());
        existingReading.setTimestamp(LocalDateTime.now());
        existingReading.setAltitude(readingRequest.getAltitude());
        readingRepository.save(existingReading);

        AlertThreshold  alertThreshold = alertThresholdRepository.findAllByDeviceId(existingReading.getDeviceId());
        boolean sendAlertFlag = addAlerts(alertThreshold, existingReading);

        if (sendAlertFlag){
            populateHeatData(existingReading);
        }
    }

    private void populateHeatData(Reading existingReading) {
        HeatMapData heatMapData = new HeatMapData();

        heatMapData.setDeviceId(existingReading.getDeviceId());
        heatMapData.setAlertCount(1);
        heatMapData.setLongitude(existingReading.getLongitude());
        heatMapData.setLatitude(existingReading.getLatitude());
        heatMapData.setTimestamp(LocalDateTime.now());

        heatMapDataRepository.save(heatMapData);
    }

    private boolean addAlerts(AlertThreshold alertThreshold, Reading existingReading) {
        Alert alert = new Alert();
        alert.setDeviceId(existingReading.getDeviceId());
        List<AlertAttributeMapping> alertAttributeMappingList = new ArrayList<>();
        boolean isAlert = false;
        if(alertThreshold == null){
            return false;
        }
        if(existingReading.getAqi() >= alertThreshold.getAqiThresholdValue()){
            AlertAttributeMapping alertAttributeMapping = new AlertAttributeMapping();
            alertAttributeMapping.setAlertType("AQI");
            alertAttributeMapping.setValue(Double.valueOf(existingReading.getAqi()));
            alertAttributeMapping.setThresholdValue(alertThreshold.getAqiThresholdValue());
            alertAttributeMappingList.add(alertAttributeMapping);

//            alert.setAlertType("AQI");
//            alert.setThresholdValue(Double.valueOf(existingReading.getAqi()));
            isAlert = true;
        }
        if(existingReading.getHumidity() >= alertThreshold.getHumidityThresholdValue()){

            AlertAttributeMapping alertAttributeMapping = new AlertAttributeMapping();
            alertAttributeMapping.setAlertType("HUMIDITY");
            alertAttributeMapping.setValue(Double.valueOf(existingReading.getHumidity()));
            alertAttributeMapping.setThresholdValue(alertThreshold.getHumidityThresholdValue());
            alertAttributeMappingList.add(alertAttributeMapping);

//            alert.setAlertType("HUMIDITY");
//            alert.setThresholdValue(existingReading.getHumidity());
            isAlert = true;
        }
        if(existingReading.getPressure() >= alertThreshold.getPressureThresholdValue()){

            AlertAttributeMapping alertAttributeMapping = new AlertAttributeMapping();
            alertAttributeMapping.setAlertType("PRESSURE");
            alertAttributeMapping.setValue(existingReading.getPressure());
            alertAttributeMapping.setThresholdValue(alertThreshold.getPressureThresholdValue());
            alertAttributeMappingList.add(alertAttributeMapping);

//            alert.setAlertType("PRESSURE");
//            alert.setThresholdValue(existingReading.getPressure());
            isAlert = true;
        }
        if(existingReading.getTemperature() >= alertThreshold.getTemperatureThresholdValue()){

            AlertAttributeMapping alertAttributeMapping = new AlertAttributeMapping();
            alertAttributeMapping.setAlertType("TEMPERATURE");
            alertAttributeMapping.setValue(existingReading.getTemperature());
            alertAttributeMapping.setThresholdValue(alertThreshold.getTemperatureThresholdValue());
            alertAttributeMappingList.add(alertAttributeMapping);

//            alert.setAlertType("TEMPERATURE");
//            alert.setThresholdValue(existingReading.getTemperature());
            isAlert = true;
        }

        if(isAlert){
            alert.setLongitude(existingReading.getLongitude());
            alert.setLatitude(existingReading.getLatitude());
            alert.setTimestamp(LocalDateTime.now());
            alert = alertRepository.save(alert);

            Long finalAlert = alert.getId();
            alertAttributeMappingList.forEach(alertAttributeMapping -> {
                alertAttributeMapping.setAlertId(finalAlert);
            });
            alertAttributeMappingRepository.saveAll(alertAttributeMappingList);
        }
        return isAlert;

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
    public Reading getReadingDetail(Long id) {
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
