package com.hackathon.wizards.service;

import com.hackathon.wizards.dto.HeatMapGraph;
import com.hackathon.wizards.dto.HeatMapPoint;
import com.hackathon.wizards.entity.HeatMapData;
import com.hackathon.wizards.repository.HeatMapDataRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GraphServiceImpl implements GraphService {
    @Autowired
    private HeatMapDataRepository heatMapDataRepository;

    @Override
    public HeatMapGraph createHeatMap(Integer precision) {
        HeatMapGraph graph = new HeatMapGraph();
        List<HeatMapPoint> heatMapPointList = new ArrayList<>();
        List<HeatMapData> heatMapDataList = heatMapDataRepository.findAll();
        Map<Double, Map<Double, Integer>> heatMap = new HashMap<>();
        heatMapDataList.forEach(heatMapData -> {
            Double latitude = Math.floor(heatMapData.getLatitude() * Math.pow(10.0, precision)) / Math.pow(10.0, precision);
            Double longitude = Math.floor(heatMapData.getLongitude() * Math.pow(10.0, precision)) / Math.pow(10.0, precision);
            heatMap.putIfAbsent(latitude, new HashMap<>());
            heatMap.get(latitude).putIfAbsent(longitude, 1);
            heatMap.get(latitude).put(longitude, heatMap.get(latitude).get(longitude) + 1);
        });
        for(Double latitude: heatMap.keySet()) {
            for(Double longitude: heatMap.get(latitude).keySet()) {
                heatMapPointList.add(new HeatMapPoint(latitude, longitude, heatMap.get(latitude).get(longitude)));
            }
        }
        graph.setHeatMapPointList(heatMapPointList);
        return graph;
    }
}
