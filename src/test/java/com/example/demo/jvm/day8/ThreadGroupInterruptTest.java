package com.example.demo.jvm.day8;

import java.util.concurrent.TimeUnit;

public class ThreadGroupInterruptTest {
    /**
     * 测试线程组中断
     * 调用线程组interrupt()，
     * 会将线程组树下的所有子孙线程中断标志置为true，
     * 可以用来批量中断线程。
     *
     * 停止线程之后，通过list()方法可以看出输出的信息中不包含已结束的线程了。
     *
     * 建议创建线程或者线程组的时候，给他们取一个有意义的名字，
     * 对于计算机来说，可能名字并不重要，但是在系统出问题的时候，
     * 可能会去查看线程堆栈信息，如果你看到的都是t1、t2、t3，
     * 估计自己也比较崩溃，如果看到的是httpAccpHandler、dubboHandler类似的名字，应该会好很多。
     */

    public static class R1 implements Runnable {
        @Override
        public void run() {
            Thread t = Thread.currentThread();
            System.out.println("所属线程组：" + t.getThreadGroup().getName()
                    + " ,线程名称：" + t.getName());
            while(!t.isInterrupted()) {

            }
            System.out.println("所属线程组：" + t.getThreadGroup().getName()
                    + " ,线程名称：" + t.getName() + "线程停止啦。。。");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        //创建线程组
        ThreadGroup tg = new ThreadGroup("threadGroup1");
        //创建线程
        Thread t1 = new Thread(tg, new R1(), "t1");
        Thread t2 = new Thread(tg, new R1(), "t2");
        t1.start();
        t2.start();
        TimeUnit.SECONDS.sleep(1);
        System.out.println("============================");
        //创建子线程组
        ThreadGroup tg2 = new ThreadGroup(tg, "threadGroup2");
        //创建线程
        Thread t3 = new Thread(tg2, new R1(), "t3");
        Thread t4 = new Thread(tg2, new R1(), "t4");
        t3.start();
        t4.start();
        TimeUnit.SECONDS.sleep(1);
        System.out.println("============================");
        System.out.println("线程组1的信息：");
        //打印输出线程关系
        tg.list();
        System.out.println("中断线程组1。。。");
        //中断线程组
        tg.interrupt();
        //延时
        TimeUnit.SECONDS.sleep(2);
        System.out.println("中断后线程组1的信息：");
        //打印输出线程组关系
        tg.list();
    }

}
