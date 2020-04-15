package com.atguigu.gmall.sms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 商品阶梯价格
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-04-04 16:12:04
 */
public interface SkuLadderService extends IService<SkuLadderEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

