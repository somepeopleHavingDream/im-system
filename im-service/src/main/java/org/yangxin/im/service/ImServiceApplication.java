package org.yangxin.im.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.yangxin.im.service", "org.yangxin.im.common"})
@MapperScan("org.yangxin.im.service.*.dao.mapper")
public class ImServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImServiceApplication.class, args);
    }
}




