package com.lzx.springboot.TokenBucketTest;
/**
 * @author lizhengxin<lizhengxin.lzx @ bytedance.com>
 * @date 08/03/2021 4:32 下午
 **/

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import sun.tools.jconsole.Plotter.Unit;

/**
 * *****************************************************
 * Copyright (C) 2021 bytedance.com. All Rights Reserved
 * This file is part of bytedance EA project.
 * Unauthorized copy of this file, via any medium is strictly prohibited.
 * Proprietary and Confidential.
 * ****************************************************
 **/
// 限流
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TokenLimit {
    /**
     * 资源的名称
     * @return
     */
    String name() default "";

    /**
     * 资源的key
     *
     * @return
     */
    String key() default "";
    /**
     * 获取数目
     */
    int count();

    /**
     * 充实次数
     */
    int retryNum() default 10;
    /**
     * 间隔时间，单位ms
     */
    long waitTime() default 100;

    int maxBurst();

    int rate();

    int expireTime() default 60;


}
