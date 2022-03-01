package com.imooc.cart.service.impl;

import static com.imooc.controller.BaseController.FOODIE_SHOPCART;

import com.imooc.cart.service.CartService;
import com.imooc.pojo.ShopCartBo;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author afu
 */
@RestController
@Slf4j
public class CartServiceImpl implements CartService {

  @Autowired private RedisOperator redisOperator;

  @Override
  public boolean addItemToCart(
      @RequestParam("userId") String userId, @RequestBody ShopCartBo shopCartBo) {
    // 前端用户在登录的情况下，添加商品到购物车，会同时在后端同步购物车到redis缓存
    // 需要判断当前购物车中包含已经存在的商品，如果存在则累加购买数量
    String shopcartJson = redisOperator.get(FOODIE_SHOPCART + ":" + userId);
    List<ShopCartBo> shopcartList;
    if (StringUtils.isNotBlank(shopcartJson)) {
      // redis中已经有购物车了
      shopcartList = JsonUtils.jsonToList(shopcartJson, ShopCartBo.class);
      // 判断购物车中是否存在已有商品，如果有的话counts累加
      boolean isHaving = false;
      assert shopcartList != null;
      for (ShopCartBo sc : shopcartList) {
        String tmpSpecId = sc.getSpecId();
        if (tmpSpecId.equals(shopCartBo.getSpecId())) {
          sc.setBuyCounts(sc.getBuyCounts() + shopCartBo.getBuyCounts());
          isHaving = true;
        }
      }
      if (!isHaving) {
        shopcartList.add(shopCartBo);
      }
    } else {
      // redis中没有购物车
      shopcartList = new ArrayList<>();
      // 直接添加到购物车中
      shopcartList.add(shopCartBo);
    }

    // 覆盖现有redis中的购物车
    redisOperator.set(FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(shopcartList));

    return true;
  }

  @Override
  public boolean removeItemFromCart(
      @RequestParam("userId") String userId, @RequestParam("itemSpecId") String itemSpecId) {
    // 用户在页面删除购物车中的商品数据，如果此时用户已经登录，则需要同步删除redis购物车中的商品
    String shopcartJson = redisOperator.get(FOODIE_SHOPCART + ":" + userId);
    if (StringUtils.isNotBlank(shopcartJson)) {
      // redis中已经有购物车了
      List<ShopCartBo> shopcartList = JsonUtils.jsonToList(shopcartJson, ShopCartBo.class);
      // 判断购物车中是否存在已有商品，如果有的话则删除
      assert shopcartList != null;
      for (ShopCartBo sc : shopcartList) {
        String tmpSpecId = sc.getSpecId();
        if (tmpSpecId.equals(itemSpecId)) {
          shopcartList.remove(sc);
          break;
        }
      }
      // 覆盖现有redis中的购物车
      redisOperator.set(FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(shopcartList));
    }

    return true;
  }

  @Override
  public boolean clearCart(@RequestParam("userId") String userId) {
    redisOperator.del(FOODIE_SHOPCART + ":" + userId);
    return true;
  }
}
