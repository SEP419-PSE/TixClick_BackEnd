package com.pse.tixclick.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.modelmapper.ModelMapper;

@Configuration
public class ModelMapperConfig {  // Đổi tên từ ModelMapper -> ModelMapperConfig
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
