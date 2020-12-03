package com.example.demo.jvm.day8;

import java.util.concurrent.*;

public class ThreadGroupTest {

    /**
     * 线程组测试
     *
     */

    public static class R1 implements Runnable {
        @Override
        public void run() {
            Thread t = Thread.currentThread();
            System.out.println("threadGroup:" + t.getThreadGroup().getName()
                    + " threadNmae:"+t.getName());
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
//        test1();
//        test2();
        test3();
    }

    /**
     * 根线程
     * 主线程的线程组为main
     * 根线程组为system
     */
    public static void test3() {
        System.out.println(Thread.currentThread().getName());
        System.out.println(Thread.currentThread().getThreadGroup());
        System.out.println(Thread.currentThread().getThreadGroup().getParent());
        System.out.println(Thread.currentThread().getThreadGroup().getParent().getParent());
    }

    /**
     * 为线程组指定父线程
     *
     * threadGroup1未指定父线程组，
     * 系统获取了主线程的线程组作为threadGroup1的父线程组，输出结果中是：main
     *
     * threadGroup1为threadGroup2的父线程组
     *
     * threadGroup1活动线程数为4，包含了threadGroup1线程组中的t1、t2，
     * 以及子线程组threadGroup2中的t3、t4
     *
     * 线程组的list()方法，将线程组中的所有子孙节点信息输出到控制台，用于调试使用
     *
     */
    public static void test2() throws InterruptedException {
        ThreadGroup tg = new ThreadGroup("threadGroup1");
        Thread t1 = new Thread(tg, new R1(), "t1");
        Thread t2 = new Thread(tg, new R1(), "t2");
        t1.start();
        t2.start();
        TimeUnit.SECONDS.sleep(1);
        System.out.println("threadGroup1活动线程数："+tg.activeCount());
        System.out.println("threadGroup1活动线程组："+tg.activeGroupCount());
        System.out.println("threadGroup1线程组名称："+tg.getName());
        System.out.println("threadGroup1父线程组名称："+tg.getParent().getName());
        System.out.println("=====================================");
        ThreadGroup tg2 = new ThreadGroup(tg, "threadGroup2");
        Thread t3 = new Thread(tg2, new R1(), "t3");
        Thread t4 = new Thread(tg2, new R1(), "t4");
        t3.start();
        t4.start();
        TimeUnit.SECONDS.sleep(1);
        System.out.println("threadGroup2活动线程数："+tg2.activeCount());
        System.out.println("threadGroup2活动线程组："+tg2.activeGroupCount());
        System.out.println("threadGroup2线程组名称："+tg2.getName());
        System.out.println("threadGroup2父线程组名称："+tg2.getParent().getName());
        System.out.println("=====================================");
        System.out.println("threadGroup1活动线程数："+tg.activeCount());
        System.out.println("threadGroup1活动线程组："+tg.activeGroupCount());
        System.out.println("=====================================");
        tg.list();
    }

    /**
     * 线程组
     * 我们可以把线程归属到某个线程组中，线程组可以包含多个线程以及线程组，
     * 线程和线程组组成了父子关系，是个树形结构
     *
     * 使用线程组可以方便管理线程，线程组提供了一些方法方便方便我们管理线程。
     *
     * activeCount()方法可以返回线程组中的所有活动线程数，包含下面的所有子孙节点的线程，
     * 由于线程组中的线程是动态变化的，这个值只能是一个估算值。
     */
    public static void test1() throws InterruptedException {
        ThreadGroup tg = new ThreadGroup("thread-group-1");
        Thread t1 = new Thread(tg, new R1(), "t1");
        Thread t2 = new Thread(tg, new R1(), "t2");
        t1.start();
        t2.start();
        TimeUnit.SECONDS.sleep(1);
        System.out.println("活动线程数："+tg.activeCount());
        System.out.println("活动线程组："+tg.activeGroupCount());
        System.out.println("线程组名称："+tg.getName());
        tg.list();
        TimeUnit.SECONDS.sleep(2);
        tg.list();
    }

}
