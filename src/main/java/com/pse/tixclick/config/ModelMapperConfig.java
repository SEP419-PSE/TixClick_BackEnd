package com.pse.tixclick.config;

import com.pse.tixclick.payload.dto.EventActivityDTO;
import com.pse.tixclick.payload.dto.EventDTO;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventActivity;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.modelmapper.ModelMapper;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(Event.class, EventDTO.class).addMappings(mapper -> {
            mapper.map(src -> src.getCategory().getEventCategoryId(), EventDTO::setCategoryId);
            mapper.map(src -> src.getOrganizer().getAccountId(), EventDTO::setOrganizerId);
        });
        modelMapper.addMappings(new PropertyMap<EventActivity, EventActivityDTO>() {
            @Override
            protected void configure() {
                map().setEventId(source.getEvent().getEventId());
                map().setCreatedBy(source.getCreatedBy().getAccountId());
            }
        });
        return modelMapper;
    }
}