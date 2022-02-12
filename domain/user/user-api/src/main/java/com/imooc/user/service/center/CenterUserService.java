package com.imooc.user.service.center;

import com.imooc.user.pojo.Users;
import com.imooc.user.pojo.bo.center.CenterUserBo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** @author afu */
@RestController("center-user-api")
public interface CenterUserService {
  /**
   * 根据用户id查询用户信息
   *
   * @param userId 用户id
   * @return 用户对象
   */
  @GetMapping("profile")
  Users queryUserInfo(@RequestParam("userId") String userId);

  /**
   * 修改用户信息
   *
   * @param userId 用户id
   * @param centerUserBo 用户信息参数
   * @return 更新后的用户对象
   */
  @PutMapping("profile/{userId}")
  Users updateUserInfo(
      @RequestParam("userId") String userId, @RequestBody CenterUserBo centerUserBo);

  /**
   * 用户头像更新
   *
   * @param userId 用户id
   * @param faceUrl 头像url
   * @return 用户信息
   */
  @PostMapping("updatePhoto")
  Users updateUserFace(
      @RequestParam("userId") String userId, @RequestParam("faceUrl") String faceUrl);
}
