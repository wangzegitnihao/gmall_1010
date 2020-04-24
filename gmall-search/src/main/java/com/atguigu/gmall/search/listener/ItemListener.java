package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.bean.Goods;
import com.atguigu.gmall.search.bean.SearchAttrValue;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemListener {

    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;
    @Autowired
    private GoodsRepository goodsRepository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "search-item-queue",durable = "true"),
            exchange = @Exchange(value = "pms-item-exchange",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"item.insert","item.update"}
    ))
    public void listener(Long spuId, Channel channel, Message message) throws IOException {
        ResponseVo<List<SkuEntity>> skuListResponseVo = this.gmallPmsClient.querySkusBySpuId(spuId);
        List<SkuEntity> skuEntities = skuListResponseVo.getData();
        if (!CollectionUtils.isEmpty(skuEntities)) {
            List<Goods> goodsList = skuEntities.stream().map(sku -> {
                Goods goods = new Goods();
                goods.setSkuId(sku.getId());
                goods.setTitle(sku.getTitle());
                goods.setPrice(sku.getPrice().doubleValue());
                goods.setDefaultImage(sku.getDefaultImage());

                ResponseVo<BrandEntity> brandEntityResponseVo = this.gmallPmsClient.queryBrandById(sku.getBrandId());
                BrandEntity brandEntity = brandEntityResponseVo.getData();
                if (brandEntity != null) {
                    goods.setBtandId(sku.getBrandId());
                    goods.setBrandName(brandEntity.getName());
                    goods.setLogo(brandEntity.getLogo());
                }

                ResponseVo<CategoryEntity> categoryEntityResponseVo = this.gmallPmsClient.queryCategoryById(sku.getCategoryId());
                CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                if (categoryEntity != null) {
                    goods.setCategoryId(sku.getCategoryId());
                    goods.setCategoryName(categoryEntity.getName());
                }
                ResponseVo<SpuEntity> spuEntityResponseVo = this.gmallPmsClient.querySpuById(spuId);
                SpuEntity spuEntity = spuEntityResponseVo.getData();
                if (spuEntity != null){
                    goods.setCreateTime(spuEntity.getCreateTime());
                }


                ResponseVo<List<WareSkuEntity>> warSkuListReponseVo = this.gmallWmsClient.queryWareSkusBySkuId(sku.getId());
                List<WareSkuEntity> wareSkuEntities = warSkuListReponseVo.getData();
                if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                    goods.setSales(wareSkuEntities.stream().map(WareSkuEntity::getSales).reduce((a, b) -> a + b).get());
                    goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
                }
                ResponseVo<List<AttrEntity>> attrResponseVo = this.gmallPmsClient.queryAttrsByCid(sku.getCategoryId(), null, 1);
                List<AttrEntity> attrEntities = attrResponseVo.getData();
                if (!CollectionUtils.isEmpty(attrEntities)) {
                    List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());

                    List<SearchAttrValue> searchAttrValues = new ArrayList<>();
                    ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResponseVo = this.gmallPmsClient.querySkuSearchAttrValue(sku.getId(), attrIds);
                    ResponseVo<List<SpuAttrValueEntity>> spuAttrValueResponseVo = this.gmallPmsClient.querySpuSearchAttrValue(spuEntity.getId(), attrIds);
                    List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueResponseVo.getData();
                    List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrValueResponseVo.getData();
                    if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                        searchAttrValues.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                            SearchAttrValue searchAttrValue = new SearchAttrValue();
                            BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValue);
                            return searchAttrValue;
                        }).collect(Collectors.toList()));
                    }
                    if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {
                        searchAttrValues.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                            SearchAttrValue searchAttrValue = new SearchAttrValue();
                            BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValue);
                            return searchAttrValue;
                        }).collect(Collectors.toList()));
                    }
                    goods.setSearchAttrs(searchAttrValues);
                }
                return goods;
            }).collect(Collectors.toList());
            this.goodsRepository.saveAll(goodsList);
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
