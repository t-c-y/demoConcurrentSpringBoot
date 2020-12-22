package com.example.demo.jvm.day21;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CASTest {
    /**
     * java中的CAS
     *
     * CAS 的问题
     * cas这么好用，那么有没有什么问题呢？还真有
     * ABA问题
     * CAS需要在操作值的时候检查下值有没有发生变化，如果没有发生变化则更新，但是如果一个值原来是A，
     * 变成了B，又变成了A，那么使用CAS进行检查时会发现它的值没有发生变化，但是实际上却变化了。
     * 这就是CAS的ABA问题。常见的解决思路是使用版本号。在变量前面追加上版本号，
     * 每次变量更新的时候把版本号加一，那么A-B-A 就会变成1A-2B-3A。
     * 目前在JDK的atomic包里提供了一个类AtomicStampedReference来解决ABA问题。
     * 这个类的compareAndSet方法作用是首先检查当前引用是否等于预期引用，并且当前标志是否等于预期标志，
     * 如果全部相等，则以原子方式将该引用和该标志的值设置为给定的更新值。
     *
     * 循环时间长开销大
     * 上面我们说过如果CAS不成功，则会原地循环（自旋操作），如果长时间自旋会给CPU带来非常大的执行开销。
     * 并发量比较大的情况下，CAS成功概率可能比较低，可能会重试很多次才会成功。
     *
     *
     */

    static int count = 0;

    public static void request() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(5);
        count++;
    }

    /**
     * 代码中的count用来记录总访问次数，request()方法表示访问一次，内部休眠5毫秒模拟内部耗时，
     * request方法内部对count++操作。程序最终耗时1秒多，执行还是挺快的，
     * 但是count和我们期望的结果不一致，我们期望的是1000，实际输出的是973（每次运行结果可能都不一样）。
     *
     * 分析一下问题出在哪呢？
     * 代码中采用的是多线程的方式来操作count，count++会有线程安全问题，count++操作实际上是由以下三步操作完成的：
     *
     * 获取count的值，记做A：A=count
     * 将A的值+1，得到B：B = A+1
     * 让B赋值给count：count = B
     *
     * 如果有A、B两个线程同时执行count++，他们同时执行到上面步骤的第1步，
     * 得到的count是一样的，3步操作完成之后，count只会+1，导致count只加了一次，从而导致结果不准确。
     *
     * 那么我们应该怎么做的呢？
     * 对count++操作的时候，我们让多个线程排队处理，多个线程同时到达request()方法的时候，
     * 只能允许一个线程可以进去操作，其他的线程在外面候着，等里面的处理完毕出来之后，
     * 外面等着的再进去一个，这样操作count++就是排队进行的，结果一定是正确的。
     *
     * 我们前面学了synchronized、ReentrantLock可以对资源加锁，保证并发的正确性，
     * 多线程情况下可以保证被锁的资源被串行访问，那么我们用synchronized来实现一下。
     *
     * @throws InterruptedException
     */
    private static void test() throws InterruptedException {
        long st = System.currentTimeMillis();
        int threadSize = 100;
        CountDownLatch latch = new CountDownLatch(threadSize);
        for(int i=0; i < threadSize; i++) {
            Thread t = new Thread(()->{
                try {
                    for(int j=0;j<10;j++) {
                        request();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
            t.start();
        }
        latch.await();
        long et = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + ", use time:" + (et-st) + ", count:" + count);
    }

    public static synchronized void request1() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(5);
        count++;
    }

    /**
     * 程序中request方法使用synchronized关键字，保证了并发情况下，
     * request方法同一时刻只允许一个线程访问，request加锁了相当于串行执行了，
     * count的结果和我们预期的结果一致，只是耗时比较长，5秒多。
     *
     * @throws InterruptedException
     */
    private static void test1() throws InterruptedException {
        long st = System.currentTimeMillis();
        int threadSize = 100;
        CountDownLatch latch = new CountDownLatch(threadSize);
        for(int i=0; i < threadSize; i++) {
            Thread t = new Thread(()->{
                try {
                    for(int j=0;j<10;j++) {
                        request1();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
            t.start();
        }
        latch.await();
        long et = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + ", use time:" + (et-st) + ", count:" + count);
    }

    volatile static int countV = 0;
//    static int countV = 0;

    private static void request2() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(5);
        int c = countV;
        while(!compareAndSwap(c, c+1)) {
            c = countV;
        }
    }

    private static synchronized boolean compareAndSwap(int c, int c1) {
        if(countV == c) {
            countV = c1;
            return true;
        }
        return false;
    }

    /**
     * 代码中用了volatile关键字修饰了count，可以保证count在多线程情况下的可见性。
     * 关于volatile关键字的使用，也是非常非常重要的，前面有讲过，
     * 不太了解的朋友可以去看一下：volatile与Java内存模型
     *
     * 咱们再看一下代码，compareAndSwap方法，我们给起个简称吧叫CAS，这个方法有什么作用呢？
     * 这个方法使用synchronized修饰了，能保证此方法是线程安全的，多线程情况下此方法是串行执行的。
     * 方法由两个参数，expectCount：表示期望的值，newCount：表示要给count设置的新值。
     * 方法内部通过getCount()获取count当前的值，然后与期望的值expectCount比较，
     * 如果期望的值和count当前的值一致，则将新值newCount赋值给count。
     *
     * 再看一下request()方法，方法中有个do-while循环，循环内部获取count当前值赋值给了expectCount，
     * 循环结束的条件是compareAndSwap返回true，也就是说如果compareAndSwap如果不成功，
     * 循环再次获取count的最新值，然后+1，再次调用compareAndSwap方法，直到compareAndSwap返回成功为止。
     *
     * 代码中相当于将count++拆分开了，只对最后一步加锁了，减少了锁的范围，此代码的性能是不是比方式2快不少，
     * 还能保证结果的正确性。大家是不是感觉这个compareAndSwap方法挺好的，这东西确实很好，
     * java中已经给我们提供了CAS的操作，功能非常强大，我们继续向下看。
     *
     * @throws InterruptedException
     */
    private static void test2() throws InterruptedException {
        long st = System.currentTimeMillis();
        int threadSize = 100;
        CountDownLatch latch = new CountDownLatch(threadSize);
        for(int i=0; i < threadSize; i++) {
            Thread t = new Thread(()->{
                try {
                    for(int j=0;j<10;j++) {
                        request2();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
            t.start();
        }
        latch.await();
        long et = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + ", use time:" + (et-st) + ", countV:" + countV);

    }

    static AtomicInteger a = new AtomicInteger();

    public static void request3() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(5);
        //对count原子加一
        a.incrementAndGet();
    }

    /**
     * juc框架中提供了一些原子操作，底层是通过Unsafe类中的cas操作实现的。
     * 通过原子操作可以保证数据在并发情况下的正确性。
     *
     * 此处我们使用java.util.concurrent.atomic.AtomicInteger类来实现计数器功能，
     * AtomicInteger内部是采用cas操作来保证对int类型数据增减操作在多线程情况下的正确性。
     *
     *
     * @throws InterruptedException
     */
    private static void test3() throws InterruptedException {
        long st = System.currentTimeMillis();
        int threadSize = 100;
        CountDownLatch latch = new CountDownLatch(threadSize);
        for(int i=0; i < threadSize; i++) {
            Thread t = new Thread(()->{
                try {
                    for(int j=0;j<10;j++) {
                        request3();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
            t.start();
        }
        latch.await();
        long et = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + ", use time:" + (et-st) + ", countV:" + a.get());

    }

    public static void main(String[] args) throws InterruptedException {
        // 测试累加
//        test();
        // 测试synchronized累加
//        test1();
        // volatile累加
//        test2();
        // AtomicInteger
        test3();
    }
}
