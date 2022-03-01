package com.imooc.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imooc.enums.CommentLevel;
import com.imooc.enums.YesOrNo;
import com.imooc.item.mapper.ItemsCommentsMapper;
import com.imooc.item.mapper.ItemsImgMapper;
import com.imooc.item.mapper.ItemsMapper;
import com.imooc.item.mapper.ItemsMapperCustom;
import com.imooc.item.mapper.ItemsParamMapper;
import com.imooc.item.mapper.ItemsSpecMapper;
import com.imooc.item.pojo.Items;
import com.imooc.item.pojo.ItemsComments;
import com.imooc.item.pojo.ItemsImg;
import com.imooc.item.pojo.ItemsParam;
import com.imooc.item.pojo.ItemsSpec;
import com.imooc.item.pojo.vo.CommentLevelCountsVo;
import com.imooc.item.pojo.vo.ItemCommentVo;
import com.imooc.item.pojo.vo.ShopCartVo;
import com.imooc.item.service.ItemService;
import com.imooc.pojo.PagedGridResult;
import com.imooc.utils.DesensitizationUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

/**
 * @author afu
 */
@RestController
@Slf4j
public class ItemServiceImpl implements ItemService {

  @Autowired private ItemsMapper itemsMapper;
  @Autowired private ItemsImgMapper itemsImgMapper;
  @Autowired private ItemsSpecMapper itemsSpecMapper;
  @Autowired private ItemsParamMapper itemsParamMapper;
  @Autowired private ItemsCommentsMapper itemsCommentsMapper;
  @Autowired private ItemsMapperCustom itemsMapperCustom;

  @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
  @Override
  public Items queryItemById(String itemId) {
    return itemsMapper.selectByPrimaryKey(itemId);
  }

  @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
  @Override
  public List<ItemsImg> queryItemImgList(String itemId) {
    Example example = new Example(ItemsImg.class);
    Criteria criteria = example.createCriteria();
    criteria.andEqualTo("itemId", itemId);
    return itemsImgMapper.selectByExample(example);
  }

  @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
  @Override
  public List<ItemsSpec> queryItemSpecList(String itemId) {
    Example example = new Example(ItemsSpec.class);
    Criteria criteria = example.createCriteria();
    criteria.andEqualTo("itemId", itemId);
    return itemsSpecMapper.selectByExample(example);
  }

  @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
  @Override
  public ItemsParam queryItemParam(String itemId) {
    Example example = new Example(ItemsParam.class);
    Criteria criteria = example.createCriteria();
    criteria.andEqualTo("itemId", itemId);
    return itemsParamMapper.selectOneByExample(example);
  }

  @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
  @Override
  public CommentLevelCountsVo queryCommentCounts(String itemId) {
    Integer goodCounts = getCommentsCounts(itemId, CommentLevel.GOOD.type);
    Integer normalCounts = getCommentsCounts(itemId, CommentLevel.NORMAL.type);
    Integer badCounts = getCommentsCounts(itemId, CommentLevel.BAD.type);
    Integer totalCounts = goodCounts + badCounts + normalCounts;

    CommentLevelCountsVo commentLevelCountVo = new CommentLevelCountsVo();
    commentLevelCountVo.setGoodCounts(goodCounts);
    commentLevelCountVo.setNormalCounts(normalCounts);
    commentLevelCountVo.setBadCounts(badCounts);
    commentLevelCountVo.setTotalCounts(totalCounts);
    return commentLevelCountVo;
  }

  @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
  @Override
  public PagedGridResult queryPagedComments(
      String itemId, Integer level, Integer page, Integer pageSize) {
    Map<String, Object> map = new HashMap<>(20);
    map.put("itemId", itemId);
    map.put("level", level);
    /*page：第几页 pageSize：每页条数*/
    PageHelper.startPage(page, pageSize);
    List<ItemCommentVo> list = itemsMapperCustom.queryItemComments(map);
    /* 数据脱敏 */
    for (ItemCommentVo vo : list) {
      vo.setNickname(DesensitizationUtil.commonDisplay(vo.getNickname()));
    }
    return setterPageGrid(list, page);
  }

  @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
  @Override
  public List<ShopCartVo> queryItemsBySpecIds(String specIds) {
    String[] ids = specIds.split(",");
    List<String> specIdsList = new ArrayList<>();
    Collections.addAll(specIdsList, ids);
    return itemsMapperCustom.queryItemsBySpecIds(specIdsList);
  }

  @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
  @Override
  public ItemsSpec queryItemSpecById(String specId) {
    return itemsSpecMapper.selectByPrimaryKey(specId);
  }

  @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
  @Override
  public String queryItemMainImgById(String itemId) {
    ItemsImg itemsImg = new ItemsImg();
    itemsImg.setItemId(itemId);
    itemsImg.setIsMain(YesOrNo.YES.type);
    ItemsImg result = itemsImgMapper.selectOne(itemsImg);
    return result != null ? result.getUrl() : "";
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  @Override
  public void decreaseItemSpecStock(String itemSpecId, int buyCounts) {
    // synchronized 不推荐使用，集群下无用，性能低下
    // 锁数据库: 不推荐，导致数据库性能低下
    // 分布式锁 zookeeper redis

    // lockUtil.getLock(); -- 加锁

    // 1. 查询库存
    //        int stock = 10;

    // 2. 判断库存，是否能够减少到0以下
    //        if (stock - buyCounts < 0) {
    // 提示用户库存不够
    //            10 - 3 -3 - 5 = -1
    //        }

    // lockUtil.unLock(); -- 解锁

    int result = itemsMapperCustom.decreaseItemSpecStock(itemSpecId, buyCounts);
    if (result != 1) {
      throw new RuntimeException("订单创建失败，原因：库存不足!");
    }
  }

  /**
   * 分类获取评价数量
   *
   * @param itemId 商品 id
   * @param commentLevel 评价级别
   * @return 评价数量
   */
  private Integer getCommentsCounts(String itemId, Integer commentLevel) {
    ItemsComments condition = new ItemsComments();
    condition.setItemId(itemId);
    if (commentLevel != null) {
      condition.setCommentLevel(commentLevel);
    }
    return itemsCommentsMapper.selectCount(condition);
  }

  /**
   * 分页列表查询
   *
   * @param list 对象列表
   * @param page 第几页
   * @return 分页后对象列表
   */
  private PagedGridResult setterPageGrid(List<?> list, Integer page) {
    PageInfo<?> pageList = new PageInfo<>(list);
    PagedGridResult grid = new PagedGridResult();
    grid.setPage(page);
    grid.setRows(list);
    grid.setTotal(pageList.getPages());
    grid.setRecords(pageList.getTotal());
    return grid;
  }
}
