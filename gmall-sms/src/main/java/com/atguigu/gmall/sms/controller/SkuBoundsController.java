package com.atguigu.gmall.sms.controller;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import com.atguigu.gmall.sms.vo.SaleVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品spu积分设置
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-04-04 16:12:04
 */
@Api(tags = "商品sku积分设置 管理")
@RestController
@RequestMapping("sms/skubounds")
public class SkuBoundsController {

    @Autowired
    private SkuBoundsService skuBoundsService;

    @PostMapping("sale")
    public ResponseVo<Object> saveSales(@RequestBody SaleVo saleVo){
        this.skuBoundsService.saveSales(saleVo);
        return ResponseVo.ok();
    }
    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> list(PageParamVo paramVo){
        PageResultVo pageResultVo = skuBoundsService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuBoundsEntity> querySpuBoundsById(@PathVariable("id") Long id){
		SkuBoundsEntity spuBounds = skuBoundsService.getById(id);

        return ResponseVo.ok(spuBounds);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SkuBoundsEntity spuBounds){
        skuBoundsService.save(spuBounds);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SkuBoundsEntity spuBounds){
        skuBoundsService.updateById(spuBounds);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
        skuBoundsService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
