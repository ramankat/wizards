package com.hackathon.wizards.service;

import com.hackathon.wizards.dto.DeviceData;
import com.hackathon.wizards.dto.ParamDataPoint;
import com.hackathon.wizards.dto.ReadingRequest;
import com.hackathon.wizards.entity.Reading;
import com.hackathon.wizards.repository.ReadingRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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
    private ReadingAuditRepository readingAuditRepository;

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

        ReadingAud readingAud = new ReadingAud();
        readingAud.setDeviceId(readingRequest.getDeviceId());
        readingAud.setAqi(readingRequest.getAqi());
        readingAud.setPressure(readingRequest.getPressure());
        readingAud.setTemperature(readingRequest.getTemperature());
        readingAud.setHumidity(readingRequest.getHumidity());
        readingAud.setLongitude(readingRequest.getLongitude());
        readingAud.setLatitude(readingRequest.getLatitude());
        readingAud.setSosAlert(readingRequest.getSosAlert());
        readingAud.setTimestamp(LocalDateTime.now());
        readingAud.setAltitude(readingRequest.getAltitude());
        readingAuditRepository.save(readingAud);
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
    public DeviceData getReadingDetail(Long id, Integer dataPoints) {
        DeviceData deviceData = new DeviceData();
        try {
            log.info("getting reading data for device id {}", id);
            Optional<Reading> readingDetail = readingRepository.findById(id);
            deviceData.setCurrReading(readingDetail.get());
            List<ReadingAud> readingAuds = readingAuditRepository.findLastNPoints(id, dataPoints);
            deviceData.setAqiGraph(new ArrayList<>());
            deviceData.setTemperatureGraph(new ArrayList<>());
            deviceData.setHumidityGraph(new ArrayList<>());
            deviceData.setPressureGraph(new ArrayList<>());
            double aqiSum = 0.0d;
            double temperatureSum = 0.0d;
            double humiditySum = 0.0d;
            double pressureSum = 0.0d;
            readingAuds.stream().sorted(Comparator.comparing(ReadingAud::getCreatedAt)).forEach(readingAud -> {
                deviceData.getHumidityGraph().add(new ParamDataPoint(readingAud.getCreatedAt(), readingAud.getHumidity()));
                deviceData.getTemperatureGraph().add(new ParamDataPoint(readingAud.getCreatedAt(), readingAud.getTemperature()));
                deviceData.getPressureGraph().add(new ParamDataPoint(readingAud.getCreatedAt(), readingAud.getPressure()));
                deviceData.getAqiGraph().add(new ParamDataPoint(readingAud.getCreatedAt(), Double.valueOf(readingAud.getAqi())));
            });
            for(ReadingAud readingAud : readingAuds) {
                aqiSum += readingAud.getAqi();
                temperatureSum += readingAud.getTemperature();
                humiditySum += readingAud.getHumidity();
                pressureSum += readingAud.getPressure();
            }
            double points = readingAuds.size();
            deviceData.setAvgAqi(aqiSum / points);
            deviceData.setAvgHumidity(humiditySum / points);
            deviceData.setAvgPressure(pressureSum / points);
            deviceData.setAvgTemperature(temperatureSum / points);
        } catch (Exception ex) {
            log.error("Error {} occurred while fetching reading detail", ex.getMessage());
            throw ex;
        }
        return deviceData;
    }
}
