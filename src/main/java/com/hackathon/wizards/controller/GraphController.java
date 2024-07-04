package com.hackathon.wizards.controller;

import com.hackathon.wizards.dto.HeatMapGraph;
import com.hackathon.wizards.service.GraphService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="v1/graph")
@CrossOrigin(origins = "/*")
@Slf4j
public class GraphController {

    @Autowired
    private GraphService graphService;

    @PostMapping(path = "/heatmap", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HeatMapGraph> saveAddressInfo(@RequestParam("precision") Integer precision) {
        return new ResponseEntity<>(graphService.createHeatMap(precision), HttpStatus.OK);
    }
}
