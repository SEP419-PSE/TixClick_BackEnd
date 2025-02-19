package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.BackgroundDTO;
import com.pse.tixclick.payload.request.create.CreateBackgroundRequest;
import com.pse.tixclick.payload.request.update.UpdateBackgroundRequest;

import java.util.List;

public interface BackgroundService {
    BackgroundDTO createBackground(CreateBackgroundRequest createBackgroundRequest);

    List<BackgroundDTO> getAllBackgrounds();

    void deleteBackground(int id);

    BackgroundDTO updateBackground(UpdateBackgroundRequest updateBackgroundRequest, int id);
}
