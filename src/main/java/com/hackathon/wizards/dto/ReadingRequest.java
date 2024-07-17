package com.hackathon.wizards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadingRequest {

    private Integer deviceId = 1;
    private Double aqi;
    private Double voc;
    private Double heatIndex;
    private Double co2;
    private Double temperature;
    private Double pressure;
    private Double longitude;
    private Double latitude;
    private Double humidity;
    private Double altitude;
    private Boolean sosAlert = false;

}
