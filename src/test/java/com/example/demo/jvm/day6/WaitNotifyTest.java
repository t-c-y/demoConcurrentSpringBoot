package com.example.demo.jvm.day6;

public class WaitNotifyTest {
    /**
     * 线程Wait 和 Notify 测试
     *
     * 这里强调一点，Object.wait()方法并不能随便调用。
     * 它必须包含在对应的synchronize语句汇总，
     * 无论是wait()方法或者notify()方法都需要首先获取目标独享的一个监视器。
     * wait()方法和nofiy()方法的工作流程细节。
     * 其中T1和T2表示两个线程。
     * T1在正确执行wait()方法钱，必须获得object对象的监视器。
     * 而wait()方法在执行后，会释放这个监视器。
     * 这样做的目的是使其他等待在object对象上的线程不至于因为T1的休眠而全部无法正常执行。
     *
     * 线程T2在notify()方法调用前，也必须获得object对象的监视器。
     * 所幸，此时T1已经释放了这个监视器，因此，T2可以顺利获得object对象的监视器。
     * 接着，T2执行了notify()方法尝试唤醒一个等待线程，这里假设唤醒了T1。
     * T1在被唤醒后，要做的第一件事并不是执行后续代码，
     * 而是要尝试重新获得object对象的监视器，
     * 而这个监视器也正是T1在wait()方法执行前所持有的那个。
     * 如果暂时无法获得，则T1还必须等待这个监视器。
     * 当监视器顺利获得后，T1才可以在真正意义上继续执行。
     *
     * 注意下打印结果，T2调用notify方法之后，T1并不能立即继续执行，
     * 而是要等待T2释放objec投递锁之后，T1重新成功获取锁后，才能继续执行。
     * 因此最后2行日志相差了2秒（因为T2调用notify方法后休眠了2秒）。
     *
     * 注意：Object.wait()方法和Thread.sleeep()方法都可以让现场等待若干时间。
     * 除wait()方法可以被唤醒外，另外一个主要的区别就是wait()方法会释放目标对象的锁，
     * 而Thread.sleep()方法不会释放锁。
     *
     */


    public static Object obj = new Object();

    public static class T1 extends Thread {

        @Override
        public void run() {

            synchronized (obj) {
                System.out.println(System.currentTimeMillis() + " T1 开始啦。。。");
                //进入等待状态
                try {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(System.currentTimeMillis() + " T1 进入等待状态。。。");
                    obj.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(System.currentTimeMillis() + " T1 线程运行结束");
            }
        }
    }

    public static class T2 extends Thread {

        @Override
        public void run() {

            synchronized (obj) {
                System.out.println(System.currentTimeMillis() + " T2 开始啦。。。");
                //进入等待状态
                obj.notify();
                System.out.println(System.currentTimeMillis() + " T2 唤醒线程。。。");
                System.out.println(System.currentTimeMillis() + " T2 线程运行结束");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        new T1().start();
        new T2().start();
    }

}
