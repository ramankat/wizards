package com.hackathon.wizards.controller;


import com.hackathon.wizards.dto.DeviceData;
import com.hackathon.wizards.dto.ReadingRequest;
import com.hackathon.wizards.entity.Reading;
import com.hackathon.wizards.service.ReadingService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="v1/reading")
@Slf4j
@CrossOrigin(origins = "*")
public class ReadingController {


    @Autowired
    private ReadingService readingService;

    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> saveAddressInfo(@RequestBody ReadingRequest readingRequest) {
        readingService.saveReading(readingRequest);
        return new ResponseEntity<>("SUCCESS", HttpStatus.OK);
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Reading>> getAllReadings() {
        return new ResponseEntity<>(readingService.getAllReadings(), HttpStatus.OK);
    }

    @GetMapping(path = "/{id}/{dataPoints}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DeviceData> getReadingDetail(@PathVariable Long id, @PathVariable Integer dataPoints) {
        return new ResponseEntity<>(readingService.getReadingDetail(id, dataPoints), HttpStatus.OK);
    }

}
