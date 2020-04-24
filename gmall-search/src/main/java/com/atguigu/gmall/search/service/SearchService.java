package com.atguigu.gmall.search.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.search.bean.Goods;
import com.atguigu.gmall.search.bean.SearchParamVo;
import com.atguigu.gmall.search.bean.SearchResponseAttrVo;
import com.atguigu.gmall.search.bean.SearchResponseVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.util.CollectionUtil;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public SearchResponseVo search(SearchParamVo searchParamVo) {
        try {
            //构建查询条件
            SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, this.buildDsl(searchParamVo));
            //执行查询
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            // TODO: 解析结果集
            SearchResponseVo responseVo = this.parseResult(searchResponse);
            responseVo.setPageNum(searchParamVo.getPageNum());
            responseVo.setPageSize(searchParamVo.getPageSize());
            return responseVo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析搜索结果集
     * @param response
     * @return
     */
    private SearchResponseVo parseResult(SearchResponse response) {
        SearchResponseVo responseVo = new SearchResponseVo();

        SearchHits hits = response.getHits();
        //总命中的记录数
        responseVo.setTotal(hits.getTotalHits());
        SearchHit[] hitsHits = hits.getHits();
        List<Goods> goodsList = Stream.of(hitsHits).map(hitsHit -> {
            String goodsJson = hitsHit.getSourceAsString();
            Goods goods = JSON.parseObject(goodsJson, Goods.class);
            Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
            HighlightField highlightField = highlightFields.get("title");
            String highlightTitle = highlightField.getFragments()[0].toString();
            goods.setTitle(highlightTitle);
            return goods;
        }).collect(Collectors.toList());
        responseVo.setData(goodsList);
        //聚合结果集解析
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();
        ParsedStringTerms brandIdAgg = (ParsedStringTerms)aggregationMap.get("brandIdAgg");
        List<? extends Terms.Bucket> buckets = brandIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)){
            List<String> collect = buckets.stream().map(bucket -> {
                Map<Object, Object> map = new HashMap<>();
                Long brandId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
                map.put("id", brandId);
                Map<String, Aggregation> brandAggregationMap = ((Terms.Bucket) bucket).getAggregations().asMap();
                ParsedStringTerms brandNameAgg = (ParsedStringTerms) brandAggregationMap.get("brandNameAgg");
                map.put("name", brandNameAgg.getBuckets().get(0).getKeyAsString());
                ParsedStringTerms logoAgg = (ParsedStringTerms) brandAggregationMap.get("logoAgg");
                List<? extends Terms.Bucket> logoAggBuckets = logoAgg.getBuckets();
                if (!CollectionUtils.isEmpty(logoAggBuckets)) {
                    map.put("logo", logoAggBuckets.get(0).getKeyAsString());
                }
                return JSON.toJSONString(map);
            }).collect(Collectors.toList());
            SearchResponseAttrVo brand = new SearchResponseAttrVo();
            brand.setAttrId(null);
            brand.setAttrName("品牌");
            brand.setAttrValues(collect);
            responseVo.setBrand(brand);
        }
        ParsedLongTerms categotyIdAgg = (ParsedLongTerms)aggregationMap.get("categoryIdAgg");
        List<? extends Terms.Bucket> categotyIdAggBuckets = categotyIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(categotyIdAggBuckets)){
            List<String> attrValues = categotyIdAggBuckets.stream().map(bucket -> {
                Map<String, Object> map = new HashMap<>();
                Long categoryId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
                map.put("id", categoryId);
                ParsedStringTerms categoryNameAgg =(ParsedStringTerms) ((Terms.Bucket) bucket).getAggregations().get("categoryNameAgg");
                map.put("name", categoryNameAgg.getBuckets().get(0).getKeyAsString());
                return JSON.toJSONString(map);
            }).collect(Collectors.toList());
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            searchResponseAttrVo.setAttrId(null);
            searchResponseAttrVo.setAttrName("分类");
            searchResponseAttrVo.setAttrValues(attrValues);
            responseVo.setCategory(searchResponseAttrVo);
        }

        ParsedNested attrAgg = (ParsedNested)aggregationMap.get("attrAgg");
        ParsedLongTerms attrIdAgg = (ParsedLongTerms)attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> attrIdAggBuckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(attrIdAggBuckets)){
             List<SearchResponseAttrVo> filters = attrIdAggBuckets.stream().map(bucket -> {
                 SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                 //规格参数
                 searchResponseAttrVo.setAttrId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
                 ParsedStringTerms attrNameAgg = (ParsedStringTerms)((Terms.Bucket) bucket).getAggregations().get("attrNameAgg");
                 searchResponseAttrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
                 ParsedStringTerms attrValueAgg =(ParsedStringTerms) ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
                 List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                 if (!CollectionUtils.isEmpty(attrValueAggBuckets)){
                     List<String> attrValues = attrValueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                     searchResponseAttrVo.setAttrValues(attrValues);
                 }
                 return searchResponseAttrVo;
             }).collect(Collectors.toList());
            responseVo.setFilters(filters);
        }
        return responseVo;
    }

    /**
     * 构建查询DSL语句
     * @return
     */
    private SearchSourceBuilder buildDsl(SearchParamVo searchParamVo) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        String keyword = searchParamVo.getKeyword();
        if (StringUtils.isEmpty(keyword)){
            // 打广告， TODO
            return null;
        }
        //1. 构建查询条件Bool查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("title",keyword).operator(Operator.AND));

        List<Long> brandId = searchParamVo.getBrandId();
        if (!CollectionUtils.isEmpty(brandId)){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",brandId));
        }
        Long cid = searchParamVo.getCid();
        if (cid != null ){
            boolQueryBuilder.filter(QueryBuilders.termQuery("categoryId",cid));
        }

        Double priceFrom = searchParamVo.getPriceFrom();
        Double priceTo = searchParamVo.getPriceTo();
        if (priceFrom != null || priceTo != null){
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            if (priceFrom != null){
                rangeQuery.gte(priceFrom);
            }
            if (priceTo != null){
                rangeQuery.lte(priceTo);
            }
            boolQueryBuilder.filter(rangeQuery);
        }
        Boolean store = searchParamVo.getStore();
        if (store != null){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("store",store));
        }

        List<String> props = searchParamVo.getProps();
        if (!CollectionUtils.isEmpty(props)){
            props.forEach(prop -> {
                String[] attrs = StringUtils.split(prop,":");
                if (attrs != null && attrs.length == 2){
                    String attrId = attrs[0];
                    String attrValueString = attrs[1];
                    String[] attrValues = StringUtils.split(attrValueString, "-");
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrId",attrId));
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue",attrValues));
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs",boolQuery, ScoreMode.None));
                }
            });
        }
        sourceBuilder.query(boolQueryBuilder);

        //2. 构建排序
        String sort = searchParamVo.getSort();
        if (StringUtils.isEmpty(sort)){
            String[] sorts = StringUtils.split(sort, ":");
            if (sorts != null && sorts.length == 2){
                String field = "";
                switch (sorts[0]){
                    case "1": field = "price";break;
                    case "2": field = "createTime";break;
                    case "3": field = "sales";break;
                }
                sourceBuilder.sort(field,StringUtils.equals(sorts[1],"desc") ? SortOrder.DESC : SortOrder.ASC);
            }
        }


        //3. 构建分页
        Integer pageNum = searchParamVo.getPageNum();
        Integer pageSize = searchParamVo.getPageSize();
        sourceBuilder.from((pageNum -1 ) * pageSize);
        sourceBuilder.size(pageSize);

        //4.构建高亮
        sourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<fomt style='color:red'>").postTags("</font>"));

        //5.构建聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                .subAggregation(AggregationBuilders.terms("logoAgg").field("logo")));
        sourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg","searchAttrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))));
        //6.构建结果集过滤
        sourceBuilder.fetchSource(new String[]{"skuId","title","price","defaultImage"},null);
        System.out.println(sourceBuilder.toString());
        return sourceBuilder;
    }
}
