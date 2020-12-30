package com.example.demo.jvm.day32;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

public class CountTest {

    /**
     *
     * AtomicLong内部采用CAS的方式实现，并发量大的情况下，CAS失败率比较高，
     * 导致性能比synchronized还低一些。并发量不是太大的情况下，CAS性能还是可以的。
     *
     *
     *
     *
     */

    static LongAdder count = new LongAdder();

    public static void incr() {
        count.increment();
    }

    /**
     *
     * LongAdder实现
     * 先介绍一下LongAdder，说到LongAdder，不得不提的就是AtomicLong，
     * AtomicLong是JDK1.5开始出现的，里面主要使用了一个long类型的value作为成员变量，
     * 然后使用循环的CAS操作去操作value的值，并发量比较大的情况下，CAS操作失败的概率较高，
     * 内部失败了会重试，导致耗时可能会增加。
     * LongAdder是JDK1.8开始出现的，所提供的API基本上可以替换掉原先的AtomicLong。
     * LongAdder在并发量比较大的情况下，操作数据的时候，相当于把这个数字分成了很多份数字，
     * 然后交给多个人去管控，每个管控者负责保证部分数字在多线程情况下操作的正确性。
     * 当多线程访问的时，通过hash算法映射到具体管控者去操作数据，最后再汇总所有的管控者的数据，
     * 得到最终结果。相当于降低了并发情况下锁的粒度，所以效率比较高
     *
     */
    public static void m1() throws InterruptedException {
        long t1 = System.currentTimeMillis();
        int tNum = 50;
        CountDownLatch countDownLatch = new CountDownLatch(tNum);
        for(int i = 0; i < tNum; i++) {
            new Thread(()->{
                try {
                    for(int j = 0; j < 1000000; j++) {
                        incr();
                    }
                } finally {
                    countDownLatch.countDown();
                }
            }).start();
        }
        countDownLatch.await();
        long t2 = System.currentTimeMillis();
        System.out.println(String.format("结果：%s,耗时(ms)：%s", count.sum(), (t2 - t1)));
    }

    /**
     *
     * LongAccumulator介绍
     * LongAccumulator是LongAdder的功能增强版。LongAdder的API只有对数值的加减，
     * 而LongAccumulator提供了自定义的函数操作，其构造函数如下：
     *
     * public LongAccumulator(LongBinaryOperator accumulatorFunction, long identity) {
     *      this.function = accumulatorFunction;
     *      base = this.identity = identity;
     * }
     *
     * LongAccumulator的效率和LongAdder差不多，不过更灵活一些。
     *
     * 调用new LongAdder()等价于new LongAccumulator((x, y) -> x + y, 0L)。
     *
     * 从上面4个示例的结果来看，LongAdder、LongAccumulator全面超越同步锁及AtomicLong的方式，
     * 建议在使用AtomicLong的地方可以直接替换为LongAdder、LongAccumulator，吞吐量更高一些。
     *
     */

    static LongAccumulator count1 = new LongAccumulator((x,y)-> x+y, 0);

    public static void incr1() {
        count1.accumulate(1);
    }

    public static void m2() throws InterruptedException {
        long t1 = System.currentTimeMillis();
        int tNum = 50;
        CountDownLatch countDownLatch = new CountDownLatch(tNum);
        for(int i = 0; i < tNum; i++) {
            new Thread(()->{
                try {
                    for(int j = 0; j < 1000000; j++) {
                        incr1();
                    }
                } finally {
                    countDownLatch.countDown();
                }
            }).start();
        }
        countDownLatch.await();
        long t2 = System.currentTimeMillis();
        System.out.println(String.format("结果：%s,耗时(ms)：%s", count1.longValue(), (t2 - t1)));
    }

    public static void main(String[] args) throws InterruptedException {
        for(int i = 0; i < 10; i++) {
//            count.reset();
//            m1();
            count1.reset();
            m2();
        }
    }

}
