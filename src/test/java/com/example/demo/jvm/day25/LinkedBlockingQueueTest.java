package com.example.demo.jvm.day25;

public class LinkedBlockingQueueTest {

    /**
     *
     * LinkedBlockingQueue
     * 内部使用单向链表实现的阻塞队列，3个构造方法：
     *
     * //默认构造方法，容量大小为Integer.MAX_VALUE
     * public LinkedBlockingQueue();
     * //创建指定容量大小的LinkedBlockingQueue
     * public LinkedBlockingQueue(int capacity);
     * //容量为Integer.MAX_VALUE,并将传入的集合丢入队列中
     * public LinkedBlockingQueue(Collection<? extends E> c);
     *
     * LinkedBlockingQueue的用法和ArrayBlockingQueue类似，建议使用的时候指定容量，
     * 如果不指定容量，插入的太快，移除的太慢，可能会产生OOM。
     *
     */


}
