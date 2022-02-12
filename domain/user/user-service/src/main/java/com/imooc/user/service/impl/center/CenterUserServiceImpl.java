package com.imooc.user.service.impl.center;

import com.imooc.user.mapper.UsersMapper;
import com.imooc.user.pojo.Users;
import com.imooc.user.pojo.bo.center.CenterUserBo;
import com.imooc.user.service.center.CenterUserService;
import java.util.Date;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

/** @author afu */
@RestController
public class CenterUserServiceImpl implements CenterUserService {
  @Autowired private UsersMapper usersMapper;

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  @Override
  public Users queryUserInfo(String userId) {
    Users user = usersMapper.selectByPrimaryKey(userId);
    //    user.setPassword(null);
    return user;
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  @Override
  public Users updateUserInfo(String userId, CenterUserBo centerUserBo) {
    Users updateUser = new Users();
    BeanUtils.copyProperties(centerUserBo, updateUser);
    updateUser.setId(userId);
    updateUser.setUpdatedTime(new Date());
    usersMapper.updateByPrimaryKeySelective(updateUser);
    return queryUserInfo(userId);
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  @Override
  public Users updateUserFace(String userId, String faceUrl) {
    Users updateUser = new Users();
    updateUser.setId(userId);
    updateUser.setFace(faceUrl);
    updateUser.setUpdatedTime(new Date());

    usersMapper.updateByPrimaryKeySelective(updateUser);

    return queryUserInfo(userId);
  }
}
