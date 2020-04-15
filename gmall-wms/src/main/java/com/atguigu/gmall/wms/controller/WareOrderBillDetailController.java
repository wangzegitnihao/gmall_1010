package com.atguigu.gmall.wms.controller;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.wms.entity.WareOrderBillDetailEntity;
import com.atguigu.gmall.wms.service.WareOrderBillDetailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库存工作单
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-04-11 14:04:28
 */
@Api(tags = "库存工作单 管理")
@RestController
@RequestMapping("wms/wareorderbilldetail")
public class WareOrderBillDetailController {

    @Autowired
    private WareOrderBillDetailService wareOrderBillDetailService;

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> list(PageParamVo paramVo){
        PageResultVo pageResultVo = wareOrderBillDetailService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<WareOrderBillDetailEntity> queryWareOrderBillDetailById(@PathVariable("id") Long id){
		WareOrderBillDetailEntity wareOrderBillDetail = wareOrderBillDetailService.getById(id);

        return ResponseVo.ok(wareOrderBillDetail);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody WareOrderBillDetailEntity wareOrderBillDetail){
		wareOrderBillDetailService.save(wareOrderBillDetail);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody WareOrderBillDetailEntity wareOrderBillDetail){
		wareOrderBillDetailService.updateById(wareOrderBillDetail);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		wareOrderBillDetailService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
