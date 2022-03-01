package com.imooc.order.service.center;

import com.imooc.order.pojo.OrderItems;
import com.imooc.order.pojo.bo.center.OrderItemsCommentBo;
import java.util.List;
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
@RequestMapping("order-comments-api")
public interface MyCommentsService {

  /**
   * 根据订单id查询关联的商品待评价列表
   *
   * @param orderId 订单编号
   * @return 评价列表
   */
  @GetMapping("orderItems")
  List<OrderItems> queryPendingComment(@RequestParam("orderId") String orderId);

  /**
   * 保存用户的评论
   *
   * @param orderId 订单编号
   * @param userId 用户编号
   * @param commentList 前端传入评论列表
   */
  @PostMapping("saveOrderComments")
  void saveComments(
      @RequestParam("orderId") String orderId,
      @RequestParam("userId") String userId,
      @RequestBody List<OrderItemsCommentBo> commentList);
}
