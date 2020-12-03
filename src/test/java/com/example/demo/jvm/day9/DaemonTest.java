package com.example.demo.jvm.day9;

public class DaemonTest {

    /**
     * 守护线程测试
     *
     * 守护线程是一种特殊的线程，在后台默默地完成一些系统性的服务，
     * 比如垃圾回收线程、JIT线程都是守护线程。
     * 与之对应的是用户线程，用户线程可以理解为是系统的工作线程，
     * 它会完成这个程序需要完成的业务操作。
     * 如果用户线程全部结束了，意味着程序需要完成的业务操作已经结束了，
     * 系统可以退出了。
     * 所以当系统只剩下守护进程的时候，java虚拟机会自动退出。
     *
     * java线程分为用户线程和守护线程，线程的daemon属性为true表示是守护线程，false表示是用户线程。
     */


    public static class R1 implements Runnable {
        @Override
        public void run() {
            Thread t = Thread.currentThread();
            System.out.println("线程名称：" + t.getName()
                    + " ," + (t.isDaemon() ? "我是守护线程" : "我是用户线程"));
            while(true) {

            }
        }
    }

    /**
     * 测试守护线程和普通线程
     *
     * 当程序中所有的用户线程执行完毕之后，不管守护线程是否结束，系统都会自动退出。
     * 设置守护线程，需要在start()方法之前进行
     *
     * setDaemon()方法必须在线程的start()方法之前调用，在后面调用会报异常，并且不起效
     *
     */
    private static void test1() {
        Thread t = new Thread(new R1(), "t1");
//        t.setDaemon(true);
        t.start();
        System.out.println("主线程结束。。。");
    }

    public static void main(String[] args) {
//        test1();
        test2();
    }

    /**
     * 线程的daemon默认值和其父线程一样
     *
     * Thread类的init()方法中
     * Thread parent = currentThread();
     * this.daemon = parent.isDaemon();
     *
     */
    private static void test2() {
        Thread t = Thread.currentThread();
        System.out.println("线程名称：" + t.getName()
                + " ," + (t.isDaemon() ? "我是守护线程" : "我是用户线程"));
        Thread t1 = new Thread(new R2(), "t1");
        t1.start();
        Thread t2 = new Thread(){
            public void run() {
                System.out.println("线程名称：" + this.getName()
                        + " ," + (this.isDaemon() ? "我是守护线程" : "我是用户线程"));
                Thread t3 = new Thread(new R2(), "t3");
                t3.start();
            }
        };
        t2.setName("t2");
        t2.setDaemon(true);
        t2.start();
    }

    public static class R2 implements Runnable {
        @Override
        public void run() {
            Thread t = Thread.currentThread();
            System.out.println("线程名称：" + t.getName()
                    + " ," + (t.isDaemon() ? "我是守护线程" : "我是用户线程"));
        }
    }

}
