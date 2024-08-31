package com.wang.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.wang.bi.model.entity.Chart;
import com.wang.bi.service.ChartService;
import com.wang.bi.mapper.ChartMapper;
import org.springframework.stereotype.Service;

/**
* @author le'boss
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2024-08-31 21:11:11
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{

}




