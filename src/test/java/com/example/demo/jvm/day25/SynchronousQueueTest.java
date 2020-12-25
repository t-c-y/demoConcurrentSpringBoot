package com.example.demo.jvm.day25;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class SynchronousQueueTest {
    /**
     *
     * SynchronousQueue
     * 同步阻塞队列，SynchronousQueue没有容量，与其他BlockingQueue不同，
     * SynchronousQueue是一个不存储元素的BlockingQueue，每一个put操作必须要等待一个take操作，
     * 否则不能继续添加元素，反之亦然。SynchronousQueue 在现实中用的不多，线程池中有用到过，
     * Executors.newCachedThreadPool()实现中用到了这个队列，当有任务丢入线程池的时候，
     * 如果已创建的工作线程都在忙于处理任务，则会新建一个线程来处理丢入队列的任务。
     *
     */

    static SynchronousQueue<String> queue = new SynchronousQueue<>();

    /**
     * main方法中启动了一个线程，调用queue.put方法向队列中丢入一条数据，
     * 调用的时候产生了阻塞，从输出结果中可以看出，直到take方法被调用时，
     * put方法才从阻塞状态恢复正常。
     *
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        new Thread(()->{
            try {
                long st = System.currentTimeMillis();
                queue.put("java高并发系列课程！！！");
                long et = System.currentTimeMillis();
                System.out.println(String.format("[%s,%s,take耗时:%s],%s",
                        st, et, (et - st), Thread.currentThread().getName()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        TimeUnit.SECONDS.sleep(5);
        System.out.println(System.currentTimeMillis() + "，调用take移除元素，" + queue.take());
    }

}
