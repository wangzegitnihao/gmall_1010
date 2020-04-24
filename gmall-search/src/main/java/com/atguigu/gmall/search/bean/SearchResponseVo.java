package com.atguigu.gmall.search.bean;

import lombok.Data;

import java.util.List;

@Data
public class SearchResponseVo {
    //品牌
    private SearchResponseAttrVo brand;
    //分类
    private SearchResponseAttrVo category;
    //内存
    private List<SearchResponseAttrVo> filters;
    //分页
    private Integer pageNum;
    private Integer pageSize;
    private Long total;

    //当前页数据
    private List<Goods> data;

}
