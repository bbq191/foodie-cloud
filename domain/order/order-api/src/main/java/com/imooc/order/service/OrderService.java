package com.imooc.order.service;

import com.imooc.order.pojo.OrderStatus;
import com.imooc.order.pojo.bo.center.PlaceOrderBo;
import com.imooc.order.pojo.vo.OrderVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author afu
 */
@FeignClient("foodie-order-service")
@RequestMapping("order-api ")
public interface OrderService {

  /**
   * 用于创建订单相关信息
   *
   * @param orderBo 提交数据
   * @return 订单详情 vo
   */
  @PostMapping("placeOrder")
  OrderVo createOrder(@RequestBody PlaceOrderBo orderBo);

  /**
   * 修改订单状态
   *
   * @param orderId 订单 id
   * @param orderStatus 订单状态
   */
  @PostMapping("updateStatus")
  void updateOrderStatus(
      @RequestParam("orderId") String orderId, @RequestParam("orderStatus") Integer orderStatus);

  /**
   * 查询订单状态
   *
   * @param orderId 订单id
   * @return 订单状态
   */
  @GetMapping("orderStatus")
  OrderStatus queryOrderStatusInfo(@RequestParam("orderId") String orderId);

  /** 关闭超时未支付订单 */
  @PostMapping("closePendingOrders")
  void closeOrder();
}
