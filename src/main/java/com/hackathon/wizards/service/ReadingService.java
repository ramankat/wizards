package com.hackathon.wizards.service;

import com.hackathon.wizards.dto.*;
import com.hackathon.wizards.entity.Reading;
import java.util.List;

public interface ReadingService {

    Boolean saveReading(ReadingRequest readingRequest);

    List<Reading> getAllReadings();

    DeviceData getReadingDetail(Long id, Integer dataPoints);

    AlertChart getAlertsChart(Integer days);

    MeanData getMeanChart(Integer days);

    ParameterThresoldDto getAllThesholdValue();

}
