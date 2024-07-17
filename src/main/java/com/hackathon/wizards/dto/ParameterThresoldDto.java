package com.hackathon.wizards.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParameterThresoldDto {

    private Double temperatureThresold;
    private Double humidityThresold;
    private Double pressureThresold;

}
