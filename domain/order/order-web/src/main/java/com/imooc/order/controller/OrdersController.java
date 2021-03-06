package com.imooc.order.controller;

import com.imooc.controller.BaseController;
import com.imooc.enums.OrderStatusEnum;
import com.imooc.enums.PayMethod;
import com.imooc.order.pojo.OrderStatus;
import com.imooc.order.pojo.bo.OrderStatusCheckBo;
import com.imooc.order.pojo.bo.SubmitOrderBo;
import com.imooc.order.pojo.bo.center.PlaceOrderBo;
import com.imooc.order.pojo.vo.MerchantOrdersVo;
import com.imooc.order.pojo.vo.OrderVo;
import com.imooc.order.service.OrderService;
import com.imooc.order.stream.CheckOrderTopic;
import com.imooc.pojo.IMOOCJSONResult;
import com.imooc.pojo.ShopCartBo;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author afu
 */
@Api(
    value = "订单相关",
    tags = {"订单相关的api接口"})
@RequestMapping("orders")
@RestController
public class OrdersController extends BaseController {
  static final Logger logger = LoggerFactory.getLogger(OrdersController.class);
  @Autowired private OrderService orderService;
  @Autowired private RestTemplate restTemplate;
  @Autowired private RedisOperator redisOperator;
  @Autowired private CheckOrderTopic orderStatusProducer;

  @ApiOperation(value = "用户下单", notes = "用户下单", httpMethod = "POST")
  @PostMapping("/create")
  public IMOOCJSONResult create(
      @RequestBody SubmitOrderBo submitOrderBo,
      HttpServletRequest request,
      HttpServletResponse response) {
    //    String orderTokenKey = "ORDER_TOKEN_" + request.getSession().getId();
    //    String lockKey = "LOCK_KEY_" + request.getSession().getId();
    // 解决并发请求进入同时下单成功，保证只有一个请求能拿到锁
    //    RLock lock = redissonClient.getLock(lockKey);
    //    lock.lock(5, TimeUnit.SECONDS);
    //    // 幂等性控制
    //    try {
    //      String orderToken = redisOperator.get(orderTokenKey);
    //      if (StringUtils.isBlank(orderToken)) {
    //        throw new RuntimeException("oreder token 不存在");
    //      }
    //      boolean corretToken = orderToken.equals(submitOrderBo.getToken());
    //      if (!corretToken) {
    //        throw new RuntimeException("order toekn 不正确");
    //      }
    //      redisOperator.del(orderTokenKey);
    //    } finally {
    //      lock.unlock();
    //    }

    if (!submitOrderBo.getPayMethod().equals(PayMethod.WEIXIN.type)
        && !submitOrderBo.getPayMethod().equals(PayMethod.ALIPAY.type)) {
      return IMOOCJSONResult.errorMsg("支付方式不支持！");
    }
    String shopcartJson = redisOperator.get(FOODIE_SHOPCART + ":" + submitOrderBo.getUserId());
    if (StringUtils.isBlank(shopcartJson)) {
      return IMOOCJSONResult.errorMsg("购物数据不正确");
    }
    List<ShopCartBo> shopcartList = JsonUtils.jsonToList(shopcartJson, ShopCartBo.class);
    // 1. 创建订单
    PlaceOrderBo placeOrderBo = new PlaceOrderBo(submitOrderBo, shopcartList);
    OrderVo orderVo = orderService.createOrder(placeOrderBo);
    String orderId = orderVo.getOrderId();

    // 2. 创建订单以后，移除购物车中已结算（已提交）的商品
    // 1001 2002 -> 用户购买 3003 -> 用户购买 4004
    assert shopcartList != null;
    shopcartList.removeAll(orderVo.getToBeRemovedShopcatdList());
    redisOperator.set(
        FOODIE_SHOPCART + ":" + submitOrderBo.getUserId(), JsonUtils.objectToJson(shopcartList));
    // 整合redis之后，完善购物车中的已结算商品清除，并且同步到前端的cookie
    CookieUtils.setCookie(
        request, response, FOODIE_SHOPCART, JsonUtils.objectToJson(shopcartList), true);

    // order status检查
    OrderStatusCheckBo msg = new OrderStatusCheckBo();
    msg.setOrderID(orderId);
    // 可以采用更短的Delay时间, 在consumer里面重新投递消息
    orderStatusProducer
        .output()
        .send(
            MessageBuilder.withPayload(msg)
                .setHeader("x-delay", 3600 * 24 * 1000 + 300 * 1000)
                .build());

    // 3. 向支付中心发送当前订单，用于保存支付中心的订单数据
    MerchantOrdersVo merchantOrdersVo = orderVo.getMerchantOrdersVo();
    merchantOrdersVo.setReturnUrl(payReturnUrl);
    // 为了方便测试购买，所以所有的支付金额都统一改为1分钱
    merchantOrdersVo.setAmount(1);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add("imoocUserId", "imooc");
    headers.add("password", "imooc");

    HttpEntity<MerchantOrdersVo> entity = new HttpEntity<>(merchantOrdersVo, headers);

    ResponseEntity<IMOOCJSONResult> responseEntity =
        restTemplate.postForEntity(paymentUrl, entity, IMOOCJSONResult.class);
    IMOOCJSONResult paymentResult = responseEntity.getBody();
    if (Objects.requireNonNull(paymentResult).getStatus() != 200) {
      logger.error("发送错误：{}", paymentResult.getMsg());
      return IMOOCJSONResult.errorMsg("支付中心订单创建失败，请联系管理员！");
    }

    return IMOOCJSONResult.ok();
  }

  @PostMapping("notifyMerchantOrderPaid")
  public Integer notifyMerchantOrderPaid(String merchantOrderId) {
    orderService.updateOrderStatus(merchantOrderId, OrderStatusEnum.WAIT_DELIVER.type);
    return HttpStatus.OK.value();
  }

  @PostMapping("getPaidOrderInfo")
  public IMOOCJSONResult getPaidOrderInfo(String orderId) {

    OrderStatus orderStatus = orderService.queryOrderStatusInfo(orderId);
    return IMOOCJSONResult.ok(orderStatus);
  }

  @ApiOperation(value = "获取订单 Token", notes = "获取订单 Token", httpMethod = "POST")
  @PostMapping("/getOrderToken")
  public IMOOCJSONResult getOrderToken(HttpSession session) {
    String token = UUID.randomUUID().toString();
    redisOperator.set("ORDER_TOKEN_" + session.getId(), token, 600);
    return IMOOCJSONResult.ok();
  }
}
