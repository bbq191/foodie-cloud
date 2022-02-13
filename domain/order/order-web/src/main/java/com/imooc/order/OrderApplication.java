package com.imooc.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

/** @author afu */
@SpringBootApplication
@MapperScan(basePackages = "com.imooc.order.mapper")
@ComponentScan(basePackages = {"com.imooc", "org.n3r.idworker"})
@EnableDiscoveryClient
@EnableScheduling
// todo feign
public class OrderApplication {
  public static void main(String[] args) {
    SpringApplication.run(OrderApplication.class);
  }
}
