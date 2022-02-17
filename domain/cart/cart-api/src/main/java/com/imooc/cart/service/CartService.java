package com.imooc.cart.service;

import com.imooc.pojo.ShopCartBo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** @author afu */
@RequestMapping("cart-api")
public interface CartService {

  @PostMapping("addItem")
  boolean addItemToCart(@RequestParam("userId") String userId, @RequestBody ShopCartBo shopCartBo);

  @PostMapping("removeItem")
  boolean removeItemFromCart(
      @RequestParam("userId") String userId, @RequestParam("itemSpecId") String itemSpecId);

  @PostMapping("clearCart")
  boolean clearCart(@RequestParam("userId") String userId);
}
