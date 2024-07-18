package com.hackathon.wizards.service;

import com.hackathon.wizards.dto.*;
import com.hackathon.wizards.entity.Reading;
import com.hackathon.wizards.repository.ReadingRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import java.util.*;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import lombok.extern.slf4j.Slf4j;
import com.hackathon.wizards.entity.*;
import com.hackathon.wizards.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

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

    @Autowired
    private AppConfigRepository appConfigRepository;

//    @Value("${twilio.account.sid}")
//    public String ACCOUNT_SID;
//    @Value("${twilio.auth.token}")
//    public String AUTH_TOKEN;


    @Override
    @Transactional
    public Boolean saveReading(ReadingRequest readingRequest) {
        boolean isAlert = saveReadingDate(readingRequest, 0);
        Random random = new Random();
        readingRequest.setDeviceId(2);
        readingRequest.setSosAlert(false);
        saveReadingDate(readingRequest, random.ints(-10, 10).findFirst().getAsInt());
        readingRequest.setDeviceId(3);
        saveReadingDate(readingRequest, random.ints(-10, 10).findFirst().getAsInt());
        return isAlert;
    }

    public static double roundToPrecision(double value, int precision) {
        double scale = Math.pow(10, precision);
        return Math.round(value * scale) / scale;
    }

    void sendSms(String body){
        try{
            AppConfig appConfig = appConfigRepository.findAllByKey("ALERT_NUMBERS");
            AppConfig appConfig1 = appConfigRepository.findAllByKey("ACCOUNT_SID");
            AppConfig appConfig2 = appConfigRepository.findAllByKey("AUTH_TOKEN");
            AppConfig appConfig3 = appConfigRepository.findAllByKey("SMS_ENABLED");

            String val = appConfig.getValue();
            if(val == null || !Boolean.parseBoolean(appConfig3.getValue())){
                return;
            }
            List<String> toNumbers = getListFromCommaSeparatedString(val);

            // Find your Account Sid and Token at twilio.com/console
            Twilio.init(appConfig1.getValue(), appConfig2.getValue());
            for (String to : toNumbers) {
                Message message = Message.creator(
                        new com.twilio.type.PhoneNumber("whatsapp:+91" + to),
                        new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),
                        body
                ).create();
                System.out.println("Sent message with SID: " + message.getSid() + " to " + to);
            }
        }catch (Exception e){
            log.error("Error while sending message");
        }

    }

    public static List<String> getListFromCommaSeparatedString(String commaSeparatedString) {
        if (commaSeparatedString == null || commaSeparatedString.isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.asList(commaSeparatedString.split("\\s*,\\s*"));
    }


    private boolean saveReadingDate(ReadingRequest readingRequest, int delta){
        roundOffRequestTo2DecimalPlaces(readingRequest);
        Reading existingReading = readingRepository.findAllByDeviceId(readingRequest.getDeviceId());
        if(Objects.isNull(existingReading)){
            existingReading = new Reading();

        }
        existingReading.setDeviceId(readingRequest.getDeviceId());
        existingReading.setAqi(Math.abs(readingRequest.getAqi().intValue() + delta));
        existingReading.setVoc(Math.abs(readingRequest.getVoc() + delta));
        existingReading.setHeatIndex(Math.abs(readingRequest.getHeatIndex() + delta));
        existingReading.setCo2(Math.abs(readingRequest.getCo2() + delta));
        existingReading.setPressure(readingRequest.getDeviceId() != 1 ? Math.abs(readingRequest.getPressure() + delta * 30) :
                    Math.abs(readingRequest.getPressure() + delta));
        existingReading.setTemperature(Math.abs(readingRequest.getTemperature() + delta));
        existingReading.setHumidity(Math.abs(readingRequest.getHumidity() + delta));
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
        readingAud.setAqi(Math.abs(readingRequest.getAqi().intValue() + delta));
        readingAud.setVoc(Math.abs(readingRequest.getVoc() + delta));
        readingAud.setHeatIndex(Math.abs(readingRequest.getHeatIndex() + delta));
        readingAud.setCo2(Math.abs(readingRequest.getCo2() + delta));
        readingAud.setPressure(readingRequest.getDeviceId() != 1 ? Math.abs(readingRequest.getPressure() + delta * 30) :
                Math.abs(readingRequest.getPressure() + delta));
        readingAud.setTemperature(Math.abs(readingRequest.getTemperature() + delta));
        readingAud.setHumidity(Math.abs(readingRequest.getHumidity() + delta));
        readingAud.setLongitude(readingRequest.getLongitude());
        readingAud.setLatitude(readingRequest.getLatitude());
        readingAud.setSosAlert(readingRequest.getSosAlert());
        readingAud.setTimestamp(LocalDateTime.now());
        readingAud.setAltitude(readingRequest.getAltitude());
        readingAuditRepository.save(readingAud);
        return sendAlertFlag;
    }

    private void roundOffRequestTo2DecimalPlaces(ReadingRequest readingRequest) {
        readingRequest.setAqi(roundToPrecision(readingRequest.getAqi(), 2));
        readingRequest.setVoc(roundToPrecision(readingRequest.getVoc(), 2));
        readingRequest.setHeatIndex(roundToPrecision(readingRequest.getHeatIndex(), 2));
        readingRequest.setCo2(roundToPrecision(readingRequest.getCo2(), 2));
        readingRequest.setTemperature(roundToPrecision(readingRequest.getTemperature(), 2));
        readingRequest.setPressure(roundToPrecision(readingRequest.getPressure(), 2));
        readingRequest.setHumidity(roundToPrecision(readingRequest.getHumidity(), 2));
    }

    private void populateHeatData(Reading existingReading) {
        if(existingReading.getLatitude() == 0.0 || existingReading.getLongitude() == 0.0){
            return;
        }
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
        String alertMessage = "An alert has been detected for Device Id : 1, for below parameter : ";
        if(alertThreshold == null){
            return false;
        }
        if(existingReading.getAqi() >= alertThreshold.getAqiThresholdValue()){
            AlertAttributeMapping alertAttributeMapping = new AlertAttributeMapping();
            alertAttributeMapping.setAlertType("AQI");
            alertAttributeMapping.setValue(Double.valueOf(existingReading.getAqi()));
            alertAttributeMapping.setThresholdValue(alertThreshold.getAqiThresholdValue());
            alertAttributeMappingList.add(alertAttributeMapping);

            alertMessage = alertMessage + String.format(" AQI breached the threshold :  %s ", alertThreshold.getAqiThresholdValue());
//            alert.setAlertType("AQI");
//            alert.setThresholdValue(Double.valueOf(existingReading.getAqi()));
            isAlert = true;
        }
        if(existingReading.getVoc() >= alertThreshold.getVocThresholdValue()){
            AlertAttributeMapping alertAttributeMapping = new AlertAttributeMapping();
            alertAttributeMapping.setAlertType("VOC");
            alertAttributeMapping.setValue(Double.valueOf(existingReading.getVoc()));
            alertAttributeMapping.setThresholdValue(alertThreshold.getVocThresholdValue());
            alertAttributeMappingList.add(alertAttributeMapping);

            alertMessage = alertMessage + String.format(" & VOC breached the threshold :  %s ", alertThreshold.getVocThresholdValue());
            isAlert = true;
        }
        if(existingReading.getHeatIndex() >= alertThreshold.getHeatIndexThresholdValue()){
            AlertAttributeMapping alertAttributeMapping = new AlertAttributeMapping();
            alertAttributeMapping.setAlertType("HEAT_INDEX");
            alertAttributeMapping.setValue(Double.valueOf(existingReading.getHeatIndex()));
            alertAttributeMapping.setThresholdValue(alertThreshold.getHeatIndexThresholdValue());
            alertAttributeMappingList.add(alertAttributeMapping);
            alertMessage = alertMessage + String.format("  & HEAT_INDEX breached the threshold :  %s ", alertThreshold.getHeatIndexThresholdValue());
            isAlert = true;
        }
        if(existingReading.getCo2() >= alertThreshold.getCo2()){
            AlertAttributeMapping alertAttributeMapping = new AlertAttributeMapping();
            alertAttributeMapping.setAlertType("CO2");
            alertAttributeMapping.setValue(Double.valueOf(existingReading.getCo2()));
            alertAttributeMapping.setThresholdValue(alertThreshold.getCo2());
            alertAttributeMappingList.add(alertAttributeMapping);

            alertMessage = alertMessage + String.format("  & CO2 breached the threshold :  %s ", alertThreshold.getCo2());
            isAlert = true;
        }
        if(existingReading.getHumidity() >= alertThreshold.getHumidityThresholdValue()){

            AlertAttributeMapping alertAttributeMapping = new AlertAttributeMapping();
            alertAttributeMapping.setAlertType("HUMIDITY");
            alertAttributeMapping.setValue(Double.valueOf(existingReading.getHumidity()));
            alertAttributeMapping.setThresholdValue(alertThreshold.getHumidityThresholdValue());
            alertAttributeMappingList.add(alertAttributeMapping);

            alertMessage = alertMessage + String.format("  & HUMIDITY breached the threshold :  %s ", alertThreshold.getHumidityThresholdValue());
            isAlert = true;
        }
        if(existingReading.getPressure() >= alertThreshold.getPressureThresholdValue()){

            AlertAttributeMapping alertAttributeMapping = new AlertAttributeMapping();
            alertAttributeMapping.setAlertType("PRESSURE");
            alertAttributeMapping.setValue(existingReading.getPressure());
            alertAttributeMapping.setThresholdValue(alertThreshold.getPressureThresholdValue());
            alertAttributeMappingList.add(alertAttributeMapping);

            alertMessage = alertMessage + String.format("  & PRESSURE breached the threshold :  %s ", alertThreshold.getPressureThresholdValue());
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
            alertMessage = alertMessage + String.format("  & TEMPERATURE breached the threshold :  %s ", alertThreshold.getTemperatureThresholdValue());
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
            alertMessage = alertMessage + " Please check on priority ";
            if(existingReading.getDeviceId() == 1){
                String finalAlertMessage = alertMessage;
                CompletableFuture.runAsync(() -> sendSms(finalAlertMessage));
            }
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
            deviceData.setVocGraph(new ArrayList<>());
            deviceData.setHeatIndexGraph(new ArrayList<>());
            deviceData.setCo2Graph(new ArrayList<>());
            deviceData.setTemperatureGraph(new ArrayList<>());
            deviceData.setHumidityGraph(new ArrayList<>());
            deviceData.setPressureGraph(new ArrayList<>());
            double aqiSum = 0.0d;
            double vocSum = 0.0d;
            double heatIndexSum = 0.0d;
            double co2Sum = 0.0d;
            double temperatureSum = 0.0d;
            double humiditySum = 0.0d;
            double pressureSum = 0.0d;
            readingAuds.stream().sorted(Comparator.comparing(ReadingAud::getCreatedAt)).forEach(readingAud -> {
                deviceData.getHumidityGraph().add(new ParamDataPoint(readingAud.getCreatedAt(), roundOff(readingAud.getHumidity(), 2)));
                deviceData.getTemperatureGraph().add(new ParamDataPoint(readingAud.getCreatedAt(), roundOff(readingAud.getTemperature(), 2)));
                deviceData.getPressureGraph().add(new ParamDataPoint(readingAud.getCreatedAt(), roundOff(readingAud.getPressure(), 2)));
                deviceData.getAqiGraph().add(new ParamDataPoint(readingAud.getCreatedAt(), roundOff(Double.valueOf(readingAud.getAqi()), 2)));
                deviceData.getVocGraph().add(new ParamDataPoint(readingAud.getCreatedAt(), roundToPrecision(Double.valueOf(readingAud.getVoc()), 2)));
                deviceData.getHeatIndexGraph().add(new ParamDataPoint(readingAud.getCreatedAt(), roundOff(Double.valueOf(readingAud.getHeatIndex()), 2)));
                deviceData.getCo2Graph().add(new ParamDataPoint(readingAud.getCreatedAt(), roundOff(Double.valueOf(readingAud.getCo2()), 2)));
            });
            List<Double> aqiList = new ArrayList<>();
            List<Double> temparatureList = new ArrayList<>();
            List<Double> humidityList = new ArrayList<>();
            List<Double> pressureList = new ArrayList<>();
            List<Double> vocList = new ArrayList<>();
            List<Double> heatIndexList = new ArrayList<>();
            List<Double> co2List = new ArrayList<>();
            for(ReadingAud readingAud : readingAuds) {
                aqiList.add(Double.valueOf(readingAud.getAqi()));
                temparatureList.add(readingAud.getTemperature());
                humidityList.add(readingAud.getHumidity());
                pressureList.add(readingAud.getPressure());
                vocList.add(readingAud.getVoc());
                heatIndexList.add(readingAud.getHeatIndex());
                co2List.add(readingAud.getCo2());

                aqiSum += readingAud.getAqi();
                vocSum += readingAud.getVoc();
                heatIndexSum += readingAud.getHeatIndex();
                co2Sum += readingAud.getCo2();
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
        Collections.sort(alertChart.getAlerts(), Comparator.comparing(AlertDataPoint::getDate));
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
        Map<LocalDateTime, List<Double>> vocMap = new HashMap<>();
        Map<LocalDateTime, List<Double>> heatIndexMap = new HashMap<>();
        Map<LocalDateTime, List<Double>> co2Map = new HashMap<>();
        Map<LocalDateTime, List<Double>> humidityMap = new HashMap<>();

        LocalDateTime start = LocalDateTime.of(localDate.getYear(), localDate.getMonth(), localDate.getDayOfMonth(), 0, 0);
        while(start.isBefore(LocalDateTime.now())) {
            pressureMap.put(start, new ArrayList<>());
            temparatureMap.put(start, new ArrayList<>());
            aqiMap.put(start, new ArrayList<>());
            vocMap.put(start, new ArrayList<>());
            heatIndexMap.put(start, new ArrayList<>());
            co2Map.put(start, new ArrayList<>());
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
            vocMap.putIfAbsent(floorTime, new ArrayList<>());
            vocMap.get(floorTime).add(readingAud.getVoc());
            heatIndexMap.putIfAbsent(floorTime, new ArrayList<>());
            heatIndexMap.get(floorTime).add(readingAud.getHeatIndex());
            co2Map.putIfAbsent(floorTime, new ArrayList<>());
            co2Map.get(floorTime).add(readingAud.getCo2());
            humidityMap.putIfAbsent(floorTime, new ArrayList<>());
            humidityMap.get(floorTime).add(readingAud.getHumidity());
        }

        MeanData meanData = new MeanData();
        meanData.setAqiGraph(new ArrayList<>());
        meanData.setVocGraph(new ArrayList<>());
        meanData.setHeatIndexGraph(new ArrayList<>());
        meanData.setCo2Graph(new ArrayList<>());
        meanData.setTemperatureGraph(new ArrayList<>());
        meanData.setHumidityGraph(new ArrayList<>());
        meanData.setPressureGraph(new ArrayList<>());

        meanData.setAqiMedianGraph(new ArrayList<>());
        meanData.setTemperatureMedianGraph(new ArrayList<>());
        meanData.setHumidityMedianGraph(new ArrayList<>());
        meanData.setPressureMedianGraph(new ArrayList<>());
        meanData.setVocMedianGraph(new ArrayList<>());
        meanData.setHeatIndexMedianGraph(new ArrayList<>());
        meanData.setCo2MedianGraph(new ArrayList<>());
        for(LocalDateTime dateTime: pressureMap.keySet()) {
            double count = pressureMap.get(dateTime).size() * 1.0d;
            meanData.getPressureGraph().add(new ParamDataPoint(dateTime,
                    findMean(pressureMap.get(dateTime))));
            meanData.getTemperatureGraph().add(new ParamDataPoint(dateTime,
                    findMean(temparatureMap.get(dateTime))));
            meanData.getAqiGraph().add(new ParamDataPoint(dateTime,
                    findMean(aqiMap.get(dateTime))));
            meanData.getVocGraph().add(new ParamDataPoint(dateTime,
                    findMean(vocMap.get(dateTime))));
            meanData.getHeatIndexGraph().add(new ParamDataPoint(dateTime,
                    findMean(heatIndexMap.get(dateTime))));
            meanData.getCo2Graph().add(new ParamDataPoint(dateTime,
                    findMean(co2Map.get(dateTime))));
            meanData.getHumidityGraph().add(new ParamDataPoint(dateTime,
                    findMean(humidityMap.get(dateTime))));

            meanData.getPressureMedianGraph().add(new ParamDataPoint(dateTime,
                    findMedian(pressureMap.get(dateTime))));
            meanData.getTemperatureMedianGraph().add(new ParamDataPoint(dateTime,
                    findMedian(temparatureMap.get(dateTime))));
            meanData.getAqiMedianGraph().add(new ParamDataPoint(dateTime,
                    findMedian(aqiMap.get(dateTime))));
            meanData.getVocMedianGraph().add(new ParamDataPoint(dateTime,
                    findMedian(vocMap.get(dateTime))));
            meanData.getHeatIndexMedianGraph().add(new ParamDataPoint(dateTime,
                    findMedian(heatIndexMap.get(dateTime))));
            meanData.getCo2MedianGraph().add(new ParamDataPoint(dateTime,
                    findMedian(co2Map.get(dateTime))));
            meanData.getHumidityMedianGraph().add(new ParamDataPoint(dateTime,
                    findMedian(humidityMap.get(dateTime))));
        }
        return meanData;
    }

    @Override
    public AlertThreshold getAllThresholdValue(Integer deviceId) {
        return alertThresholdRepository.findAllByDeviceId(deviceId);

    }

    @Override
    public AlertThreshold addAllThresholdValue(Integer deviceId, ParameterThresholdDto parameterThresholdDto) {
        AlertThreshold existingAlertThreshold = alertThresholdRepository.findAllByDeviceId(deviceId);
        if(existingAlertThreshold == null){
            existingAlertThreshold = new AlertThreshold();
        }
        if(parameterThresholdDto.getAqiThreshold() != null){
            existingAlertThreshold.setAqiThresholdValue(parameterThresholdDto.getAqiThreshold());
        }
        if(parameterThresholdDto.getHumidityThreshold() != null){
            existingAlertThreshold.setHumidityThresholdValue(parameterThresholdDto.getHumidityThreshold());
        }
        if(parameterThresholdDto.getVocThreshold() != null){
            existingAlertThreshold.setVocThresholdValue(parameterThresholdDto.getVocThreshold());
        }
        if(parameterThresholdDto.getTemperatureThreshold() != null){
            existingAlertThreshold.setTemperatureThresholdValue(parameterThresholdDto.getTemperatureThreshold());
        }
        if(parameterThresholdDto.getPressureThreshold() != null){
            existingAlertThreshold.setPressureThresholdValue(parameterThresholdDto.getPressureThreshold());
        }
        if(parameterThresholdDto.getCo2Threshold() != null){
            existingAlertThreshold.setCo2(parameterThresholdDto.getCo2Threshold());
        }
        return alertThresholdRepository.save(existingAlertThreshold);
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

        return roundOff((double)(list.get((n - 1) / 2) + list.get(n / 2)) / 2.0, 2);
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

        return roundOff((double)sum / (double)n, 2);
    }

    public static double roundOff(Double value, Integer precision) {
        if(value == null){
            return 0.0;
        }
        return Math.floor(Math.round(value * Math.pow(10, precision)) / Math.pow(10, precision));
    }
}
