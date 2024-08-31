package com.wang.bi.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.bi.model.dto.post.PostQueryRequest;
import com.wang.bi.model.entity.Chart;
import com.wang.bi.model.entity.Post;

/**
* @author le'boss
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2024-08-31 21:11:11
*/
public interface ChartService extends IService<Chart> {
}
