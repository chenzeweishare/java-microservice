package com.itheima;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class MzyDispatchServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MzyDispatchServiceApplication.class, args);
	}

}
