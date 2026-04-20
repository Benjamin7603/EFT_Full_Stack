package com.duoc.ms_geografico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class MsGeograficoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsGeograficoApplication.class, args);
	}

}
