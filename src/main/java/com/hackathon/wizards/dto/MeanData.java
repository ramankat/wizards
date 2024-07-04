package com.hackathon.wizards.dto;

import java.util.List;
import lombok.Data;

@Data
public class MeanData {
    private List<ParamDataPoint> humidityGraph;
    private List<ParamDataPoint> temperatureGraph;
    private List<ParamDataPoint> pressureGraph;
    private List<ParamDataPoint> aqiGraph;

    private List<ParamDataPoint> humidityMedianGraph;
    private List<ParamDataPoint> temperatureMedianGraph;
    private List<ParamDataPoint> pressureMedianGraph;
    private List<ParamDataPoint> aqiMedianGraph;
}
