package com.imooc.order.pojo.bo.center;

import com.imooc.order.pojo.bo.SubmitOrderBo;
import com.imooc.pojo.ShopCartBo;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** @author afu */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderBo {
  private SubmitOrderBo order;
  private List<ShopCartBo> items;
}
