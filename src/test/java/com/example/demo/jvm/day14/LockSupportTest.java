package com.example.demo.jvm.day14;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class LockSupportTest {
    /**
     * LockSupport
     *
     * Object和Condition的局限性
     * 关于Object和Condtion中线程等待和唤醒的局限性，有以下几点：
     * 1.让线程等待和唤醒的方法能够执行的先决条件是：线程需要先获取锁
     * 2.唤醒方法需要在等待方法之后调用，线程才能够被唤醒
     *
     * LockSupport类介绍
     * LockSupport类可以阻塞当前线程以及唤醒指定被阻塞的线程。
     * 主要是通过park()和unpark(thread)方法来实现阻塞和唤醒线程的操作的。
     *
     * 每个线程都有一个许可(permit)，permit只有两个值1和0，默认是0。
     * 1.当调用unpark(thread)方法，就会将thread线程的许可permit设置成1
     * (注意多次调用unpark方法，不会累加，permit值还是1)。
     * 2.当调用park()方法，如果当前线程的permit是1，那么将permit设置为0，并立即返回。
     * 如果当前线程的permit是0，那么当前线程就会阻塞，直到别的线程将当前线程的permit设置为1时，
     * park方法会被唤醒，然后会将permit再次设置为0，并返回。
     * 注意：因为permit默认是0，所以一开始调用park()方法，线程必定会被阻塞。
     * 调用unpark(thread)方法后，会自动唤醒thread线程，即park方法立即返回。
     *
     * LockSupport中常用的方法
     * 阻塞线程
     * 1.void park()：
     * 阻塞当前线程，如果调用unpark方法或者当前线程被中断，从能从park()方法中返回
     * 2.void park(Object blocker)：
     * 功能同方法1，入参增加一个Object对象，用来记录导致线程阻塞的阻塞对象，方便进行问题排查
     * 3.void parkNanos(long nanos)：
     * 阻塞当前线程，最长不超过nanos纳秒，增加了超时返回的特性
     * 4.void parkNanos(Object blocker, long nanos)：
     * 功能同方法3，入参增加一个Object对象，用来记录导致线程阻塞的阻塞对象，方便进行问题排查
     * 5.void parkUntil(long deadline)：
     * 阻塞当前线程，直到deadline，deadline是一个绝对时间，表示某个时间的毫秒格式
     * 6.void parkUntil(Object blocker, long deadline)：
     * 功能同方法5，入参增加一个Object对象，用来记录导致线程阻塞的阻塞对象，方便进行问题排查；
     * 唤醒线程
     * 1.void unpark(Thread thread):唤醒处于阻塞状态的指定线程
     *
     */

    /**
     * t1中调用 LockSupport.park();
     * 让当前线程t1等待，主线程休眠了5秒之后，调用 LockSupport.unpark(t1);
     * 将t1线程唤醒，输出结果中1、3行结果相差5秒左右，说明t1线程等待5秒之后，被唤醒了。
     * LockSupport.park();
     * 无参数，内部直接会让当前线程处于等待中；
     * unpark方法传递了一个线程对象作为参数，表示将对应的线程唤醒。
     * @throws InterruptedException
     */
    private static void test() throws InterruptedException {
        Thread t1 = new Thread(()->{
            System.out.println(System.currentTimeMillis() + "," + Thread.currentThread().getName() + " start");
            LockSupport.park();
            System.out.println(System.currentTimeMillis() + "," + Thread.currentThread().getName() + " 被唤醒！");
        });
        t1.setName("t1");
        t1.start();
        TimeUnit.SECONDS.sleep(5);
        LockSupport.unpark(t1);
        System.out.println(System.currentTimeMillis() + " , LockSupport.unpack()执行完毕！");
    }

    /**
     * 代码中启动t1线程，t1线程内部休眠了5秒，然后主线程休眠1秒之后，调用了 LockSupport.unpark(t1);
     * 唤醒线程t1，此时 LockSupport.park();方法还未执行，说明唤醒方法在等待方法之前执行的；
     * 输出结果中2、3行结果时间一样，表示 LockSupport.park();没有阻塞了，是立即返回的。
     *
     * 说明：唤醒方法在等待方法之前执行，线程也能够被唤醒，这点是另外2中方法无法做到的。
     * Object和Condition中的唤醒必须在等待之后调用，线程才能被唤醒。
     * 而LockSupport中，唤醒的方法不管是在等待之前还是在等待之后调用，线程都能够被唤醒。
     *
     * @throws InterruptedException
     */
    private static void test1() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(System.currentTimeMillis() + "," + Thread.currentThread().getName() + " start");
            LockSupport.park();
            System.out.println(System.currentTimeMillis() + "," + Thread.currentThread().getName() + " 被唤醒！");
        });
        t1.setName("t1");
        t1.start();
        TimeUnit.SECONDS.sleep(1);
        LockSupport.unpark(t1);
        System.out.println(System.currentTimeMillis() + " , LockSupport.unpack()执行完毕！");
    }

    /**
     * t1线程中调用了park()方法让线程等待，主线程休眠了5秒之后，调用 t1.interrupt();
     * 给线程t1发送中断信号，然后线程t1从等待中被唤醒了，输出结果中的1、4行结果相差5秒左右，
     * 刚好是主线程休眠了5秒之后将t1唤醒了。结论：park方法可以相应线程中断。
     *
     * LockSupport.park方法让线程等待之后，唤醒方式有2种：
     * 1.调用LockSupport.unpark方法
     * 2.调用等待线程的 interrupt()方法，给等待的线程发送中断信号，可以唤醒线程
     * @throws InterruptedException
     */
    private static void test2() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            System.out.println(System.currentTimeMillis() + "," + Thread.currentThread().getName() + ",start");
            System.out.println(System.currentTimeMillis() + ",1中断状态：" + Thread.currentThread().isInterrupted());
            LockSupport.park();
            System.out.println(System.currentTimeMillis() + ",2中断状态：" + Thread.currentThread().isInterrupted());
            System.out.println(System.currentTimeMillis() + "," + Thread.currentThread().getName() + ",被唤醒！");
        });
        t1.setName("t1");
        t1.start();
        TimeUnit.SECONDS.sleep(3);
        t1.interrupt();
    }

    public static void main(String[] args) throws InterruptedException {
        //测试LockSupport
//        test();
        //测试提前执行unpack
//        test1();
        //测试中断
        test2();
    }


}
