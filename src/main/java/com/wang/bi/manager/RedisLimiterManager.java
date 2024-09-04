package com.wang.bi.manager;


import com.wang.bi.common.ErrorCode;
import com.wang.bi.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RedisLimiterManager {
    @Resource
    private RedissonClient redissonClient;

    public void doRateLimit(String key) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        //限流器统计规则（每秒2个请求，连续地请求，最多只能有一个请求被允许通过）
        //RateType.OVERALL：全局限流，表示速率限制作用于整个令牌桶，即限制所有请求的速率
        rateLimiter.trySetRate(RateType.OVERALL,2,1, RateIntervalUnit.SECONDS);
        //每当一个操作来之后，请求一个令牌
        boolean canOperate = rateLimiter.tryAcquire(1);
        if (!canOperate) {
            throw new BusinessException(ErrorCode.TO_MANY_REQUEST);
        }
    }
}
