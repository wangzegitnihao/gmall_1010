package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * spu信息
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-04-04 09:54:08
 */
public interface SpuService extends IService<SpuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    PageResultVo querySpusByCidPage(PageParamVo pageParamVo, Long categoryId);

    void bigSave(SpuVo spuVo);
}

