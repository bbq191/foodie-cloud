package com.imooc.cart.controller;

import com.imooc.cart.service.CartService;
import com.imooc.controller.BaseController;
import com.imooc.pojo.IMOOCJSONResult;
import com.imooc.pojo.ShopCartBo;
import com.imooc.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(
    value = "购物车接口controller",
    tags = {"购物车接口相关的api"})
@RequestMapping("shopcart")
@RestController
public class ShopcatController extends BaseController {

  @Autowired private RedisOperator redisOperator;

  @Autowired private CartService cartService;

  @ApiOperation(value = "添加商品到购物车", notes = "添加商品到购物车", httpMethod = "POST")
  @PostMapping("/add")
  public IMOOCJSONResult add(
      @RequestParam String userId,
      @RequestBody ShopCartBo shopCartBo,
      HttpServletRequest request,
      HttpServletResponse response) {

    if (StringUtils.isBlank(userId)) {
      return IMOOCJSONResult.errorMsg("");
    }

    System.out.println(shopCartBo);

    cartService.addItemToCart(userId, shopCartBo);
    return IMOOCJSONResult.ok();
  }

  @ApiOperation(value = "从购物车中删除商品", notes = "从购物车中删除商品", httpMethod = "POST")
  @PostMapping("/del")
  public IMOOCJSONResult del(
      @RequestParam String userId,
      @RequestParam String itemSpecId,
      HttpServletRequest request,
      HttpServletResponse response) {

    if (StringUtils.isBlank(userId) || StringUtils.isBlank(itemSpecId)) {
      return IMOOCJSONResult.errorMsg("参数不能为空");
    }

    cartService.removeItemFromCart(userId, itemSpecId);
    return IMOOCJSONResult.ok();
  }

  // TODO 1） 购物车清空功能
  //      2) 加减号 - 添加、减少商品数量
  //         +1 -1 -1 = 0  =>  -1 -1 +1 = 1 (问题： 如何保证前端请求顺序执行)

}
