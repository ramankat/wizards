package com.hackathon.wizards.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlertDataPoint {
    private LocalDateTime date;
    private Integer count;
}
