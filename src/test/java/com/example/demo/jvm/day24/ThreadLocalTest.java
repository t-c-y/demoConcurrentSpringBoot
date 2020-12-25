package com.example.demo.jvm.day24;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadLocalTest {
    /**
     *
     * ThreadLocal
     * 还是拿上面的问题，我们来分析一下，每个请求都是由一个线程处理的，线程就相当于一个人一样，
     * 每个请求相当于一个任务，任务来了，人来处理，处理完毕之后，再处理下一个请求任务。
     * 人身上是不是有很多口袋，人刚开始准备处理任务的时候，我们把任务的编号放在处理者的口袋中，
     * 然后处理中一路携带者，处理过程中如果需要用到这个编号，直接从口袋中获取就可以了。
     * 那么刚好java中线程设计的时候也考虑到了这些问题，Thread对象中就有很多口袋，用来放东西。
     * Thread类中有这么一个变量：
     * ThreadLocal.ThreadLocalMap threadLocals = null;
     *
     * 这个就是用来操作Thread中所有口袋的东西，ThreadLocalMap源码中有一个数组
     * （有兴趣的可以去看一下源码），对应处理者身上很多口袋一样，数组中的每个元素对应一个口袋。
     *
     * 如何来操作Thread中的这些口袋呢，java为我们提供了一个类ThreadLocal，
     * ThreadLocal对象用来操作Thread中的某一个口袋，可以向这个口袋中放东西、获取里面的东西、
     * 清除里面的东西，这个口袋一次性只能放一个东西，重复放东西会将里面已经存在的东西覆盖掉。
     *
     * 常用的3个方法：
     * //向Thread中某个口袋中放东西
     * public void set(T value);
     * //获取这个口袋中目前放的东西
     * public T get();
     * //清空这个口袋中放的东西
     * public void remove()
     *
     * ------------------------------------------------------------------------------
     *
     * ThreadLocal的官方API解释为：
     *
     * “该类提供了线程局部 (thread-local) 变量。这些变量不同于它们的普通对应物，
     * 因为访问某个变量（通过其 get 或 set 方法）的每个线程都有自己的局部变量，
     * 它独立于变量的初始化副本。ThreadLocal 实例通常是类中的 private static 字段，
     * 它们希望将状态与某一个线程（例如，用户 ID 或事务 ID）相关联。”
     *
     * ------------------------------------------------------------------------------
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

    public static void log(String msg) {
        StackTraceElement stack[] = (new Throwable()).getStackTrace();
        System.out.println("****" + System.currentTimeMillis()
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
        log("执行数据库操作");
        for(String s : dataList) {
            log("插入 "+s+" 成功");
        }
    }

    public static void test() {
        List<String> dataList = new ArrayList<>();
        for(int i=0; i<3; i++) {
            dataList.add("数据："+i);
        }

        for (int i = 0; i < 5; i++) {
            executor.execute(()->{
                controller(dataList);
            });
        }

        executor.shutdown();
    }

    static ThreadLocal<String> traceIdKD = new ThreadLocal<>();

    public static void log1(String msg) {
        StackTraceElement stack[] = (new Throwable()).getStackTrace();
        System.out.println("****" + System.currentTimeMillis()
                + ",[tId:" + traceIdKD.get() + "]"
                + ",[线程:" + Thread.currentThread().getName() + "],"
                + stack[1] + ":" + msg);
    }

    public static void controller1(List<String> dataList) {
        log1("接受请求");
        service1(dataList);
    }

    public static void service1(List<String> dataList) {
        log1("执行业务");
        dao1(dataList);
    }

    public static void dao1(List<String> dataList) {
        log1("执行数据库操作");
        for(String s : dataList) {
            log1("插入 "+s+" 成功");
        }
    }

    /**
     *
     * 代码中创建了一个ThreadLocal traceIdKD，这个对象用来操作Thread中一个口袋，
     * 用这个口袋来存放tranceId。在main方法中通过traceIdKD.set(traceId)方法将traceId放入口袋，
     * log方法中通traceIdKD.get()获取口袋中的traceId，最后任务处理完之后，
     * main中的finally中调用traceIdKD.remove();将口袋中的traceId清除。
     *
     *
     */
    public static void test1() {
        List<String> dataList = new ArrayList<>();
        for(int i=0; i<3; i++) {
            dataList.add("数据："+i);
        }

        for (int i = 0; i < 5; i++) {
            String tId = String.valueOf(i);
            executor.execute(()->{
                traceIdKD.set(tId);
                try {
                    controller1(dataList);
                } finally {
                    traceIdKD.remove();
                }
            });
        }

        executor.shutdown();
    }


    public static void main(String[] args) {
        // 模拟并发请求，一整套逻辑
//        test();
        // 请求携带标识
        test1();
    }

}
