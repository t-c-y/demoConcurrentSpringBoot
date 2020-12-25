package com.example.demo.jvm.day24;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class InheritableThreadLocalTest {

    /**
     *
     *
     *
     */

    static AtomicInteger threadIndex = new AtomicInteger(1);
    static ThreadPoolExecutor executor = new ThreadPoolExecutor(3,
            3,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            r->{
                Thread t = new Thread(r);
                t.setName("disposeRequestThread-" + threadIndex.getAndIncrement());
                return t;
            });

    static ThreadLocal<String> traceIdKD = new ThreadLocal<>();
    static InheritableThreadLocal<String> traceIdKDIn = new InheritableThreadLocal<>();

    public static void log(String msg) {
        StackTraceElement stack[] = (new Throwable()).getStackTrace();
        System.out.println("****" + System.currentTimeMillis()
                + ",[tId:" + traceIdKD.get() + "]"
                + ",[tIdIn:" + traceIdKDIn.get() + "]"
                + ",[线程:" + Thread.currentThread().getName() + "],"
                + stack[1] + ":" + msg);
    }

    public static void controller(List<String> dataList) {
        log("接受请求");
        service(dataList);
    }

    public static void service(List<String> dataList) {
        log("执行业务");
        dao(dataList);
    }

    public static void dao(List<String> dataList) {
        CountDownLatch countDownLatch = new CountDownLatch(dataList.size());
        log("执行数据库操作");
        String threadName = Thread.currentThread().getName();
        for(String s : dataList) {
            new Thread(()->{
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                    log("插入 "+s+" 成功，主线程：" + threadName);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void test() {
        List<String> dataList = new ArrayList<>();
        for(int i=0; i<3; i++) {
            dataList.add("数据："+i);
        }

        for (int i = 0; i < 5; i++) {
            String tId = "TID_"+String.valueOf(i);
            executor.execute(()->{
                traceIdKD.set(tId);
                traceIdKDIn.set(tId);
                try {
                    controller(dataList);
                } finally {
                    traceIdKD.remove();
                }
            });
        }

        executor.shutdown();
    }

    public static void main(String[] args) {
        // 并发插入线程，子线程不携带父线程信息（ThreadLocal）
        // 子线程携带父线程信息(InheritableThreadLocal)
        test();
    }


}
