package com.example.demo.jvm.day25;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

public class PriorityBlockingQueueTest {

    /**
     *
     * PriorityBlockingQueue
     * 无界的优先级阻塞队列，内部使用数组存储数据，达到容量时，会自动进行扩容，
     * 放入的元素会按照优先级进行排序，4个构造方法：
     * //默认构造方法，默认初始化容量是11
     * public PriorityBlockingQueue();
     * //指定队列的初始化容量
     * public PriorityBlockingQueue(int initialCapacity);
     * //指定队列的初始化容量和放入元素的比较器
     * public PriorityBlockingQueue(int initialCapacity,Comparator<? super E> comparator);
     * //传入集合放入来初始化队列，传入的集合可以实现SortedSet接口或者PriorityQueue接口进行排序，
     * //如果没有实现这2个接口，按正常顺序放入队列
     * public PriorityBlockingQueue(Collection<? extends E> c);
     *
     * 优先级队列放入元素的时候，会进行排序，所以我们需要指定排序规则，有2种方式：
     * 1.创建PriorityBlockingQueue指定比较器Comparator
     * 2.放入的元素需要实现Comparable接口
     *
     * 上面2种方式必须选一个，如果2个都有，则走第一个规则排序。
     *
     * 需求：还是上面的推送业务，目前推送是按照放入的先后顺序进行发送的，比如有些公告比较紧急，优先级比较高，
     * 需要快点发送，怎么搞？此时PriorityBlockingQueue就派上用场了
     *
     */

    static class Msg implements Comparable<Msg> {

        public Msg(int priority, String msg) {
            this.priority = priority;
            this.msg = msg;
        }

        private int priority;
        private String msg;

        @Override
        public String toString() {
            return "Msg{priority=" + priority
                    + " ,msg=" + msg + "}";
        }

        @Override
        public int compareTo(Msg o) {
            return Integer.compare(priority, o.priority);
        }
    }

    static PriorityBlockingQueue<Msg> parseQueue = new PriorityBlockingQueue<>();

    static {
        new Thread(() -> {
            while(true) {
                Msg msg;
                try {
                    long st = System.currentTimeMillis();
                    msg = parseQueue.take();
                    TimeUnit.MILLISECONDS.sleep(100);
                    long et = System.currentTimeMillis();
                    System.out.println(String.format("[%s,%s,take耗时:%s],%s,发送消息:%s",
                            st, et, (et - st), Thread.currentThread().getName(), msg));
                } catch (InterruptedException e) {

                }
            }
        }).start();
    }

    public static void pushMsg(int pri, String msg) {
        parseQueue.put(new Msg(pri, msg));
    }

    /**
     * main中放入了5条推送信息，i作为消息的优先级按倒叙放入的，最终输出结果中按照优先级由小到大输出。
     * 注意Msg实现了Comparable接口，具有了比较功能。
     * @param args
     */
    public static void main(String[] args) {
        for(int i = 5; i > 0; i--) {
            pushMsg(i, "一起来学java高并发,第" + i + "天");
        }
    }


}
