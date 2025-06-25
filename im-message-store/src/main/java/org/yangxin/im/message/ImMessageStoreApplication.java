package org.yangxin.im.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.yangxin.im.message.dao.mapper")
public class ImMessageStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImMessageStoreApplication.class, args);
    }
}




