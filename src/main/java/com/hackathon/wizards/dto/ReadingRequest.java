package com.hackathon.wizards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadingRequest {

    private Integer deviceId = 1;
    private Integer aqi;
    private Double temperature;
    private Double pressure;
    private Double longitude;
    private Double latitude;
    private Double humidity;
    private Double altitude;
    private Boolean sosAlert = false;

}
