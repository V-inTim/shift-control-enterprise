package com.example.shift_control_enterprise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("file:${user.dir}/.env.properties")
public class ShiftControlEnterpriseApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShiftControlEnterpriseApplication.class, args);
	}

}
