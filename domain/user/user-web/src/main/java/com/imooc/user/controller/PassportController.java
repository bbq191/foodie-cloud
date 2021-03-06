package com.imooc.user.controller;

import com.imooc.auth.service.AuthService;
import com.imooc.auth.service.pojo.Account;
import com.imooc.auth.service.pojo.AuthCode;
import com.imooc.auth.service.pojo.AuthResponse;
import com.imooc.controller.BaseController;
import com.imooc.pojo.IMOOCJSONResult;
import com.imooc.pojo.ShopCartBo;
import com.imooc.user.UserApplicationProperties;
import com.imooc.user.pojo.Users;
import com.imooc.user.pojo.bo.UserBo;
import com.imooc.user.service.UserService;
import com.imooc.user.stream.ForceLogoutTopic;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.MD5Utils;
import com.imooc.utils.RedisOperator;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author afu
 */
@Api(
    value = "注册登录",
    tags = {"用于注册登录的相关接口"})
@RestController
@RequestMapping("passport")
@Slf4j
public class PassportController extends BaseController {
  @Qualifier("userServiceImpl")
  @Autowired
  private UserService userService;

  @Autowired private AuthService authService;
  @Autowired private RedisOperator redisOperator;
  @Autowired private UserApplicationProperties userApplicationProperties;
  @Autowired private ForceLogoutTopic producer;
  private final int PASSWORD_MIN_LENGTH = 6;
  private static final String AUTH_HEADER = "Authorization";
  private static final String REFRESH_TOKEN_HEADER = "refresh-token";
  private static final String UID_HEADER = "imooc-user-id";

  @ApiOperation(value = "用户名是否存在", notes = "用户名是否存在", httpMethod = "GET")
  @GetMapping("/usernameIsExist")
  public IMOOCJSONResult usernameIsExist(@RequestParam String username) {
    // 判断传入参数是否为空
    if (StringUtils.isBlank(username)) {
      return IMOOCJSONResult.errorMsg("用户名不能为空");
    }
    // 查找用户是否存在
    boolean isExist = userService.queryUsernameIsExist(username);
    if (isExist) {
      return IMOOCJSONResult.errorMsg("用户名已存在");
    }
    // 不重复，返回成功
    return IMOOCJSONResult.ok();
  }

  @ApiOperation(value = "用户注册", notes = "用户注册", httpMethod = "POST")
  @PostMapping("/regist")
  public IMOOCJSONResult regist(
      @RequestBody UserBo userBo, HttpServletRequest request, HttpServletResponse response) {
    if (userApplicationProperties.isDisabledRegistration()) {
      log.info("user registration is blocked - {}", userBo.getUsername());
      return IMOOCJSONResult.errorMsg("当前用户注册过多，请等待再次开放");
    }
    String username = userBo.getUsername();
    String password = userBo.getPassword();
    String confirmPwd = userBo.getConfirmPassword();
    if (StringUtils.isBlank(username)
        || StringUtils.isBlank(password)
        || StringUtils.isBlank(confirmPwd)) {
      return IMOOCJSONResult.errorMsg("用户名密码不能为空");
    }
    // 查找用户是否存在
    boolean isExist = userService.queryUsernameIsExist(username);
    if (isExist) {
      return IMOOCJSONResult.errorMsg("用户名已存在");
    }
    if (password.length() < PASSWORD_MIN_LENGTH) {
      return IMOOCJSONResult.errorMsg("密码不能小于 6 位");
    }
    if (!password.equals(confirmPwd)) {
      return IMOOCJSONResult.errorMsg("两次密码必须一致");
    }
    Users userResult = userService.createUser(userBo);
    // 生成用户 token，存入 redis
    //    UsersVo usersVo = conventUsersVo(userResult);
    // 设置 cookie 值
    //    CookieUtils.setCookie(request, response, "user", JsonUtils.objectToJson(usersVo), true);
    // 不重复，返回成功
    return IMOOCJSONResult.ok();
  }

