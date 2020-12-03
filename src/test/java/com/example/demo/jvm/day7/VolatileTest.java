package com.example.demo.jvm.day7;

public class VolatileTest {

    /**
     * 测试volatile
     *
     * 线程之间的共享变量存储在主内存（main memory）中，
     * 每个线程都有一个私有的本地内存（local memory），
     * 本地内存中存储了该线程以读/写共享变量的副本
     *
     * 线程t1中为何看不到被主线程修改为false的flag的值，有两种可能:
     * 1.主线程修改了flag之后，未将其刷新到主内存，所以t1看不到
     * 2.主线程将flag刷新到了主内存，但是t1一直读取的是自己工作内存中flag的值，
     * 没有去主内存中获取flag最新的值
     *
     * java帮我们提供了这样的方法，使用volatile修饰共享变量，就可以达到下边面的效果：
     * 1.线程中修改了工作内存中的副本之后，立即将其刷新到主内存；
     * 2.工作内存中每次读取共享变量时，都去主内存中重新读取，然后拷贝到工作内存。
     *
     * 被volatile修改的变量有以下特点：
     * 线程中读取的时候，每次读取都会去主内存中读取共享变量最新的值，然后将其复制到工作内存
     * 线程中修改了工作内存中变量的副本，修改之后会立即刷新到主内存
     *
     */

//    public static boolean flg = true;
    public static volatile boolean flg = true;

    public static class T1 extends Thread {
        public void run() {
            System.out.println("线程开始。。。");
            while(flg) {

            }
            System.out.println("线程结束");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.printf("主方法开始");
        new T1().start();
        Thread.sleep(2000);
        flg = false;
    }

}
