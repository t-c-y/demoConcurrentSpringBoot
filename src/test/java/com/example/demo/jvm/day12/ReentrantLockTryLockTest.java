package com.example.demo.jvm.day12;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockTryLockTest {
    /**
     * ReentrantLock 测试锁申请等待时限
     * 申请锁等待限时是什么意思？一般情况下，获取锁的时间我们是不知道的，
     * synchronized关键字获取锁的过程中，只能等待其他线程把锁释放之后才能够有机会获取到所。
     * 所以获取锁的时间有长有短。如果获取锁的时间能够设置超时时间，那就非常好了。
     *
     * ReentrantLock刚好提供了这样功能，给我们提供了获取锁限时等待的方法 tryLock()，
     * 可以选择传入时间参数，表示等待指定的时间，
     * 无参则表示立即返回锁申请的结果：true表示获取锁成功，false表示获取锁失败。
     *
     * 关于tryLock()方法和tryLock(long timeout, TimeUnit unit)方法，说明一下：
     * 1.都会返回boolean值，结果表示获取锁是否成功
     * 2.tryLock()方法，不管是否获取成功，都会立即返回；
     * 而有参的tryLock方法会尝试在指定的时间内去获取锁，中间会阻塞的现象，
     * 在指定的时间之后会不管是否能够获取锁都会返回结果
     * 3.tryLock()方法不会响应线程的中断方法；
     * 而有参的tryLock方法会响应线程的中断方法，而出发 InterruptedException异常，
     * 这个从2个方法的声明上可以可以看出来
     *
     * isHeldByCurrentThread：实例方法，判断当前线程是否持有ReentrantLock的锁
     *
     */

    private static ReentrantLock lock = new ReentrantLock();

    public static class T extends Thread {

        public T(String name) {
            super(name);
        }

        @Override
        public void run() {
            System.out.println(this.getName() + " 开始线程，尝试获取锁");
            try {
                if(lock.tryLock()) {
//                if(lock.tryLock(3, TimeUnit.SECONDS)) {
//                if(lock.tryLock(6, TimeUnit.SECONDS)) {
                    System.out.println(this.getName() + " 已经获取到锁");
                    TimeUnit.SECONDS.sleep(5);
                } else {
                    System.out.println(this.getName() + " 获取锁失败");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if(lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }

    public static void main(String[] args) {
        Thread t1 = new T("t1");
        Thread t2 = new T("t2");
        t1.start();
        t2.start();
    }

}
