package com.imooc.auth.service;

import com.imooc.auth.service.pojo.Account;
import com.imooc.auth.service.pojo.AuthResponse;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("foodie-auth-service")
@RequestMapping("auth-service")
public interface AuthService {

  @PostMapping("token")
  public AuthResponse tokenize(@RequestParam("userId") String userId);

  @PostMapping("verify")
  public AuthResponse verify(@RequestBody Account account);

  @PostMapping("refresh")
  public AuthResponse refresh(@RequestParam("refresh") String refresh);

  @DeleteMapping("delete")
  public AuthResponse delete(@RequestBody Account account);
}
