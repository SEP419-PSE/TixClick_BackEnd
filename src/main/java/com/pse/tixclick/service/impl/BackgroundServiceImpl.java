package com.pse.tixclick.service.impl;

import com.pse.tixclick.config.Util;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.BackgroundDTO;
import com.pse.tixclick.payload.entity.seatmap.Background;
import com.pse.tixclick.payload.request.create.CreateBackgroundRequest;
import com.pse.tixclick.payload.request.update.UpdateBackgroundRequest;
import com.pse.tixclick.repository.BackgroundRepository;
import com.pse.tixclick.service.BackgroundService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BackgroundServiceImpl implements BackgroundService {
    @Autowired
    BackgroundRepository backgroundRepository;

    @Autowired
    Util util;

    @Autowired
    ModelMapper mapper;


    @Override
    public BackgroundDTO createBackground(CreateBackgroundRequest createBackgroundRequest) {
        Background background = new Background();
        background.setBackgroundName(createBackgroundRequest.getBackgroundName());
        background.setType(createBackgroundRequest.getType());
        background.setValue(createBackgroundRequest.getValue());

        backgroundRepository.save(background);
        return mapper.map(background, BackgroundDTO.class);
  }

    @Override
    public List<BackgroundDTO> getAllBackgrounds() {
        List<Background> backgrounds = backgroundRepository.findAll();

        return backgrounds.stream()
                .map(background -> mapper.map(background, BackgroundDTO.class))
                .toList();
    }

    @Override
    public void deleteBackground(int id) {
        Background background = backgroundRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BACKGROUND_NOT_FOUND));

        backgroundRepository.delete(background);
    }

    @Override
    public BackgroundDTO updateBackground(UpdateBackgroundRequest updateBackgroundRequest, int id) {
        Background background = backgroundRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BACKGROUND_NOT_FOUND));

        background.setBackgroundName(updateBackgroundRequest.getBackgroundName());
        background.setType(updateBackgroundRequest.getType());
        background.setValue(updateBackgroundRequest.getValue());

        background = backgroundRepository.save(background);
        return mapper.map(background, BackgroundDTO.class);
    }
}
