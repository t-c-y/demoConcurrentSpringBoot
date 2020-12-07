package com.example.demo.jvm.day12;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockTest {
    /**
     * JUC:ReentrantLock重入锁 测试
     *
     * synchronized是java内置的关键字，它提供了一种独占的加锁方式。
     * synchronized的获取和释放锁由jvm实现，用户不需要显示的释放锁，
     * 非常方便，然而synchronized也有一定的局限性，例如：
     * 1.当线程尝试获取锁的时候，如果获取不到锁会一直阻塞，这个阻塞的过程，用户无法控制
     * 2.如果获取锁的线程进入休眠或者阻塞，除非当前线程异常，否则其他线程尝试获取锁必须一直等待
     *
     * ReentrantLock是Lock的默认实现，在聊ReentranLock之前，我们需要先弄清楚一些概念：
     * 1.可重入锁：可重入锁是指同一个线程可以多次获得同一把锁；
     * ReentrantLock和关键字Synchronized都是可重入锁
     * 2.可中断锁：可中断锁时只线程在获取锁的过程中，
     * 是否可以相应线程中断操作。synchronized是不可中断的，ReentrantLock是可中断的
     * 3.公平锁和非公平锁：公平锁是指多个线程尝试获取同一把锁的时候，
     * 获取锁的顺序按照线程到达的先后顺序获取，而不是随机插队的方式获取。
     * synchronized是非公平锁，而ReentrantLock是两种都可以实现，不过默认是非公平锁
     *
     * ReentrantLock实现公平锁
     * 在大多数情况下，锁的申请都是非公平的，也就是说，线程1首先请求锁A，接着线程2也请求了锁A。
     * 那么当锁A可用时，是线程1可获得锁还是线程2可获得锁呢？
     * 这是不一定的，系统只是会从这个锁的等待队列中随机挑选一个，因此不能保证其公平性。
     * 这就好比买票不排队，大家都围在售票窗口前，售票员忙的焦头烂额，也顾及不上谁先谁后，
     * 随便找个人出票就完事了，最终导致的结果是，有些人可能一直买不到票。而公平锁，
     * 则不是这样，它会按照到达的先后顺序获得资源。
     *
     * 公平锁的一大特点是：它不会产生饥饿现象，只要你排队，最终还是可以等到资源的；
     * synchronized关键字默认是有jvm内部实现控制的，是非公平锁。
     * 而ReentrantLock运行开发者自己设置锁的公平性。
     *
     */

    private static int num = 0;

    private static ReentrantLock lock = new ReentrantLock();



    public static void main(String[] args) throws InterruptedException {
        //测试ReentrantLock-普通锁、可重入锁
//        test();
        //测试公平锁与非公平锁
        test1();
    }

    /**
     * 默认构造方法创建的是非公平锁。
     * 公平锁看起来很不错，不过要实现公平锁，系统内部肯定需要维护一个有序队列，
     * 因此公平锁的实现成本比较高，性能相对于非公平锁来说相对低一些。
     * 因此，在默认情况下，锁是非公平的，如果没有特别要求，则不建议使用公平锁。
     * @throws InterruptedException
     */
    public static void test1() throws InterruptedException {
        Thread t1 = new T1("t1");
        Thread t2 = new T1("t2");
        Thread t3 = new T1("t3");
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
    }

    private static ReentrantLock fairLock = new ReentrantLock(true);
//    private static ReentrantLock fairLock = new ReentrantLock(false);

    public static class T1 extends Thread {

        public T1(String name) {
            super(name);
        }

        @Override
        public void run() {
            for(int i=0; i<5; i++) {
                fairLock.lock();

                try {
                    System.out.println(this.getName()+"获得锁！");
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    fairLock.unlock();
                }
            }
        }
    }

    /**
     * 测试ReentrantLock-普通锁、可重入锁
     * 1.lock()方法和unlock()方法需要成对出现，锁了几次，也要释放几次，
     * 否则后面的线程无法获取锁了；可以将add中的unlock删除一个事实，上面代码运行将无法结束
     * 2.unlock()方法放在finally中执行，保证不管程序是否有异常，锁必定会释放
     *
     * @throws InterruptedException
     */
    public static void test() throws InterruptedException {
        Thread t1 = new T();
        Thread t2 = new T();
        Thread t3 = new T();
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
        System.out.println("num = [" + num + "]");
    }

    public static class T extends Thread {
        @Override
        public void run() {
            for(int i=0; i<1000; i++) {
                ReentrantLockTest.add();
            }
        }
    }

    /**
     * ReentrantLock的使用过程：
     * 1.创建锁：ReentrantLock lock = new ReentrantLock();
     * 2.获取锁：lock.lock()
     * 3.释放锁：lock.unlock()
     *
     * 对比上面的代码，与关键字synchronized相比，ReentrantLock锁有明显的操作过程，
     * 开发人员必须手动的指定何时加锁，何时释放锁，正是因为这样手动控制，
     * ReentrantLock对逻辑控制的灵活度要远远胜于关键字synchronized，
     * 上面代码需要注意lock.unlock()一定要放在finally中，
     * 否则，若程序出现了异常，锁没有释放，那么其他线程就再也没有机会获取这个锁了。
     *
     */
    private static void add() {
        lock.lock();
        lock.lock();
        try {
            num++;
        } finally {
            lock.unlock();
            lock.unlock();
        }
    }

}
