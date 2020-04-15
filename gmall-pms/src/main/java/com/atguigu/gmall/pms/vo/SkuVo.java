package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class SkuVo extends SkuEntity {
    //积分相关信息
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;

    //满点信息
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;

    //sku打折信息
    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther;

    //sku图片信息
    private List<String> images;
    private List<SkuAttrValueEntity> saleAttrs;

}
