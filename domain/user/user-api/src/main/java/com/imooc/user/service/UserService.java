package com.imooc.user.service;

import com.imooc.user.pojo.Users;
import com.imooc.user.pojo.bo.UserBo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** @author afu */
@RestController("user-api")
public interface UserService {
  /**
   * 判断是否存在相同用户名
   *
   * @param username 用户名
   * @return boolean
   */
  @GetMapping("user/exists")
  boolean queryUsernameIsExist(@RequestParam("username") String username);

  /**
   * 新建用户，注册用户
   *
   * @param userBo 前端传入的业务对象
   * @return 注册后的用户脱敏信息
   */
  @PostMapping("user")
  Users createUser(@RequestBody UserBo userBo);

  /**
   * 检索用户名和密码是否匹配，用于登陆
   *
   * @param username 用户名
   * @param password 密码
   * @return 登陆成功后的用户对象，需要脱敏
   */
  @GetMapping("verify")
  Users queryUserForLogin(
      @RequestParam("username") String username, @RequestParam("password") String password);
}
