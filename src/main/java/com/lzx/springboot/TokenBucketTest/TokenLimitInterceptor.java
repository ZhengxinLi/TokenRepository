package com.lzx.springboot.TokenBucketTest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;


/**
 * @author lizhengxin<lizhengxin.lzx @ bytedance.com>
 * @date 08/03/2021 4:35 下午
 **/

@Aspect
@Configuration
@Slf4j
public class TokenLimitInterceptor {

    @Resource
    private RateLimitUtils rateLimitUtils;

    @Around("execution(public * *(..)) && @annotation(com.lzx.springboot.TokenBucketTest.TokenLimit)")
    public Object interceptor(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        TokenLimit tokenLimit = method.getAnnotation(TokenLimit.class);
        String name = tokenLimit.name();
        String key = tokenLimit.key();
        if(StringUtils.isEmpty(key)){
            key = method.getName();
        }
        int retryNum = tokenLimit.retryNum();
        long waitTime = tokenLimit.waitTime();
        int requestCount = tokenLimit.count();
        List<String> params = new ArrayList<>();
        params.add(String.valueOf(requestCount));
        params.add(String.valueOf(tokenLimit.maxBurst()));
        params.add(String.valueOf(tokenLimit.rate()));
        params.add(String.valueOf(tokenLimit.expireTime()));
        try {
            log.info("Access try requestCount is {} for name={} and key = {}", requestCount, name, key);
            if (rateLimitUtils.canReleaseRequest(key, params)) {
                log.info("request was dealt success");
                return pjp.proceed();
            } else {
                while (retryNum >= 0){
                    log.error("You have been dragged into the blacklist, entry num{}", retryNum);
                    if (rateLimitUtils.canReleaseRequest(key, params)){
                        log.info("request was dealt success");
                        return pjp.proceed();
                    }
                    retryNum -= 1;
                    Thread.sleep(waitTime);
                }
                log.error("request was refused");
                return 0;
            }
        } catch (Throwable e) {
            log.error("lua script execute error");
            if (e instanceof RuntimeException) {
                throw new RuntimeException(e.getLocalizedMessage());
            }
            throw new RuntimeException("server exception");
        }
    }
}