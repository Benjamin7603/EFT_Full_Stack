package com.duoc.ms_reportes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients; // Nueva importación

@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class MsReportesApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsReportesApplication.class, args);
	}

}