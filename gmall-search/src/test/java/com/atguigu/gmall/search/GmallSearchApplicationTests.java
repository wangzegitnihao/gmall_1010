package com.atguigu.gmall.search;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.bean.Goods;
import com.atguigu.gmall.search.bean.SearchAttrValue;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;
    @Autowired
    private GmallWmsClient gmallWmsClient;
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GoodsRepository goodsRepository;
    @Test
    void contextLoads() {
        this.restTemplate.createIndex(Goods.class);
        this.restTemplate.putMapping(Goods.class);
    }
    @Test
    void importData(){
        Integer pageNum = 1;
        Integer pageSize = 100;

        do {
            PageParamVo pageParamVo = new PageParamVo();
            pageParamVo.setPageNum(pageNum);
            pageParamVo.setPageSize(pageSize);
            ResponseVo<List<SpuEntity>> spuListResponseVo = this.gmallPmsClient.querySpuByPage(pageParamVo);
            List<SpuEntity> spuEntitiess = spuListResponseVo.getData();

            if (CollectionUtils.isEmpty(spuEntitiess)){
                continue;
            }
            spuEntitiess.forEach(spuEntity -> {
                ResponseVo<List<SkuEntity>> skuListResponseVo = this.gmallPmsClient.querySkusBySpuId(spuEntity.getId());
                List<SkuEntity> skuEntities = skuListResponseVo.getData();
                if (!CollectionUtils.isEmpty(skuEntities)){
                    List<Goods> goodsList = skuEntities.stream().map(sku -> {
                        Goods goods = new Goods();
                        goods.setSkuId(sku.getId());
                        goods.setTitle(sku.getTitle());
                        goods.setPrice(sku.getPrice().doubleValue());
                        goods.setDefaultImage(sku.getDefaultImage());

                        ResponseVo<BrandEntity> brandEntityResponseVo = this.gmallPmsClient.queryBrandById(sku.getBrandId());
                        BrandEntity brandEntity = brandEntityResponseVo.getData();
                        if (brandEntity != null){
                            goods.setBtandId(sku.getBrandId());
                            goods.setBrandName(brandEntity.getName());
                            goods.setLogo(brandEntity.getLogo());
                        }

                        ResponseVo<CategoryEntity> categoryEntityResponseVo = this.gmallPmsClient.queryCategoryById(sku.getCategoryId());
                        CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                        if (categoryEntity != null){
                            goods.setCategoryId(sku.getCategoryId());
                            goods.setCategoryName(categoryEntity.getName());
                        }

                        goods.setCreateTime(spuEntity.getCreateTime());

                        ResponseVo<List<WareSkuEntity>> warSkuListReponseVo = this.gmallWmsClient.queryWareSkusBySkuId(sku.getId());
                        List<WareSkuEntity> wareSkuEntities = warSkuListReponseVo.getData();
                        if (!CollectionUtils.isEmpty(wareSkuEntities)){
                            goods.setSales(wareSkuEntities.stream().map(WareSkuEntity::getSales).reduce((a,b) -> a+b).get());
                            goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock()>0));
                        }
                        ResponseVo<List<AttrEntity>> attrResponseVo = this.gmallPmsClient.queryAttrsByCid(sku.getCategoryId(), null, 1);
                        List<AttrEntity> attrEntities = attrResponseVo.getData();
                        if (!CollectionUtils.isEmpty(attrEntities)){
                            List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());

                            List<SearchAttrValue> searchAttrValues = new ArrayList<>();
                            ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResponseVo = this.gmallPmsClient.querySkuSearchAttrValue(sku.getId(), attrIds);
                            ResponseVo<List<SpuAttrValueEntity>> spuAttrValueResponseVo = this.gmallPmsClient.querySpuSearchAttrValue(spuEntity.getId(), attrIds);
                            List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueResponseVo.getData();
                            List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrValueResponseVo.getData();
                            if (!CollectionUtils.isEmpty(skuAttrValueEntities)){
                                searchAttrValues.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                                    SearchAttrValue searchAttrValue = new SearchAttrValue();
                                    BeanUtils.copyProperties(skuAttrValueEntity,searchAttrValue);
                                    return searchAttrValue;
                                }).collect(Collectors.toList()));
                            }
                            if (!CollectionUtils.isEmpty(spuAttrValueEntities)){
                                searchAttrValues.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                                    SearchAttrValue searchAttrValue = new SearchAttrValue();
                                    BeanUtils.copyProperties(spuAttrValueEntity,searchAttrValue);
                                    return searchAttrValue;
                                }).collect(Collectors.toList()));
                            }
                            goods.setSearchAttrs(searchAttrValues);
                        }
                        return goods;
                    }).collect(Collectors.toList());
                    this.goodsRepository.saveAll(goodsList);
                }

            });
            pageNum++;
            pageSize = spuEntitiess.size();
        }while (pageSize == 100);
        //分批查询spu

    }

}
