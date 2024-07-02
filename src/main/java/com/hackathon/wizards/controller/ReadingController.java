package com.hackathon.wizards.controller;


import com.hackathon.wizards.dto.ReadingRequest;
import com.hackathon.wizards.service.ReadingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="v1/reading")
@Slf4j
public class ReadingController {


    @Autowired
    private ReadingService readingService;

    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> saveAddressInfo(@RequestBody ReadingRequest readingRequest) {
        readingService.saveReading(readingRequest);
        return new ResponseEntity<>("SUCCESS", HttpStatus.OK);
    }

}
