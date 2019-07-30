package com.itheima;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class MzyOrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MzyOrderServiceApplication.class, args);
	}

}
