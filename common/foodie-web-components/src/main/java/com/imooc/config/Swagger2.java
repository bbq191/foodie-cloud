package com.imooc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * @author afu
 */
@Configuration
public class Swagger2 {
  private ApiInfo apiInfo() {
    return new ApiInfoBuilder()
        .title("天天吃货 电商平台接口api") // 文档页标题
        .contact(new Contact("imooc", "https://www.imooc.com", "abc@imooc.com")) // 联系人信息
        .description("专为天天吃货提供的api文档") // 详细信息
        .version("1.0.1") // 文档版本号
        .termsOfServiceUrl("https://www.imooc.com") // 网站地址
        .build();
  }

  @Bean
  public Docket createRestestApi() {
    return new Docket(DocumentationType.OAS_30)
        // 用于定义api文档汇总信息
        .apiInfo(apiInfo())
        .select()
        // 指定注解为restcontroller就会被抓取
        .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
        // 所有controller
        .paths(PathSelectors.any())
        .build();
  }
}
