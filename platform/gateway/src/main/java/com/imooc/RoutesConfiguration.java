package com.imooc;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutesConfiguration {
  @Bean
  public RouteLocator routes(RouteLocatorBuilder builder) {
    return builder
        .routes()
        // FIXME search服务自己实现
        .route(
            r ->
                r.path("/search/**", "/index/**", "/items/search", "/items/catItems")
                    .uri("lb://FOODIE-SEARCH-SERVICE"))
        // 配置url pattern经常会漏掉某些字符导致转发出错，同学们发现视频里提到的那个导致错误的彩蛋了吗？
        .route(
            r ->
                r.path("/address/**", "/passport/**", "/userInfo/**", "/center/**")
                    .uri("lb://FOODIE-USER-SERVICE"))
        .route(r -> r.path("/items/**").uri("lb://FOODIE-ITEM-SERVICE"))
        .route(r -> r.path("/shopcart/**").uri("lb://FOODIE-CART-SERVICE"))
        .route(
            r ->
                r.path("/orders/**", "/myorders/**", "/mycomments/**")
                    .uri("lb://FOODIE-ORDER-SERVICE"))
        .build();
  }
}