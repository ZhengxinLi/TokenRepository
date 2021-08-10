package com.lzx.springboot.TokenBucketTest;
/**
 * @author lizhengxin<lizhengxin.lzx @ bytedance.com>
 * @date 08/03/2021 3:38 下午
 **/

/**
 * *****************************************************
 * Copyright (C) 2021 bytedance.com. All Rights Reserved
 * This file is part of bytedance EA project.
 * Unauthorized copy of this file, via any medium is strictly prohibited.
 * Proprietary and Confidential.
 * ****************************************************
 **/
import java.io.Serializable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author : lizhengxin
 * @description : 支持分布式的令牌桶限流算法
 */
@Component
@ConditionalOnBean(RateLimitLuaConfig.class)
@Slf4j
public class RateLimitUtils {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;
    @Resource(name = "initLua")
    private RedisScript<Long> initLua;
    @Resource(name = "executeLua")
    private RedisScript<Long> executeLua;

    @Resource(name = "test")
    private RedisScript<Long> testLua;

    @Value("${rateLimit.key}")
    private String key;
    @Value("${rateLimit.currentPermits}")
    private String currentPermits;
    @Value("${rateLimit.maxBurst}")
    private String maxBurst;
    @Value("${rateLimit.rate}")
    private String rate;

//    @PostConstruct
    /**
     * 功能描述: 往redis里面初始化一个hash对象
     * TODO
     * @param null:
     * @return
     * @author lizhengxin
     * @date   2021/8/3 5:01 下午
     */
    private void initRateLimitBuckets() {
        List<String> paramList = initBucketParams();
        this.key = paramList.get(paramList.size() - 1);
        Long result = stringRedisTemplate.execute(initLua, getKeyList(key), paramList.toArray());
        if (result != null && result == 1) {
            log.info("初始化令牌桶成功");
        } else {
            throw new RuntimeException("初始化令牌桶失败");
        }
        log.info("sha1值为:" + executeLua.getSha1());
        // 预加载优化:
        // 1.x的stringRedisTemplate在redis cluster的时候不支持evalSha,JedisClusterConnection.scriptLoad()函数直接抛异常InvalidDataAccessApiUsageException("EvalSha is not supported in cluster environment.")
        // 2.x的stringRedisTemplate采用lettuce架构,在redis cluster的时候支持scriptLoad
        //String luaSha1 = Objects.requireNonNull(stringRedisTemplate.getConnectionFactory()).getConnection().scriptLoad(executeLua.getScriptAsString().getBytes());
    }

    private List<String> initBucketParams() {
        // 参数顺序(要与lua中的参数顺序一致):request_count,current_permits, max_burst, rate, key
        List<String> paramList = new ArrayList<>();
        // current_permits
        paramList.add(currentPermits);
        // max_burst
        paramList.add(maxBurst);
        // rate
        paramList.add(rate);
        // key
        paramList.add(key);
        return paramList;
    }

    /**
     * 是否允许放行数量为requestCount的请求
     *
     * @param requestCount 请求数量
     * @param key          redis中存储的key，配置文件中的key
     * @return true:不超过流量;false:超过流量
     */
    public boolean canReleaseRequest(String key, List<String> requestCount) {
        Long result = stringRedisTemplate.execute(testLua, getKeyList(key), requestCount.toArray());

        return result == 1;
    }

    private Long executeLuaScript(RedisScript<Long> script, List<String> keys, String... params) {
        return stringRedisTemplate.execute(script, keys, params);
    }

    /**
     * 执行lua脚本
     *
     * @param requestCount 当前请求的数量
     * @return 执行结果:-1代表请求数量超出当前允许的请求数量;1代表没有超过
     */
//    private Long executeRateLimit(Integer requestCount, String key) {
//        // 执行lua脚本获取当前请求的数量是否超过允许的请求数量
//        Long result = executeLuaScript(testLua, getKeyList(key), requestCount.toString());
//        return result;
//    }

    /**
     * 获取redis中的bucket的key列表
     *
     * @return key列表
     */
    private List<String> getKeyList(String key) {
        return Collections.singletonList(RateLimitLuaConfig.RATE_LIMIT_KEY_PREFIX + key);
    }

    /**
     * 获取redis的当前时间
     *
     * @return redis的当前时间(ms)
     */
//    private Long getCurrentRedisTimeMills() {
//        return stringRedisTemplate.execute((RedisCallback<Long>) RedisServerCommands::time);
//    }
    private Long getCurrentRedisTimeMills() {
        Long currentMills = redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return redisConnection.time();
            }
        });
        return currentMills;
    }

}
