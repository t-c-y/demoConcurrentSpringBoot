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
     * 父线程相当于主管，子线程相当于干活的小弟，主管让小弟们干活的时候，
     * 将自己兜里面的东西复制一份给小弟们使用，主管兜里面可能有很多牛逼的工具，为了提升小弟们的工作效率，
     * 给小弟们都复制一个，丢到小弟们的兜里，然后小弟就可以从自己的兜里拿去这些东西使用了，
     * 也可以清空自己兜里面的东西。
     *
     * Thread对象中有个inheritableThreadLocals变量，代码如下：
     * ThreadLocal.ThreadLocalMap inheritableThreadLocals = null;
     * inheritableThreadLocals相当于线程中另外一种兜，这种兜有什么特征呢，当创建子线程的时候，
     * 子线程会将父线程这种类型兜的东西全部复制一份放到自己的inheritableThreadLocals兜中，
     * 使用InheritableThreadLocal对象可以操作线程中的inheritableThreadLocals兜。
     *
     * InheritableThreadLocal常用的方法也有3个：
     * //向Thread中某个口袋中放东西
     * public void set(T value);
     * //获取这个口袋中目前放的东西
     * public T get();
     * //清空这个口袋中放的东西
     * public void remove()
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

    /**
     *
     * 使用InheritableThreadLocal解决上面子线程中无法输出traceId的问题，
     * 只需要将上一个示例代码中的ThreadLocal替换成InheritableThreadLocal即可
     *
     */
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