  @ApiOperation(value = "用户登陆", notes = "用户登陆", httpMethod = "POST")
  @PostMapping("/login")
  @HystrixCommand(
      commandKey = "loginFail", // 全局唯一的标识服务，默认函数名称
      groupKey = "password", // 全局服务分组，用于组织仪表盘，统计信息。默认：类名
      fallbackMethod = "loginFail", // 同一个类里，public private都可以
      // 在列表中的exception，不会触发降级
      //      ignoreExceptions = {IllegalArgumentException.class},
      // 线程有关的属性
      // 线程组, 多个服务可以共用一个线程组
      threadPoolKey = "threadPoolA",
      threadPoolProperties = {
        // 核心线程数
        @HystrixProperty(name = "coreSize", value = "10"),
        // size > 0, LinkedBlockingQueue -> 请求等待队列
        // 默认-1 , SynchronousQueue -> 不存储元素的阻塞队列（建议读源码，学CAS应用）
        @HystrixProperty(name = "maxQueueSize", value = "40"),
        // 在maxQueueSize=-1的时候无效，队列没有达到maxQueueSize依然拒绝
        @HystrixProperty(name = "queueSizeRejectionThreshold", value = "15"),
        // （线程池）统计窗口持续时间
        @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "2024"),
        // （线程池）窗口内桶子的数量
        @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "18"),
      },
      commandProperties = {
        // TODO: 熔断降级相关属性，也可以放在这里
      })
  public IMOOCJSONResult login(
      @RequestBody UserBo userBo, HttpServletRequest request, HttpServletResponse response) {
    String username = userBo.getUsername();
    String password = userBo.getPassword();
    if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
      return IMOOCJSONResult.errorMsg("用户名密码不能为空");
    }
    Users userResult = null;
    try {
      userResult = userService.queryUserForLogin(username, MD5Utils.getMD5Str(password));
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (userResult == null) {
      return IMOOCJSONResult.errorMsg("用户名密码不正确");
    }
    AuthResponse token = authService.tokenize(userResult.getId());
    if (!AuthCode.SUCCESS.getCode().equals(token.getCode())) {
      log.error("Token erroe - uid = {}", userResult.getId());
      return IMOOCJSONResult.errorMsg("Token erroe");
    }
    // 将token添加到Header当中
    addAuth2Header(response, token.getAccount());

    // 生成用户 token，存入 redis
    //    UsersVo usersVo = conventUsersVo(userResult);
    // 设置 cookie 值
    //    CookieUtils.setCookie(request, response, "user", JsonUtils.objectToJson(usersVo), true);
    // 同步购物车数据
    synchShopcartData(userResult.getId(), request, response);
    return IMOOCJSONResult.ok(userResult);
  }

  private IMOOCJSONResult loginFail(
      UserBo userBo, HttpServletRequest request, HttpServletResponse response, Throwable throwable)
      throws Exception {
    return IMOOCJSONResult.errorMsg("验证码输错了（模仿12306）" + throwable.getLocalizedMessage());
  }

  @ApiOperation(value = "用户退出", notes = "用户退出并清理 cookie", httpMethod = "POST")
  @PostMapping("/logout")
  public IMOOCJSONResult logout(
      @RequestParam String userId, HttpServletRequest request, HttpServletResponse response) {
    Account account =
        Account.builder()
            .token(request.getHeader(AUTH_HEADER))
            .refreshToken(request.getHeader(REFRESH_TOKEN_HEADER))
            .userId(userId)
            .build();
    AuthResponse authResponse = authService.delete(account);
    if (!AuthCode.SUCCESS.getCode().equals(authResponse.getCode())) {
      log.error("Token erroe - uid = {}", userId);
      return IMOOCJSONResult.errorMsg("Token erroe");
    }

    // 清楚用户 cookie
    CookieUtils.deleteCookie(request, response, "user");
    // 分布式会话中需要清除用户数据
    redisOperator.del(REDIS_USER_TOKEN + ":" + userId);
    CookieUtils.deleteCookie(request, response, FOODIE_SHOPCART);
    return IMOOCJSONResult.ok();
  }

  // FIXME 将这个接口从网关层移除，不对外暴露
  // 简陋版api - 长得丑但是跑得快
  @ApiOperation(value = "用户强制退出登录", notes = "用户退出登录", httpMethod = "POST")
  @PostMapping("/forceLogout")
  public IMOOCJSONResult forceLogout(@RequestParam String userIds) {
    if (StringUtils.isNotBlank(userIds)) {
      for (String uid : userIds.split(",")) {
        log.info("send logout message, uid={}", uid);
        producer.output().send(MessageBuilder.withPayload(uid).build());
      }
    }
    return IMOOCJSONResult.ok();
  }

  /**
   * 注册登录成功后，同步cookie和redis中的购物车数据
   *
   * @param userId 用户id
   * @param request 请求
   * @param response 响应
   */
  private void synchShopcartData(
      String userId, HttpServletRequest request, HttpServletResponse response) {
    /*
     * 1. redis中无数据，如果cookie中的购物车为空，那么这个时候不做任何处理
     *                 如果cookie中的购物车不为空，此时直接放入redis中
     * 2. redis中有数据，如果cookie中的购物车为空，那么直接把redis的购物车覆盖本地cookie
     *                 如果cookie中的购物车不为空，如果cookie中的某个商品在redis中存在，
     *                 则以cookie为主，删除redis中的，把cookie中的商品直接覆盖redis中（参考京东）
     * 3. 同步到redis中去了以后，覆盖本地cookie购物车的数据，保证本地购物车的数据是同步最新的
     */
    // 从redis中获取购物车
    String shopcartJsonRedis = redisOperator.get(FOODIE_SHOPCART + ":" + userId);
    // 从cookie中获取购物车
    String shopcartStrCookie = CookieUtils.getCookieValue(request, FOODIE_SHOPCART, true);
    if (StringUtils.isBlank(shopcartJsonRedis)) {
      // redis为空，cookie不为空，直接把cookie中的数据放入redis
      if (StringUtils.isNotBlank(shopcartStrCookie)) {
        redisOperator.set(FOODIE_SHOPCART + ":" + userId, shopcartStrCookie);
      }
    } else {
      // redis不为空，cookie不为空，合并cookie和redis中购物车的商品数据（同一商品则覆盖redis）
      if (StringUtils.isNotBlank(shopcartStrCookie)) {
        /*
         * 1. 已经存在的，把cookie中对应的数量，覆盖redis（参考京东）
         * 2. 该项商品标记为待删除，统一放入一个待删除的list
         * 3. 从cookie中清理所有的待删除list
         * 4. 合并redis和cookie中的数据
         * 5. 更新到redis和cookie中
         */
        List<ShopCartBo> shopcartListRedis =
            JsonUtils.jsonToList(shopcartJsonRedis, ShopCartBo.class);
        List<ShopCartBo> shopcartListCookie =
            JsonUtils.jsonToList(shopcartStrCookie, ShopCartBo.class);
        // 定义一个待删除list
        List<ShopCartBo> pendingDeleteList = new ArrayList<>();
        assert shopcartListRedis != null;
        for (ShopCartBo redisShopcart : shopcartListRedis) {
          String redisSpecId = redisShopcart.getSpecId();
          assert shopcartListCookie != null;
          for (ShopCartBo cookieShopcart : shopcartListCookie) {
            String cookieSpecId = cookieShopcart.getSpecId();
            if (redisSpecId.equals(cookieSpecId)) {
              // 覆盖购买数量，不累加，参考京东
              redisShopcart.setBuyCounts(cookieShopcart.getBuyCounts());
              // 把cookieShopcart放入待删除列表，用于最后的删除与合并
              pendingDeleteList.add(cookieShopcart);
            }
          }
        }
        // 从现有cookie中删除对应的覆盖过的商品数据
        assert shopcartListCookie != null;
        shopcartListCookie.removeAll(pendingDeleteList);
        // 合并两个list
        shopcartListRedis.addAll(shopcartListCookie);
        // 更新到redis和cookie
        CookieUtils.setCookie(
            request, response, FOODIE_SHOPCART, JsonUtils.objectToJson(shopcartListRedis), true);
        redisOperator.set(
            FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(shopcartListRedis));
      } else {
        // redis不为空，cookie为空，直接把redis覆盖cookie
        CookieUtils.setCookie(request, response, FOODIE_SHOPCART, shopcartJsonRedis, true);
      }
    }
  }

  // TODO 修改前端js代码
  // 在前端页面里拿到Authorization, refresh-token和imooc-user-id。
  // 前端每次请求服务，都把这几个参数带上
  private void addAuth2Header(HttpServletResponse response, Account token) {
    response.setHeader(AUTH_HEADER, token.getToken());
    response.setHeader(REFRESH_TOKEN_HEADER, token.getRefreshToken());
    response.setHeader(UID_HEADER, token.getUserId());

    // 让前端感知到，过期时间一天，这样可以在临近过期的时候refresh token
    Calendar expTime = Calendar.getInstance();
    expTime.add(Calendar.DAY_OF_MONTH, 1);
    response.setHeader("token-exp-time", expTime.getTimeInMillis() + "");
  }
}
