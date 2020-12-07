package com.example.demo.jvm.day12;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockInterruptTest {
    /**
     * ReentrantLock 中断测试
     *
     * ReentrantLock获取锁的过程是可中断的
     * 对于synchronized关键字，如果一个线程在等待获取锁，最终只有2种结果：
     * 要么获取到锁然后继续后面的操作
     * 要么一直等待，直到其他线程释放锁为止
     * 而ReentrantLock提供了另外一种可能，就是在等的获取锁的过程中
     * （发起获取锁请求到还未获取到锁这段时间内）是可以被中断的，也就是说在等待锁的过程中，
     * 程序可以根据需要取消获取锁的请求。有些使用这个操作是非常有必要的。
     * 比如：你和好朋友越好一起去打球，如果你等了半小时朋友还没到，突然你接到一个电话，
     * 朋友由于突发状况，不能来了，那么你一定达到回府。
     * 中断操作正是提供了一套类似的机制，如果一个线程正在等待获取锁，
     * 那么它依然可以收到一个通知，被告知无需等待，可以停止工作了。
     *
     * 关于获取锁的过程中被中断，注意几点:
     * 1.ReentrankLock中必须使用实例方法 lockInterruptibly()获取锁时，
     * 在线程调用interrupt()方法之后，才会引发 InterruptedException异常
     * 2.线程调用interrupt()之后，线程的中断标志会被置为true
     * 3.触发InterruptedException异常之后，线程的中断标志有会被清空，即置为false
     * 4.所以当线程调用interrupt()引发InterruptedException异常，
     * 中断标志的变化是:false->true->false
     *
     */

    private static ReentrantLock lock1 = new ReentrantLock();
    private static ReentrantLock lock2 = new ReentrantLock();

    public static class T extends Thread {
        int lock;
        public T(String name, int lock) {
            super(name);
            this.lock = lock;
        }

        @Override
        public void run() {
            System.out.println(this.getName() + " 线程开始");
            try {
                if(this.lock == 1) {
                    lock1.lockInterruptibly();
                    TimeUnit.SECONDS.sleep(1);
                    lock2.lockInterruptibly();
                } else {
                    lock2.lockInterruptibly();
                    TimeUnit.SECONDS.sleep(1);
                    lock1.lockInterruptibly();
                }
            } catch (InterruptedException e) {
                System.out.println(this.getName() + " 中断标识：" + this.isInterrupted());
                e.printStackTrace();
            } finally {
                if(lock1.isHeldByCurrentThread()) {
                    lock1.unlock();
                }
                if(lock2.isHeldByCurrentThread()) {
                    lock2.unlock();
                }
            }
            System.out.println(this.getName() + " 线程结束");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // lock1被线程t1占用，lock2倍线程t2占用，线程t1在等待获取lock2，
        // 线程t2在等待获取lock1，都在相互等待获取对方持有的锁，最终产生了死锁，
        // 如果是在synchronized关键字情况下发生了死锁现象，程序是无法结束的。
        Thread t1 = new T("t1", 1);
        Thread t2 = new T("t2", 2);
        t1.start();
        t2.start();

        // 添加下边代码改造，解决死锁问题
        //// t2在31行一直获取不到lock1的锁，主线程中等待了5秒之后，
        //// t2线程调用了 interrupt()方法，将线程的中断标志置为true，
        //// 此时31行会触发 InterruptedException异常，然后线程t2可以继续向下执行，
        //// 释放了lock2的锁，然后线程t1可以正常获取锁，程序得以继续进行。
        //// 线程发送中断信号触发InterruptedException异常之后，中断标志将被清空。
        TimeUnit.SECONDS.sleep(5);
        t2.interrupt();
        System.out.println("t2.interrupt = [" + t2.isInterrupted() + "]");
    }



}
