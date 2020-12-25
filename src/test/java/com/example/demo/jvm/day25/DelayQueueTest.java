package com.example.demo.jvm.day25;

import java.util.Calendar;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayQueueTest {
    /**
     *
     * DelayQueue
     * DelayQueue是一个支持延时获取元素的无界阻塞队列，里面的元素全部都是“可延期”的元素，
     * 列头的元素是最先“到期”的元素，如果队列里面没有元素到期，是不能从列头获取元素的，
     * 哪怕有元素也不行，也就是说只有在延迟期到时才能够从队列中取元素。
     *
     * 需求：还是推送的业务，有时候我们希望早上9点或者其他指定的时间进行推送，如何实现呢？
     * 此时DelayQueue就派上用场了。
     *
     * 我们先看一下DelayQueue类的声明：
     * public class DelayQueue<E extends Delayed> extends AbstractQueue<E>
     *     implements BlockingQueue<E>
     *
     * 元素E需要实现接口Delayed，我们看一下这个接口的代码：
     * public interface Delayed extends Comparable<Delayed> {
     *     long getDelay(TimeUnit unit);
     * }
     *
     * Delayed继承了Comparable接口，这个接口是用来做比较用的，
     * DelayQueue内部使用PriorityQueue来存储数据的，PriorityQueue是一个优先级队列，
     * 丢入的数据会进行排序，排序方法调用的是Comparable接口中的方法。
     * 下面主要说一下Delayed接口中的getDelay方法：此方法在给定的时间单位内返回与此对象关联的剩余延迟时间。
     *
     */

    static class Msg implements Delayed {

        private int priority;
        private String msg;
        private long sendTimeMs;

        public Msg(int priority, String msg, long sendTimeMs) {
            this.priority = priority;
            this.msg = msg;
            this.sendTimeMs = sendTimeMs;
        }

        @Override
        public String toString() {
            return "Msg{priority="
                    + priority +
                    ",msg=" + msg +
                    ",sendTimeMs=" + sendTimeMs +"}";
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(
                    sendTimeMs-Calendar.getInstance().getTimeInMillis(),
                    TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            if(o instanceof Msg) {
                Msg ms = (Msg)o;
                return Integer.compare(priority, ms.priority);
            }
            return 0;
        }
    }

    public static DelayQueue<Msg> delayQueue = new DelayQueue<>();

    static {
        new Thread(()->{
            while(true) {
                Msg msg;
                try {
                    msg = delayQueue.take();
                    long endTime = System.currentTimeMillis();
                    System.out.println(String.format("定时发送时间：%s,实际发送时间：%s,发送消息:%s",
                            msg.sendTimeMs, endTime, msg));
                } catch (InterruptedException e) {

                }
            }
        }).start();
    }

    public static void pushMsg(int pri, String msg, long sendTimeMs) {
        delayQueue.put(new Msg(pri, msg, sendTimeMs));
    }

    /**
     * 可以看出时间发送时间，和定时发送时间基本一致，代码中Msg需要实现Delayed接口，
     * 重点在于getDelay方法，这个方法返回剩余的延迟时间，
     * 代码中使用this.sendTimeMs减去当前时间的毫秒格式时间，得到剩余延迟时间
     *
     * @param args
     */
    public static void main(String[] args) {
        for (int i = 5; i > 0; i--) {
            pushMsg(i, "一起来学习高并发，第"+i+"天",
                    Calendar.getInstance().getTimeInMillis() + i*2000);
        }
    }
}
