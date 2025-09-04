package com.hdfcbank.pacs004;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.hdfcbank"})
public class NeftIlPacs004ProcessorApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeftIlPacs004ProcessorApplication.class, args);
	}

}
