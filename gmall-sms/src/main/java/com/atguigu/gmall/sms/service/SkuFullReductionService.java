package com.atguigu.gmall.sms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 商品满减信息
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-04-04 16:12:04
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

