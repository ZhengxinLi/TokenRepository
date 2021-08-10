package com.lzx.springboot;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringbootApplicationTests {

    public static void main(String[] args) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(10),
            new RejectedExecutionHandler() {
                @SneakyThrows
                @Override
                public void rejectedExecution(Runnable r,
                                              ThreadPoolExecutor executor) {

                    System.out.println("reject");
                }
            });
        for(int i =0;i<20;i++){
            try {
                task task  = new task("ads");
                threadPoolExecutor.submit(task);
//                futureTask.get();
            }catch (Exception e){
                System.out.println(e);
            }

        }
        threadPoolExecutor.shutdown();
    }


    public static class task implements Callable<Integer>{
        private int version = 0;
        private final String pj;
        private int ENTRY_MAX = 11;

        public task(String pj){
            this.pj = pj;
        }
        @SneakyThrows
        @Override
        public Integer call() {
            this.version += 1;
            if(version >= ENTRY_MAX){
                // 将重试次数超过抛给前方
                return 2;
            }
            System.out.println(pj);
            return 1;
        }

        public void versionShift(){
            this.version += 1;
        }
        ewqeqweqweqwewqeqweq

    }

}
