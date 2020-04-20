package com.atguigu.gmall.search.bean;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Document(indexName = "goods",type = "info",shards = 3,replicas = 2)
public class Goods {

    //搜索字段
    @Id
    private Long skuId;
    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private String title;
    @Field(type = FieldType.Keyword,index = false)
    private String defaultImage;
    @Field(type = FieldType.Double)
    private Double price;

    //排序筛选字段
    @Field(type = FieldType.Long)
    private Long sales;//销量
    @Field(type = FieldType.Date)
    private Data createTime;//新品
    @Field(type = FieldType.Boolean)
    private boolean store;//是否有货

    //聚合字段
    @Field(type = FieldType.Long)
    private Long btandId;
    @Field(type = FieldType.Keyword)
    private String brandName;
    @Field(type = FieldType.Keyword)
    private String logo;

    //分类字段
    @Field(type = FieldType.Long)
    private Long categoryId;
    @Field(type = FieldType.Keyword)
    private String categoryName;
    @Field(type = FieldType.Nested)
    private List<SearchAttrValue> searchAttrs;
}
