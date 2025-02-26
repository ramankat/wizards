package com.hackathon.wizards.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="v1/test")
@Slf4j
public class TestController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping()
    {
        return new ResponseEntity<>("Pong", HttpStatus.OK );
    }
}
