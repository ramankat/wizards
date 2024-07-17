package com.hackathon.wizards.dto;

import com.hackathon.wizards.entity.Reading;
import java.util.List;
import lombok.Data;

@Data
public class DeviceData {
    private Reading currReading;
    private List<ParamDataPoint> humidityGraph;
    private List<ParamDataPoint> temperatureGraph;
    private List<ParamDataPoint> pressureGraph;
    private List<ParamDataPoint> aqiGraph;
    private List<ParamDataPoint> vocGraph;
    private List<ParamDataPoint> heatIndexGraph;
    private List<ParamDataPoint> co2Graph;


    private Double avgHumidity;
    private Double avgTemperature;
    private Double avgPressure;
    private Double avgAqi;

    private Double medianHumidity;
    private Double medianTemperature;
    private Double medianPressure;
    private Double medianAqi;
}
