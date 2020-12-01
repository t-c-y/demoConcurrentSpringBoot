package com.example.demo.jvm.day1;

import java.util.concurrent.*;

public class StarvationlockTest {

    /**
     * 编写饥饿引起的死锁
     *
     * #查看java进程pid
     * ps -ef | grep DeadlockTest
     * #查看死锁信息
     * jstack <pid>
     *
     * 然后通过jstack方式查看信息如下：
     * 堆栈信息结合图中的代码，可以看出主线程在72行处于等待中，
     * 线程池中的工作线程在65行处于等待中，等待获取结果。
     * 由于线程池是一个线程，AnotherCallable得不到执行，而被饿死，最终导致了程序死锁的现象。
     *
     *
     * "pool-1-thread-1" #10 prio=5 os_prio=31 tid=0x00007fefe9982000 nid=0x4103 waiting on condition [0x0000700004c3a000]
     *    java.lang.Thread.State: WAITING (parking)
     *         at sun.misc.Unsafe.park(Native Method)
     *         - parking to wait for  <0x000000079596bc58> (a java.util.concurrent.FutureTask)
     *         at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
     *         at java.util.concurrent.FutureTask.awaitDone(FutureTask.java:429)
     *         at java.util.concurrent.FutureTask.get(FutureTask.java:191)
     *         at com.example.demo.jvm.day1.StarvationlockTest$MyCallable.call(StarvationlockTest.java:65)
     *         at com.example.demo.jvm.day1.StarvationlockTest$MyCallable.call(StarvationlockTest.java:50)
     *         at java.util.concurrent.FutureTask.run(FutureTask.java:266)
     *         at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
     *         at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
     *         at java.lang.Thread.run(Thread.java:748)
     *
     *
     * "main" #1 prio=5 os_prio=31 tid=0x00007fefe9809000 nid=0x1003 waiting on condition [0x0000700003c07000]
     *    java.lang.Thread.State: WAITING (parking)
     *         at sun.misc.Unsafe.park(Native Method)
     *         - parking to wait for  <0x00000007957d55c0> (a java.util.concurrent.FutureTask)
     *         at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
     *         at java.util.concurrent.FutureTask.awaitDone(FutureTask.java:429)
     *         at java.util.concurrent.FutureTask.get(FutureTask.java:191)
     *         at com.example.demo.jvm.day1.StarvationlockTest.main(StarvationlockTest.java:72)
     *
     */

    private static ExecutorService single = Executors.newSingleThreadExecutor();
    private static ExecutorService pool = Executors.newFixedThreadPool(2);

    public static class AnotherCallable implements Callable<String> {

        @Override
        public String call() throws Exception {
            System.out.println("in AnotherCallable");
            return "another success";
        }
    }

    public static class MyCallable implements Callable<String> {

        @Override
        public String call() throws Exception {
            System.out.println("in MyCallable");
            Future<String> submit = single.submit(new AnotherCallable());
            return "my success " + submit.get();
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        MyCallable task = new MyCallable();
        Future<String> submit = single.submit(task);
        System.out.println(submit.get());
        System.out.println("over");
        single.shutdown();
    }

}
