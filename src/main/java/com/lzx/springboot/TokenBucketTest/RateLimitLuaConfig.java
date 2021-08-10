package com.lzx.springboot.TokenBucketTest;
/**
 * @author lizhengxin<lizhengxin.lzx @ bytedance.com>
 * @date 08/03/2021 3:34 下午
 **/

/**
 * *****************************************************
 * Copyright (C) 2021 bytedance.com. All Rights Reserved
 * This file is part of bytedance EA project.
 * Unauthorized copy of this file, via any medium is strictly prohibited.
 * Proprietary and Confidential.
 * ****************************************************
 **/

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.io.IOException;

/**
 * @author : HolidayLee
 * @description : 加载限流脚本
 */
@Configuration
public class RateLimitLuaConfig {

    public static final String RATE_LIMIT_KEY_PREFIX = "rateLimitBuckets_";
    public static final String RATE_LIMIT_LUA_FILE_DIRECTORY = "rateLimit/";

    @Bean("initLua")
    public DefaultRedisScript getInitLua() throws IOException {
        DefaultRedisScript luaScript = new DefaultRedisScript();
        luaScript.setScriptText(new ResourceScriptSource(new ClassPathResource(
            RATE_LIMIT_LUA_FILE_DIRECTORY + "rateLimitInit.lua")).getScriptAsString());
        luaScript.setResultType(Long.class);
        return luaScript;
    }

    @Bean("executeLua")
    public DefaultRedisScript getLua() throws IOException {
        DefaultRedisScript luaScript = new DefaultRedisScript();
        luaScript.setScriptText(new ResourceScriptSource(new ClassPathResource(
            RATE_LIMIT_LUA_FILE_DIRECTORY + "rateLimitExecute.lua")).getScriptAsString());
        luaScript.setResultType(Long.class);
        return luaScript;
    }

    @Bean("test")
    public DefaultRedisScript getTestLua() throws IOException {
        DefaultRedisScript luaScript = new DefaultRedisScript();
        luaScript.setScriptText(new ResourceScriptSource(new ClassPathResource(
            RATE_LIMIT_LUA_FILE_DIRECTORY + "test.lua")).getScriptAsString());
        luaScript.setResultType(Long.class);
        return luaScript;
    }
}
