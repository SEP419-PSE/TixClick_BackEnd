package com.pse.tixclick;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TixclickApplication {

	public static void main(String[] args) {
		SpringApplication.run(TixclickApplication.class, args);
	}

}
