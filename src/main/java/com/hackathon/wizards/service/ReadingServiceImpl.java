package com.hackathon.wizards.service;

import com.hackathon.wizards.dto.AlertChart;
import com.hackathon.wizards.dto.AlertDataPoint;
import com.hackathon.wizards.dto.DeviceData;
import com.hackathon.wizards.dto.MeanData;
import com.hackathon.wizards.dto.ParamDataPoint;
import com.hackathon.wizards.dto.ReadingRequest;
import com.hackathon.wizards.entity.Reading;
import com.hackathon.wizards.repository.ReadingRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import java.util.*;
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
        saveReadingDate(readingRequest, 0);
        Random random = new Random();
        readingRequest.setDeviceId(2);
        saveReadingDate(readingRequest, random.ints(-10, 10).findFirst().getAsInt());
        readingRequest.setDeviceId(3);
        saveReadingDate(readingRequest, random.ints(-10, 10).findFirst().getAsInt());

    }

    private void saveReadingDate(ReadingRequest readingRequest, int delta){
        Reading existingReading = readingRepository.findAllByDeviceId(readingRequest.getDeviceId());
        if(Objects.isNull(existingReading)){
            existingReading = new Reading();

        }
        existingReading.setDeviceId(readingRequest.getDeviceId());
        existingReading.setAqi(readingRequest.getAqi() + delta);
        existingReading.setPressure(readingRequest.getPressure() + delta);
        existingReading.setTemperature(readingRequest.getTemperature() + delta);
        existingReading.setHumidity(readingRequest.getHumidity() + delta);
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
        readingAud.setAqi(readingRequest.getAqi() + delta);
        readingAud.setPressure(readingRequest.getPressure() + delta);
        readingAud.setTemperature(readingRequest.getTemperature() + delta);
        readingAud.setHumidity(readingRequest.getHumidity() + delta);
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
            List<Double> aqiList = new ArrayList<>();
            List<Double> temparatureList = new ArrayList<>();
            List<Double> humidityList = new ArrayList<>();
            List<Double> pressureList = new ArrayList<>();
            for(ReadingAud readingAud : readingAuds) {
                aqiList.add(Double.valueOf(readingAud.getAqi()));
                temparatureList.add(readingAud.getTemperature());
                humidityList.add(readingAud.getHumidity());
                pressureList.add(readingAud.getPressure());

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

            deviceData.setMedianAqi(findMedian(aqiList));
            deviceData.setMedianPressure(findMedian(pressureList));
            deviceData.setMedianHumidity(findMedian(humidityList));
            deviceData.setMedianTemperature(findMedian(temparatureList));
        } catch (Exception ex) {
            log.error("Error {} occurred while fetching reading detail", ex.getMessage());
            throw ex;
        }
        return deviceData;
    }

    @Override
    public AlertChart getAlertsChart(Integer days) {
        LocalDate localDate = LocalDate.now().minusDays(days);
        LocalDateTime localDateTime = LocalDateTime.of(localDate.getYear(), localDate.getMonth(), localDate.getDayOfMonth(), 0, 0);
        List<Alert> alerts = alertRepository.findAllByCreatedAtGreaterThan(localDateTime);
        Map<LocalDateTime, Integer> map = new HashMap<>();

        LocalDateTime start = LocalDateTime.of(localDate.getYear(), localDate.getMonth(), localDate.getDayOfMonth(), 0, 0);
        while(start.isBefore(LocalDateTime.now())) {
            map.put(start, 0);
            start = start.plusDays(1);
        }
        for(Alert alert: alerts) {
            LocalDateTime alertTime = alert.getCreatedAt();
            LocalDateTime floorTime = LocalDateTime.of(alertTime.getYear(), alertTime.getMonth(), alertTime.getDayOfMonth(), 0, 0);
            map.putIfAbsent(floorTime, 0);
            map.put(floorTime, map.get(floorTime) + 1);
        }
        AlertChart alertChart = new AlertChart();
        List<AlertDataPoint> alertDataPointList = new ArrayList<>();
        for(LocalDateTime dateTime : map.keySet()) {
            alertDataPointList.add(new AlertDataPoint(dateTime, map.get(dateTime)));
        }
        alertChart.setAlerts(alertDataPointList);
        return alertChart;
    }

    @Override
    public MeanData getMeanChart(Integer days) {
        LocalDate localDate = LocalDate.now().minusDays(days);
        LocalDateTime localDateTime = LocalDateTime.of(localDate.getYear(), localDate.getMonth(), localDate.getDayOfMonth(), 0, 0);
        List<ReadingAud> readingAuds = readingAuditRepository.findAllByCreatedAtGreaterThan(localDateTime);
        Map<LocalDateTime, List<Double>> pressureMap = new HashMap<>();
        Map<LocalDateTime, List<Double>> temparatureMap = new HashMap<>();
        Map<LocalDateTime, List<Double>> aqiMap = new HashMap<>();
        Map<LocalDateTime, List<Double>> humidityMap = new HashMap<>();

        LocalDateTime start = LocalDateTime.of(localDate.getYear(), localDate.getMonth(), localDate.getDayOfMonth(), 0, 0);
        while(start.isBefore(LocalDateTime.now())) {
            pressureMap.put(start, new ArrayList<>());
            temparatureMap.put(start, new ArrayList<>());
            aqiMap.put(start, new ArrayList<>());
            humidityMap.put(start, new ArrayList<>());
            start = start.plusDays(1);
        }

        for(ReadingAud readingAud: readingAuds) {
            LocalDateTime alertTime = readingAud.getCreatedAt();
            LocalDateTime floorTime = LocalDateTime.of(alertTime.getYear(), alertTime.getMonth(), alertTime.getDayOfMonth(), 0, 0);
            pressureMap.putIfAbsent(floorTime, new ArrayList<>());
            pressureMap.get(floorTime).add(readingAud.getPressure());
            temparatureMap.putIfAbsent(floorTime, new ArrayList<>());
            temparatureMap.get(floorTime).add(readingAud.getTemperature());
            aqiMap.putIfAbsent(floorTime, new ArrayList<>());
            aqiMap.get(floorTime).add(Double.valueOf(readingAud.getAqi()));
            humidityMap.putIfAbsent(floorTime, new ArrayList<>());
            humidityMap.get(floorTime).add(readingAud.getHumidity());
        }

        MeanData meanData = new MeanData();
        meanData.setAqiGraph(new ArrayList<>());
        meanData.setTemperatureGraph(new ArrayList<>());
        meanData.setHumidityGraph(new ArrayList<>());
        meanData.setPressureGraph(new ArrayList<>());

        meanData.setAqiMedianGraph(new ArrayList<>());
        meanData.setTemperatureMedianGraph(new ArrayList<>());
        meanData.setHumidityMedianGraph(new ArrayList<>());
        meanData.setPressureMedianGraph(new ArrayList<>());
        for(LocalDateTime dateTime: pressureMap.keySet()) {
            double count = pressureMap.get(dateTime).size() * 1.0d;
            meanData.getPressureGraph().add(new ParamDataPoint(dateTime,
                    findMean(pressureMap.get(dateTime))));
            meanData.getTemperatureGraph().add(new ParamDataPoint(dateTime,
                    findMean(temparatureMap.get(dateTime))));
            meanData.getAqiGraph().add(new ParamDataPoint(dateTime,
                    findMean(aqiMap.get(dateTime))));
            meanData.getHumidityGraph().add(new ParamDataPoint(dateTime,
                    findMean(humidityMap.get(dateTime))));

            meanData.getPressureMedianGraph().add(new ParamDataPoint(dateTime,
                    findMedian(pressureMap.get(dateTime))));
            meanData.getTemperatureMedianGraph().add(new ParamDataPoint(dateTime,
                    findMedian(temparatureMap.get(dateTime))));
            meanData.getAqiMedianGraph().add(new ParamDataPoint(dateTime,
                    findMedian(aqiMap.get(dateTime))));
            meanData.getHumidityMedianGraph().add(new ParamDataPoint(dateTime,
                    findMedian(humidityMap.get(dateTime))));
        }
        return meanData;
    }

    public static double findMedian(List<Double> list)
    {
        if(list == null || list.size() == 0) {
            return 0.0d;
        }
        int n = list.size();
        // First we sort the array
        Collections.sort(list);

        // check for even case
        if (n % 2 != 0)
            return list.get(n/2);

        return (double)(list.get((n - 1) / 2) + list.get(n / 2)) / 2.0;
    }

    public static double findMean(List<Double> list)
    {
        if(list == null || list.size() == 0) {
            return 0.0d;
        }
        int n = list.size();
        int sum = 0;
        for (int i = 0; i < n; i++)
            sum += list.get(i);

        return (double)sum / (double)n;
    }
}
