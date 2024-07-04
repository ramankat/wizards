package com.hackathon.wizards.dto;

import java.util.List;
import lombok.Data;

@Data
public class AlertChart {
    private List<AlertDataPoint> alerts;
}
