package com.hackathon.wizards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HeatMapPoint {
    private Double latitude;
    private Double longitude;
    private Integer count;
}
