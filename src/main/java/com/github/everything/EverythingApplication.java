package com.github.everything;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication
//@EnableAdminServer
public class EverythingApplication {

    public static void main(String[] args) {
        System.out.println("##############启动开始##############");
        SpringApplication.run(EverythingApplication.class, args);
        System.out.println("##############启动成功##############");
    }

}
