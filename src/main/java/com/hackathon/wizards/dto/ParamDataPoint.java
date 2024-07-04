package com.hackathon.wizards.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParamDataPoint {
    private LocalDateTime date;
    private Double value;
}
