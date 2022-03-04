package com.imooc.order;

import com.imooc.item.service.ItemService;
import com.imooc.order.fallback.itermservice.ItemCommentsFeignClient;
import com.imooc.user.service.AddressService;
import com.imooc.user.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author afu
 */
@SpringBootApplication
@MapperScan(basePackages = "com.imooc.order.mapper")
@ComponentScan(basePackages = {"com.imooc", "org.n3r.idworker"})
@EnableDiscoveryClient
@EnableScheduling
@EnableFeignClients(
    clients = {
      ItemCommentsFeignClient.class,
      ItemService.class,
      UserService.class,
      AddressService.class
    })
public class OrderApplication {
  public static void main(String[] args) {
    SpringApplication.run(OrderApplication.class);
  }
}
