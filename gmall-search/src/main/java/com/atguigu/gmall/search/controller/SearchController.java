package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.search.bean.SearchParamVo;
import com.atguigu.gmall.search.bean.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    public ResponseVo<SearchResponseVo> search(SearchParamVo searchParamVo){
        SearchResponseVo responseVo = this.searchService.search(searchParamVo);
        return ResponseVo.ok(responseVo);
    }
}
