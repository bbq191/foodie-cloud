package com.imooc.service.impl;

import com.github.pagehelper.PageHelper;
import com.imooc.item.mapper.ItemsCommentsMapperCustom;
import com.imooc.item.pojo.vo.MyCommentVo;
import com.imooc.pojo.PagedGridResult;
import com.imooc.service.BaseService;
import com.imooc.service.ItemCommentsService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/** @author afu */
@RestController
@Slf4j
public class ItemCommentsServiceImpl extends BaseService implements ItemCommentsService {
  @Autowired private ItemsCommentsMapperCustom itemsCommentsMapperCustom;

  @Override
  public PagedGridResult queryMyComments(String userId, Integer page, Integer pageSize) {
    Map<String, Object> map = new HashMap<>(10);
    map.put("userId", userId);
    PageHelper.startPage(page, pageSize);
    List<MyCommentVo> list = itemsCommentsMapperCustom.queryMyComments(map);
    return setterPageGrid(list, page);
  }

  @Override
  public void saveComments(Map<String, Object> map) {
    itemsCommentsMapperCustom.saveComments(map);
  }
}
