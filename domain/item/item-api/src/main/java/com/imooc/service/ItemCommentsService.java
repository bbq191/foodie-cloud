package com.imooc.service;

import com.imooc.pojo.PagedGridResult;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** @author afu */
@RequestMapping("item-comments-api")
public interface ItemCommentsService {
  /**
   * 我的评价查询 分页
   *
   * @param userId 用户 id
   * @param page 分页
   * @param pageSize 每页数据
   * @return 分页评价列表
   */
  @GetMapping("myComments")
  PagedGridResult queryMyComments(
      @RequestParam("userId") String userId,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "pageSize", required = false) Integer pageSize);

  /**
   * 保存评论
   *
   * @param map 评论对象
   */
  @PostMapping("saveComments")
  void saveComments(@RequestBody Map<String, Object> map);
}
