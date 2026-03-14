package com.smartmedical;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.smartmedical.mapper")
public class SmartMedicalApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartMedicalApplication.class, args);
    }
}


