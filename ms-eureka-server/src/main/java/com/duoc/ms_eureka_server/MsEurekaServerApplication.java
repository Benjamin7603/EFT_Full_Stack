package com.duoc.ms_eureka_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@EnableEurekaServer
@SpringBootApplication
public class MsEurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsEurekaServerApplication.class, args);
	}

}
