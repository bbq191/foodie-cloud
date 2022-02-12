package com.imooc.item.service;

import com.imooc.item.pojo.Items;
import com.imooc.item.pojo.ItemsImg;
import com.imooc.item.pojo.ItemsParam;
import com.imooc.item.pojo.ItemsSpec;
import com.imooc.item.pojo.vo.CommentLevelCountsVo;
import com.imooc.item.pojo.vo.ShopCartVo;
import com.imooc.pojo.PagedGridResult;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** @author afu */
@RequestMapping("item-api")
public interface ItemService {

  /**
   * 根据商品 id 查询商品
   *
   * @param itemId 商品 id
   * @return 商品对象
   */
  @GetMapping("item")
  Items queryItemById(@RequestParam("itemId") String itemId);

  /**
   * 根据商品 id 查询商品图片列表
   *
   * @param itemId 商品 id
   * @return 商品对象
   */
  @GetMapping("itemImages")
  List<ItemsImg> queryItemImgList(@RequestParam("itemId") String itemId);
  /**
   * 根据商品 id 查询商品规格列表
   *
   * @param itemId 商品 id
   * @return 商品对象
   */
  @GetMapping("itemSpecs")
  List<ItemsSpec> queryItemSpecList(@RequestParam("itemId") String itemId);
  /**
   * 根据商品 id 查询商品参数
   *
   * @param itemId 商品 id
   * @return 商品参数对象
   */
  @GetMapping("itemParam")
  ItemsParam queryItemParam(@RequestParam("itemId") String itemId);

  /**
   * 查询商品评价数量
   *
   * @param itemId 商品 id
   * @return 评价数量 vo
   */
  @GetMapping("countComments")
  CommentLevelCountsVo queryCommentCounts(@RequestParam("itemId") String itemId);

  /**
   * 查询评价详情（分页）
   *
   * @param itemId 商品 id
   * @param level 评价级别
   * @param page 显示页
   * @param pageSize 当前页条数
   * @return 评价详情列表
   */
  @GetMapping("pagedComments")
  PagedGridResult queryPagedComments(
      @RequestParam("itemId") String itemId,
      @RequestParam(value = "level", required = false) Integer level,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "pageSize", required = false) Integer pageSize);

  /**
   * 搜索商品列表
   *
   * @param keywords 搜索关键字
   * @param sort 排序规则
   * @param page 显示页
   * @param pageSize 每页数量
   * @return 商品分页列表
   */
  //  PagedGridResult searchItems(String keywords, String sort, Integer page, Integer pageSize);

  /**
   * 根据分类 id 搜索商品列表
   *
   * @param catId 分类 id
   * @param sort 排序规则
   * @param page 显示页
   * @param pageSize 每页数量
   * @return 商品分页列表
   */
  //  PagedGridResult searchItemsByThirdCat(String catId, String sort, Integer page, Integer
  // pageSize);

  /**
   * 根据规格 ids 查询最新购物车中的商品数据
   *
   * @param specIds 规格 id 拼接串
   * @return 商品列表
   */
  @GetMapping("getCartBySpecIds")
  List<ShopCartVo> queryItemsBySpecIds(@RequestParam("specIds") String specIds);

  /**
   * 根据 specId 获取商品规格信息
   *
   * @param specId 规格 id
   * @return 商品规格
   */
  @GetMapping("itemSpec")
  ItemsSpec queryItemSpecById(@RequestParam("specId") String specId);

  /**
   * 根据商品 id 获取主图片
   *
   * @param itemId 商品 id
   * @return 图片地址
   */
  @GetMapping("primaryImage")
  String queryItemMainImgById(@RequestParam("itemId") String itemId);

  /**
   * 减少库存
   *
   * @param itemSpecId 规格 id
   * @param buyCounts 减少数量
   */
  @PostMapping("decreaseSstock")
  void decreaseItemSpecStock(
      @RequestParam("itemSpecId") String itemSpecId, @RequestParam("buyCounts") int buyCounts);
}
