package com.atguigu.gmall.index.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndexService {
    @Autowired
    private GmallPmsClient pmsClient;


    public List<CategoryEntity> queryLvllCategories() {
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.querCategoriesByPid(0l);
        return listResponseVo.getData();
    }

    public List<CategoryVo> queryLvl2CategoriesWithSub(Long pid) {
        ResponseVo<List<CategoryVo>> listResponseVo = this.pmsClient.queryCategoryVoByPid(pid);
        return listResponseVo.getData();
    }
}
