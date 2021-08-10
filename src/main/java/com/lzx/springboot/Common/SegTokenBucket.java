package com.lzx.springboot.Common;
/**
 * @author lizhengxin<lizhengxin.lzx @ bytedance.com>
 * @date 08/03/2021 11:01 上午
 **/

/**
 * *****************************************************
 * Copyright (C) 2021 bytedance.com. All Rights Reserved
 * This file is part of bytedance EA project.
 * Unauthorized copy of this file, via any medium is strictly prohibited.
 * Proprietary and Confidential.
 * ****************************************************
 **/

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;

/**
 * 功能描述:基于分段、分组的限流算法
 */
@Slf4j
public class SegTokenBucket {
    // 每个桶的大小
    private final int ARRAY_SIZE;
    // QPS: 次/s
    private final int QPS;

    private AtomicInteger[] provider;
    private AtomicInteger[] consumer;

    private int cur = 0;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private ScheduledExecutorService scheduledExecutorService = Executors
        .newSingleThreadScheduledExecutor();

    SegTokenBucket(int size, int qps){
        this.ARRAY_SIZE = size;
        this.QPS = qps;
        provider = new AtomicInteger[ARRAY_SIZE];
        consumer = new AtomicInteger[ARRAY_SIZE];
        for (int i = 0; i < ARRAY_SIZE; i++) {
            provider[i] = new AtomicInteger();
            consumer[i] = new AtomicInteger();
        }
        SegTokenBucket.TokenProducer tokenProducer = new SegTokenBucket.TokenProducer(this);

        scheduledExecutorService.scheduleAtFixedRate(tokenProducer, 0, 1,
            TimeUnit.SECONDS);

    }

    void addToken(){
        int everyToken = QPS / ARRAY_SIZE;
        for(AtomicInteger atomicInteger : provider){
            atomicInteger.set(everyToken);
        }
        AtomicInteger[] temp = consumer;
        consumer = provider;
        provider = temp;
//        try {
//            readWriteLock.writeLock().lock();
//
//            System.out.println("进入写锁");
//
//            int everyToken = QPS / ARRAY_SIZE;
//            for(AtomicInteger atomicInteger : consumer){
//                atomicInteger.set(everyToken);
//            }
//
//        } catch (Exception e){
//            log.error("加写锁失败 e ：{}", e);
//        }finally {
//            readWriteLock.writeLock().unlock();
//            System.out.println("出来写锁");
//        }


    }

    public boolean getToken(){
        try {
            readWriteLock.readLock().lock();
            System.out.println("进来读锁");
            cur += 1;
            cur = cur % ARRAY_SIZE;
            return consumer[cur].decrementAndGet() >= 0;
        } catch (Exception e){
            log.error("加读锁失败, e : {}", e);
        }finally {
            readWriteLock.readLock().unlock();
            System.out.println("出来读锁");
        }
        return false;
    }


    public static class TokenProducer implements Runnable{
        private SegTokenBucket segTokenBucket;

        TokenProducer(SegTokenBucket segTokenBucket){
            this.segTokenBucket = segTokenBucket;
        }

        @Override
        public void run() {
            this.segTokenBucket.addToken();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SegTokenBucket segTokenBucket = new SegTokenBucket(10, 50);
        int num = 0;
        long startTime = System.currentTimeMillis();
        for(int i = 0; i < 1000; i ++) {
            boolean flag = segTokenBucket.getToken();
            System.out.println(flag);
            if(flag){
                num += 1;
            }
            Thread.sleep(20);
        }
        System.out.println((float)num / 1000);
        segTokenBucket.scheduledExecutorService.shutdown();
        while (!segTokenBucket.scheduledExecutorService.isTerminated()){
            System.out.println("waiting");
        }
        System.out.println(System.currentTimeMillis() - startTime);

    }

}
