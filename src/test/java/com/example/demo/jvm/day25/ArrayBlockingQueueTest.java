package com.example.demo.jvm.day25;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ArrayBlockingQueueTest {

    /**
     *
     * ArrayBlockingQueue
     * 有界阻塞队列，内部使用数组存储元素，有2个常用构造方法：
     *
     * //capacity表示容量大小，默认内部采用非公平锁
     * public ArrayBlockingQueue(int capacity)
     * //capacity：容量大小，fair：内部是否是使用公平锁
     * public ArrayBlockingQueue(int capacity, boolean fair)
     *
     * 需求：业务系统中有很多地方需要推送通知，由于需要推送的数据太多，
     * 我们将需要推送的信息先丢到阻塞队列中，然后开一个线程进行处理真实发送
     *
     */

    static ArrayBlockingQueue<String> pushQueue = new ArrayBlockingQueue<>(10);

    static {
        new Thread(()->{
            while(true) {
                String msg;
                try {
                    long sT = System.currentTimeMillis();
                    msg = pushQueue.take();
                    TimeUnit.MILLISECONDS.sleep(500);
                    long eT = System.currentTimeMillis();
                    System.out.println(String.format("[%s,%s,take耗时:%s],%s,发送消息:%s", sT, eT, (eT - sT), Thread.currentThread().getName(), msg));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void pushMsg(String msg) throws InterruptedException {
        pushQueue.put(msg);
    }

    /**
     * 代码中我们使用了有界队列ArrayBlockingQueue，创建ArrayBlockingQueue时候需要制定容量大小，
     * 调用pushQueue.put将推送信息放入队列中，如果队列已满，此方法会阻塞。
     * 代码中在静态块中启动了一个线程，调用pushQueue.take();从队列中获取待推送的信息进行推送处理。
     *
     * 注意：ArrayBlockingQueue如果队列容量设置的太小，消费者发送的太快，消费者消费的太慢的情况下，
     * 会导致队列空间满，调用put方法会导致发送者线程阻塞，所以注意设置合理的大小，协调好消费者的速度。
     *
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {

        for(int i=1; i<=5; i++) {
            String msg = "一起来学java高并发，第" + i + "天";
            pushMsg(msg);
        }
    }

}
