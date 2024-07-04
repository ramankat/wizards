package com.hackathon.wizards.service;

import com.hackathon.wizards.dto.ReadingRequest;
import com.hackathon.wizards.entity.Reading;
import java.util.List;

public interface ReadingService {

    void saveReading(ReadingRequest readingRequest);

    List<Reading> getAllReadings();

    Reading getReadingDetail(Integer id);


}
