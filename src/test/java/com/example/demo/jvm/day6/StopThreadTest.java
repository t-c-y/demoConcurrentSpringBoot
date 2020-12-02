package com.example.demo.jvm.day6;

public class StopThreadTest {
    /**
     * 测试停止线程
     *
     * 终止线程
     * 一般来说线程执行完毕就会结束，无需手动关闭。
     * 但是如果我们想关闭一个正在运行的线程，有什么方法呢？
     * 可以看一下Thread类中提供了一个stop()方法，
     * 调用这个方法，就可以立即将一个线程终止，非常方便。
     *
     * 代码中有个死循环，调用stop方法之后，
     * 线程thread1的状态变为TERMINATED（结束状态），线程停止了。
     * 我们使用idea或者eclipse的时候，会发现这个方法是一个废弃的方法，
     * 也就是说，在将来，jdk可能就会移除该方法。
     * stop方法为何会被废弃而不推荐使用？stop方法过于暴力，强制把正在执行的方法停止了
     *
     */

    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(){
            @Override
            public void run() {
                System.out.println("线程开始");
                while(true) {
                    if(1==2) {
                        break;
                    }
                }
                System.out.println("线程结束");
            }
        };

        t.start();
        Thread.sleep(1000);
        System.out.println(t.getState());
        t.stop();
        Thread.sleep(1000);
        System.out.println(t.getState());
    }
}
