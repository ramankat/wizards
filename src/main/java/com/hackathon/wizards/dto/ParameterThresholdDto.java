package com.hackathon.wizards.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParameterThresholdDto {

    private Double temperatureThreshold;
    private Double humidityThreshold;
    private Double pressureThreshold;
    private Double aqiThreshold;
    private Double vocThreshold;
    private Double co2Threshold;

}
