package com.imooc.user.service.impl;

import com.imooc.enums.Sex;
import com.imooc.user.mapper.UsersMapper;
import com.imooc.user.pojo.Users;
import com.imooc.user.pojo.bo.UserBo;
import com.imooc.user.service.UserService;
import com.imooc.utils.DateUtil;
import com.imooc.utils.MD5Utils;
import java.util.Date;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

/** @author afu */
@RestController
public class UserServiceImpl implements UserService {

  @Autowired private UsersMapper usersMapper;
  @Autowired private Sid sid;
  private static final String USER_FACE =
      "http://122.152.205.72:88/group1/M00/00/05/CpoxxFw_8_qAIlFXAAAcIhVPdSg994.png";

  @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
  @Override
  public boolean queryUsernameIsExist(String username) {
    Example userExample = new Example(Users.class);
    Example.Criteria userCriteria = userExample.createCriteria();
    userCriteria.andEqualTo("username", username);
    Users result = usersMapper.selectOneByExample(userExample);
    return result != null;
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  @Override
  public Users createUser(UserBo userBo) {
    String userId = sid.nextShort();
    Users user = new Users();
    user.setId(userId);
    user.setUsername(userBo.getUsername());
    try {
      user.setPassword(MD5Utils.getMD5Str(userBo.getPassword()));
    } catch (Exception e) {
      e.printStackTrace();
    }
    user.setNickname(userBo.getUsername());
    user.setFace(USER_FACE);
    user.setBirthday(DateUtil.stringToDate("1900-01-01"));
    user.setSex(Sex.secret.type);
    user.setCreatedTime(new Date());
    user.setUpdatedTime(new Date());
    // 开始注册
    usersMapper.insert(user);
    return user;
  }

  @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
  @Override
  public Users queryUserForLogin(String username, String password) {
    Example userExample = new Example(Users.class);
    Example.Criteria userCriteria = userExample.createCriteria();
    userCriteria.andEqualTo("username", username);
    userCriteria.andEqualTo("password", password);
    return usersMapper.selectOneByExample(userExample);
  }
}
