package com.imooc.user.service;

import com.imooc.user.pojo.UserAddress;
import com.imooc.user.pojo.bo.AddressBo;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** @author afu */
@RestController("address-api")
public interface AddressService {

  /**
   * 根据用户 id 查询用户地址列表
   *
   * @param userId 用户 id
   * @return 地址列表
   */
  @GetMapping("addressList")
  List<UserAddress> queryAll(@RequestParam("userId") String userId);

  /**
   * 用户新增地址
   *
   * @param addressBo 地址业务参数
   */
  @PostMapping("address")
  void addNewUserAddress(@RequestBody AddressBo addressBo);

  /**
   * 修改用户地址
   *
   * @param addressBo 地址业务参数
   */
  @PutMapping("address")
  void updateUserAddress(@RequestBody AddressBo addressBo);

  /**
   * 根据传入的地址及用户 id 删除对应信息
   *
   * @param addressId 地址 id
   * @param userId 用户 id
   */
  @DeleteMapping("address")
  void deleteUserAddress(
      @RequestParam("addressId") String addressId, @RequestParam("userId") String userId);

  /**
   * 根据传入的地址及用户 id 修改默认地址
   *
   * @param addressId 地址 id
   * @param userId 用户 id
   */
  @PostMapping("setDefaultAddress")
  void updateUserAddressToDefualt(
      @RequestParam("addressId") String addressId, @RequestParam("userId") String userId);

  /**
   * 根据地址及用户 id 获取地址信息
   *
   * @param addressId 地址 id
   * @param userId 用户 id
   * @return 地址信息
   */
  @GetMapping("queryAddress")
  UserAddress queryUserAddres(
      @RequestParam("addressId") String addressId, @RequestParam("userId") String userId);
}
