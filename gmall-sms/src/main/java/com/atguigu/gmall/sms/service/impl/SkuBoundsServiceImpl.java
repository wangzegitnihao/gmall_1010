package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.mapper.SkuBoundsMapper;
import com.atguigu.gmall.sms.mapper.SkuFullReductionMapper;
import com.atguigu.gmall.sms.mapper.SkuLadderMapper;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import com.atguigu.gmall.sms.vo.SaleVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {
    @Autowired
    private SkuFullReductionMapper skuFullReductionMapper;
    @Autowired
    private SkuLadderMapper skuLadderMapper;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuBoundsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    @Transactional
    public void saveSales(SaleVo saleVo) {
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        skuBoundsEntity.setSkuId(saleVo.getSkuId());
        skuBoundsEntity.setBuyBounds(saleVo.getBuyBounds());
        skuBoundsEntity.setGrowBounds(saleVo.getGrowBounds());
        List<Integer> works = saleVo.getWork();
        skuBoundsEntity.setWork(works.get(3) * 8 + works.get(2) * 4 + works.get(1) * 2 + works.get(0) * 1);
        this.save(skuBoundsEntity);


        SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
        reductionEntity.setSkuId(saleVo.getSkuId());
        reductionEntity.setFullPrice(saleVo.getFullPrice());
        reductionEntity.setReducePrice(saleVo.getReducePrice());
        reductionEntity.setAddOther(saleVo.getFullAddOther());
        this.skuFullReductionMapper.insert(reductionEntity);

        SkuLadderEntity ladderEntity = new SkuLadderEntity();
        ladderEntity.setSkuId(saleVo.getSkuId());
        ladderEntity.setFullCount(saleVo.getFullCount());
        ladderEntity.setDiscount(saleVo.getDiscount());
        ladderEntity.setAddOther(saleVo.getLadderAddOther());
        this.skuLadderMapper.insert(ladderEntity);
    }

}