package com.example.demo.jvm.day6;

public class InterruptThreadTest {
    /**
     * 中断线程
     *
     * 在java中，线程中断是一种重要的线程写作机制，从表面上理解，
     * 中断就是让目标线程停止执行的意思，实际上并非完全如此。
     * 在上面中，我们已经详细讨论了stop方法停止线程的坏处，
     * jdk中提供了更好的中断线程的方法。
     * 严格的说，线程中断并不会使线程立即退出，而是给线程发送一个通知，
     * 告知目标线程，有人希望你退出了！至于目标线程接收到通知之后如何处理，
     * 则完全由目标线程自己决定，这点很重要，如果中断后，线程立即无条件退出，
     * 我们又会到stop方法的老问题。
     *
     * Thread提供了3个与线程中断有关的方法，这3个方法容易混淆，大家注意下：
     * public void interrupt() //中断线程
     * public boolean isInterrupted() //判断线程是否被中断
     * public static boolean interrupted() //判断线程是否被中断，并清除当前中断状态
     *
     * 注意：
     * sleep方法由于中断而抛出异常之后，线程的中断标志会被清除（置为false），
     * 所以在异常中需要执行this.interrupt()方法，将中断标志位置为true
     *
     */

    public static void main(String[] args) throws InterruptedException {
        test2();
    }

    public static void test2() throws InterruptedException {
        Thread t = new Thread() {
            @Override
            public void run() {
                System.out.println("线程开始。。。");
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // sleep方法由于中断而抛出异常之后，
                        // 线程的中断标志会被清除（置为false），
                        // 所以在异常中需要执行this.interrupt()方法，将中断标志位置为true
                        this.interrupt();
                        e.printStackTrace();
                    }

                    if(this.isInterrupted()) {
                        System.out.println("有人让线程退出。。。");
                        break;
                    }
                }
                System.out.println("线程结束。。。");
            }
        };

        t.start();
        Thread.sleep(1000);
        t.interrupt();
    }

    public static void test1() throws InterruptedException {
        Thread t = new Thread() {
            @Override
            public void run() {
                System.out.println("线程开始。。。");
                while (true) {
                    if(this.isInterrupted()) {
                        System.out.println("有人让线程退出。。。");
                        break;
                    }
                }
                System.out.println("线程结束。。。");
            }
        };

        t.start();
        Thread.sleep(1000);
        System.out.println(t.getState());
        System.out.println("开始中断");
//        t.interrupt();
//        System.out.println(t.getState());
//        System.out.println("线程中断状态：" + t.interrupted());
        System.out.println(t.getState());
        t.interrupt();
        System.out.println(t.getState());
        Thread.sleep(1000);
        System.out.println(t.getState());
//        System.out.println("线程中断状态：" + t.interrupted());
    }


}
