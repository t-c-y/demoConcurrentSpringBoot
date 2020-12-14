package com.example.demo.jvm.day15;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SemaphoreTest {
    /**
     * 测试Semaphore
     * Semaphore（信号量）为多线程协作提供了更为强大的控制方法，
     * 前面的文章中我们学了synchronized和重入锁ReentrantLock，
     * 这2种锁一次都只能允许一个线程访问一个资源，
     * 而信号量可以控制有多少个线程可以访问特定的资源。
     *
     * Semaphore常用场景：限流
     * 举个例子：
     * 比如有个停车场，有5个空位，门口有个门卫，手中5把钥匙分别对应5个车位上面的锁，
     * 来一辆车，门卫会给司机一把钥匙，然后进去找到对应的车位停下来，
     * 出去的时候司机将钥匙归还给门卫。停车场生意比较好，同时来了100两车，
     * 门卫手中只有5把钥匙，同时只能放5辆车进入，其他车只能等待，等有人将钥匙归还给门卫之后，
     * 才能让其他车辆进入。
     * 上面的例子中门卫就相当于Semaphore，车钥匙就相当于许可证，车就相当于线程。
     */

    static Semaphore s = new Semaphore(2);

    public static class T extends Thread {
        public T(String name) {
            super(name);
        }

        @Override
        public void run() {
            try {
                s.acquire();
                System.out.println(System.currentTimeMillis() + ", " + this.getName() + ", 获取信号量");
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                s.release();
                System.out.println(System.currentTimeMillis() + ", " + this.getName() + ", 释放信号量");
            }

        }
    }

    /**
     * 代码中 newSemaphore(2)创建了许可数量为2的信号量，每个线程获取1个许可，
     * 同时允许两个线程获取许可，从输出中也可以看出，同时有两个线程可以获取许可，
     * 其他线程需要等待已获取许可的线程释放许可之后才能运行。
     * 为获取到许可的线程会阻塞在 acquire()方法上，直到获取到许可才能继续。
     */
    private static void test() {
        for(int i=0;i<10;i++) {
            new T("t" + i).start();
        }
    }

    public static class T1 extends Thread {
        public T1(String name) {
            super(name);
        }

        @Override
        public void run() {
            try {
                s.acquire();
                System.out.println(System.currentTimeMillis() + ", " + this.getName() + ", 获取信号量");
                TimeUnit.SECONDS.sleep(3);
                System.out.println(System.currentTimeMillis() + ", " + this.getName() + ", 运行结束");
                System.out.println(System.currentTimeMillis() + ", " + this.getName() + ", 当前可用信号量数量：" + s.availablePermits());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 程序运行后一直无法结束，观察一下代码，代码中获取许可后，
     * 没有释放许可的代码，最终导致，可用许可数量为0，其他线程无法获取许可，
     * 会在 semaphore.acquire();处等待，导致程序无法结束。
     */
    private static void test1() {
        for(int i=0;i<10;i++) {
            new T1("t" + i).start();
        }
    }

    static Semaphore s1 = new Semaphore(1);

    public static class T2 extends Thread {
        public T2(String name) {
            super(name);
        }

        @Override
        public void run() {
            try {
                s1.acquire();
                System.out.println(System.currentTimeMillis() + "," + this.getName() + ",获取信号量！当前信号量剩余数量：" + s1.availablePermits());
                TimeUnit.SECONDS.sleep(10);
                System.out.println(System.currentTimeMillis() + "," + this.getName() + ",运行结束！");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                s1.release();
            }
            System.out.println(System.currentTimeMillis() + "," + this.getName() + ",当前信号量剩余数量：" + s1.availablePermits());
        }
    }

    /**
     * 程序中信号量许可数量为1，创建了3个线程获取许可，线程t1获取成功了，
     * 然后休眠30秒。其他两个线程阻塞在 semaphore.acquire();方法处，
     * 代码中对线程t2、t3发送中断信号，我们看一下Semaphore中acquire的源码：
     * public void acquire() throws InterruptedException;
     *
     * 这个方法会响应线程中断，主线程中对t2、t3发送中断信号之后，
     * acquire()方法会触发 InterruptedException异常，
     * t2、t3最终没有获取到许可，但是他们都执行了finally中的释放许可的操作，
     * 最后导致许可数量变为了2，导致许可数量增加了。所以程序中释放许可的方式有问题。
     * 需要改进一下，获取许可成功才去释放锁。
     *
     * @throws InterruptedException
     */
    private static void test2() throws InterruptedException {
        Thread t1 = new T2("t1");
        t1.start();
        TimeUnit.SECONDS.sleep(1);
        Thread t2 = new T2("t2");
        t2.start();
        TimeUnit.SECONDS.sleep(1);
        Thread t3 = new T2("t3");
        t3.start();

        //中断t2和t3
        t2.interrupt();
        t3.interrupt();

    }

    public static class T3 extends Thread {
        public T3(String name) {
            super(name);
        }

        @Override
        public void run() {
            boolean isSuccess = false;
            try {
                s1.acquire();
                System.out.println(System.currentTimeMillis() + "," + this.getName() + ",获取信号量！当前信号量剩余数量：" + s1.availablePermits());
                TimeUnit.SECONDS.sleep(30);
                System.out.println(System.currentTimeMillis() + "," + this.getName() + ",运行结束！");
                isSuccess = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if(isSuccess) {
                    s1.release();
                }
            }
            System.out.println(System.currentTimeMillis() + "," + this.getName() + ",当前信号量剩余数量：" + s1.availablePermits());
        }
    }

    /**
     * 程序中增加了一个变量 isSuccess用来标记获取许可是否成功，
     * 在finally中根据这个变量是否为true，来确定是否释放许可。
     * @throws InterruptedException
     */
    private static void test3() throws InterruptedException {
        Thread t1 = new T3("t1");
        t1.start();
        TimeUnit.SECONDS.sleep(1);
        Thread t2 = new T3("t2");
        t2.start();
        TimeUnit.SECONDS.sleep(1);
        Thread t3 = new T3("t3");
        t3.start();

        //中断t2和t3
        t2.interrupt();
        t3.interrupt();

    }

    public static class T4 extends Thread {
        public T4(String name) {
            super(name);
        }

        @Override
        public void run() {
            boolean isSuccess = false;
            try {
                System.out.println(System.currentTimeMillis() + "," + this.getName() + ",尝试获取信号量！当前信号量剩余数量：" + s1.availablePermits());
                isSuccess = s1.tryAcquire(1, TimeUnit.SECONDS);
                if (isSuccess) {
                    System.out.println(System.currentTimeMillis() + "," + this.getName() + ",获取信号量！当前信号量剩余数量：" + s1.availablePermits());
                    TimeUnit.SECONDS.sleep(5);
                } else {
                    System.out.println(System.currentTimeMillis() + "," + this.getName() + ",获取信号量失败！运行结束！当前信号量剩余数量：" + s1.availablePermits());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if(isSuccess) {
                    s1.release();
                }
            }
        }
    }

    /**
     * 在指定的时间内去尝试获取许可，如果能够获取到，返回true，获取不到返回false。
     *
     * 线程尝试在1秒内获取许可，获取成功立即返回true，超过1秒还是获取不到，返回false。
     * 线程t1获取许可成功，之后休眠了5秒，从输出中可以看出t2和t3都尝试了1秒，获取失败。
     *
     * @throws InterruptedException
     */
    private static void test4() throws InterruptedException {
        Thread t1 = new T4("t1");
        t1.start();
        TimeUnit.SECONDS.sleep(1);
        Thread t2 = new T4("t2");
        t2.start();
        TimeUnit.SECONDS.sleep(1);
        Thread t3 = new T4("t3");
        t3.start();
    }

    public static void main(String[] args) throws InterruptedException {
        //测试Semaphore
//        test();
        //测试Semaphore不释放信号量
//        test1();
        //测试 释放许可
//        test2();
        //测试 释放许可-修改bug
//        test3();
        //测试等待指定时间
        test4();
    }
}
