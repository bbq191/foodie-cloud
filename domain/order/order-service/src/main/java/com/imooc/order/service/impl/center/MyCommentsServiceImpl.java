package com.imooc.order.service.impl.center;

import com.imooc.enums.YesOrNo;
import com.imooc.item.service.BaseService;
import com.imooc.order.fallback.itermservice.ItemCommentsFeignClient;
import com.imooc.order.mapper.OrderItemsMapper;
import com.imooc.order.mapper.OrderStatusMapper;
import com.imooc.order.mapper.OrdersMapper;
import com.imooc.order.pojo.OrderItems;
import com.imooc.order.pojo.OrderStatus;
import com.imooc.order.pojo.Orders;
import com.imooc.order.pojo.bo.center.OrderItemsCommentBo;
import com.imooc.order.service.center.MyCommentsService;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author afu
 */
@RestController
public class MyCommentsServiceImpl extends BaseService implements MyCommentsService {
  @Autowired private OrderItemsMapper orderItemsMapper;
  @Autowired private OrdersMapper ordersMapper;
  @Autowired private OrderStatusMapper orderStatusMapper;
  @Autowired private Sid sid;
  //  @Autowired private ItemCommentsService itemCommentsService;
  @Autowired private ItemCommentsFeignClient itemCommentsService;

  @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
  @Override
  public List<OrderItems> queryPendingComment(String orderId) {
    OrderItems query = new OrderItems();
    query.setOrderId(orderId);
    return orderItemsMapper.select(query);
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  @Override
  public void saveComments(String orderId, String userId, List<OrderItemsCommentBo> commentList) {
    // 1. 保存评价 items_comments
    for (OrderItemsCommentBo oic : commentList) {
      oic.setCommentId(sid.nextShort());
    }
    Map<String, Object> map = new HashMap<>(10);
    map.put("userId", userId);
    map.put("commentList", commentList);
    itemCommentsService.saveComments(map);
    // 2. 修改订单表改已评价 orders
    Orders order = new Orders();
    order.setId(orderId);
    order.setIsComment(YesOrNo.YES.type);
    ordersMapper.updateByPrimaryKeySelective(order);
    // 3. 修改订单状态表的留言时间 order_status
    OrderStatus orderStatus = new OrderStatus();
    orderStatus.setOrderId(orderId);
    orderStatus.setCommentTime(new Date());
    orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
  }
}
