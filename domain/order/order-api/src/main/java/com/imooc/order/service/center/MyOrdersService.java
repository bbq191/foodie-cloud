package com.imooc.order.service.center;

import com.imooc.order.pojo.Orders;
import com.imooc.order.pojo.vo.OrderStatusCountsVo;
import com.imooc.pojo.IMOOCJSONResult;
import com.imooc.pojo.PagedGridResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author afu
 */
@FeignClient("foodie-order-service")
@RequestMapping("myorder-api")
public interface MyOrdersService {

  /**
   * 查询我的订单列表
   *
   * @param userId 用户id
   * @param orderStatus 订单状态
   * @param page 第几页
   * @param pageSize 每页数量
   * @return 订单分页列表
   */
  @GetMapping("order/query")
  PagedGridResult queryMyOrders(
      @RequestParam("userId") String userId,
      @RequestParam("orderStatus") Integer orderStatus,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "pageSize", required = false) Integer pageSize);

  /**
   * 订单状态 --> 商家发货
   *
   * @param orderId 订单id
   */
  @PostMapping("order/delivered")
  void updateDeliverOrderStatus(@RequestParam("userId") String orderId);

  /**
   * 查询我的订单
   *
   * @param userId 用户 id
   * @param orderId 订单 id
   * @return 用户关联订单
   */
  @GetMapping("order/details")
  Orders queryMyOrder(
      @RequestParam("userId") String userId, @RequestParam("orderId") String orderId);

  /**
   * 更新订单状态 —> 确认收货
   *
   * @param orderId 订单id
   * @return 成功失败
   */
  @PostMapping("order/received")
  boolean updateReceiveOrderStatus(@RequestParam("orderId") String orderId);

  /**
   * 删除订单（逻辑删除）
   *
   * @param userId 用户 id
   * @param orderId 订单id
   * @return 成功失败
   */
  @DeleteMapping("order")
  boolean deleteOrder(
      @RequestParam("userId") String userId, @RequestParam("orderId") String orderId);

  /**
   * 查询用户订单数
   *
   * @param userId 用户id
   * @return 分状态订单数量
   */
  @GetMapping("order/counts")
  OrderStatusCountsVo getOrderStatusCounts(@RequestParam("userId") String userId);

  /**
   * 获得分页的订单动向
   *
   * @param userId 用户id
   * @param page 分页
   * @param pageSize 每页数量
   * @return 订单动向分页列表
   */
  @GetMapping("order/trend")
  PagedGridResult getOrdersTrend(
      @RequestParam("userId") String userId,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "pageSize", required = false) Integer pageSize);

  /**
   * 用于验证用户和订单是否有关联关系，避免非法用户调用
   *
   * @param userId 用户id
   * @param orderId 订单id
   * @return 检查结果
   */
  @GetMapping("checkUserOrder")
  IMOOCJSONResult checkUserOrder(
      @RequestParam("userId") String userId, @RequestParam("orderId") String orderId);
  //  public IMOOCJSONResult checkUserOrder(String userId, String orderId) {
  //    Orders order = myOrdersService.queryMyOrder(userId, orderId);
  //    if (order == null) {
  //      return IMOOCJSONResult.errorMsg("订单不存在！");
  //    }
  //    return IMOOCJSONResult.ok(order);
  //  }
}
