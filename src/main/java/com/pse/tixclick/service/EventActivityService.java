package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.EventActivityDTO;
import com.pse.tixclick.payload.request.CreateEventActivityRequest;

public interface EventActivityService {
    EventActivityDTO createEventActivity(CreateEventActivityRequest eventActivityRequest);


}
