package com.lzx.springboot.Controller;
/**
 * @author lizhengxin<lizhengxin.lzx @ bytedance.com>
 * @date 08/02/2021 8:43 下午
 **/

import com.lzx.springboot.Common.Limit;
import com.lzx.springboot.TokenBucketTest.RateLimitUtils;
import com.lzx.springboot.TokenBucketTest.TokenLimit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * *****************************************************
 * Copyright (C) 2021 bytedance.com. All Rights Reserved
 * This file is part of bytedance EA project.
 * Unauthorized copy of this file, via any medium is strictly prohibited.
 * Proprietary and Confidential.
 * ****************************************************
 **/

@RestController
public class myController {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger();

    @Limit(key = "test", period = 100, count = 10)
    // 意味著 100S 内最多允許訪問10次
    @GetMapping("/test")
    public int testLimiter() {
        return ATOMIC_INTEGER.incrementAndGet();
    }

    @GetMapping("/token")
    @TokenLimit(count = 40, maxBurst = 50, rate = 50)
    public int testToken() throws InterruptedException {
        return ATOMIC_INTEGER.incrementAndGet();
    }

}
