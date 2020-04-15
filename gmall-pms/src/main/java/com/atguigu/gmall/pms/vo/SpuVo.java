package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;
@Data
public class SpuVo extends SpuEntity {
    private List<String> spuImages;//描述信息
    private List<SpuAttrValueVo> baseAttrs;
    private List<SkuVo> skus;
}
