package com.atguigu.gmall.search.bean;

import lombok.Data;

import java.util.List;

@Data
public class SearchParamVo {

    private String keyword;//检索条件

    private List<Long> brandId;//品牌过滤

    private Long cid;//商品分类

    private List<String> props; //过滤检索参数

    private String sort;//排序字段

    private Double priceFrom;
    private Double priceTo;

    private Integer pageNum = 1;//页码
    private final Integer pageSize = 20;//每页记录数

    private Boolean store;//是否有货

}
