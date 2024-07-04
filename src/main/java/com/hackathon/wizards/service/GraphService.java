package com.hackathon.wizards.service;

import com.hackathon.wizards.dto.HeatMapGraph;

public interface GraphService {
    HeatMapGraph createHeatMap(Integer precision);
}
