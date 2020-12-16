package com.example.demo.jvm.day16;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CountDownLatchTest {
    /**
     * CountDownLatch 测试
     *
     * CountDownLatch介绍
     * CountDownLatch称之为闭锁，它可以使一个或一批线程在闭锁上等待，
     * 等到其他线程执行完相应操作后，闭锁打开，这些等待的线程才可以继续执行。
     * 确切的说，闭锁在内部维护了一个倒计数器。通过该计数器的值来决定闭锁的状态，
     * 从而决定是否允许等待的线程继续执行。
     *
     * 常用方法：
     * public CountDownLatch(int count)：
     * 构造方法，count表示计数器的值，不能小于0，否者会报异常。
     * public void await() throws InterruptedException：
     * 调用await()会让当前线程等待，直到计数器为0的时候，方法才会返回，此方法会响应线程中断操作。
     * public boolean await(long timeout, TimeUnit unit) throws InterruptedException：
     * 限时等待，在超时之前，计数器变为了0，方法返回true，否者直到超时，
     * 返回false，此方法会响应线程中断操作。
     * public void countDown()：让计数器减1
     *
     * CountDownLatch使用步骤：
     * 1.创建CountDownLatch对象
     * 2.调用其实例方法 await()，让当前线程等待
     * 3.调用 countDown()方法，让计数器减1
     * 4.当计数器变为0的时候， await()方法会返回
     *
     */

    public static class T extends Thread {
        int sleepSeconds;
        CountDownLatch countDownLatch;

        public T(String name, int sleepSeconds, CountDownLatch countDownLatch) {
            super(name);
            this.sleepSeconds = sleepSeconds;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            long sTime = System.currentTimeMillis();
            System.out.println(sTime + " , " + this.getName()
                    + " , 开始执行");
            try {
                TimeUnit.SECONDS.sleep(this.sleepSeconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                this.countDownLatch.countDown();
            }
            long eTime =  System.currentTimeMillis();
            System.out.println(eTime + " , " + this.getName() + " 处理完成，耗时（ms）：" + (eTime-sTime));
        }
    }

    /**
     * 从结果中看出，效果和join实现的效果一样，代码中创建了计数器为2的 CountDownLatch，
     * 主线程中调用 countDownLatch.await();会让主线程等待，t1、t2线程中模拟执行耗时操作，
     * 最终在finally中调用了 countDownLatch.countDown();,此方法每调用一次，
     * CountDownLatch内部计数器会减1，当计数器变为0的时候，主线程中的await()会返回，
     * 然后继续执行。注意：上面的 countDown()这个是必须要执行的方法，所以放在finally中执行。
     * @throws InterruptedException
     */
    private static void test() throws InterruptedException {
        long sTime = System.currentTimeMillis();
        System.out.println(sTime + " , " + Thread.currentThread().getName()
                + " , 开始执行");
        CountDownLatch countDownLatch = new CountDownLatch(2);
        Thread t1 = new T("sheet1",2, countDownLatch);
        Thread t2 = new T("sheet2",5, countDownLatch);
        t1.start();
        t2.start();
        countDownLatch.await();
        long eTime =  System.currentTimeMillis();
        System.out.println(eTime + " , " + Thread.currentThread().getName() + " 处理完成，耗时（ms）：" + (eTime-sTime));
    }

    /**
     * 等待指定的时间
     * 从输出结果中可以看出，线程2耗时了5秒，主线程耗时了2秒，
     * 主线程中调用 countDownLatch.await(2,TimeUnit.SECONDS);
     * 表示最多等2秒，不管计数器是否为0，await方法都会返回，若等待时间内，
     * 计数器变为0了，立即返回true，否则超时后返回false。
     */
    private static void test1() throws InterruptedException {
        long sTime = System.currentTimeMillis();
        System.out.println(sTime + " , " + Thread.currentThread().getName()
                + " , 开始执行");
        CountDownLatch countDownLatch = new CountDownLatch(2);
        Thread t1 = new T("sheet1",2, countDownLatch);
        Thread t2 = new T("sheet2",5, countDownLatch);
        t1.start();
        t2.start();
        boolean result = countDownLatch.await(2, TimeUnit.SECONDS);
        long eTime =  System.currentTimeMillis();
        System.out.println("result:"+result);
        System.out.println(eTime + " , " + Thread.currentThread().getName() + " 处理完成，耗时（ms）：" + (eTime-sTime));
    }

    public static void main(String[] args) throws InterruptedException {
        //测试CountDownLatch
//        test();
        //等待指定的时间
        test1();
    }

}
