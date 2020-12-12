package com.example.demo.jvm.day13;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingQueueDemo<E> {
    /**
     * 测试Condition多个锁
     *
     */
    int size;
    ReentrantLock lock = new ReentrantLock();
    Condition notFull = lock.newCondition();
    Condition notEmpty = lock.newCondition();
    LinkedList<E> list = new LinkedList();

    public BlockingQueueDemo(int size) {
        this.size = size;
    }

    public void enqueue(E e) {
        lock.lock();
        try {
            while(list.size() == size) {
                notFull.await();
            }
            list.add(e);
            System.out.println("入队列：" + e);
            notEmpty.signal();
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public E dequeue() {
        E e = null;
        lock.lock();
        try {
            while (list.size() == 0) {
                notEmpty.await();
            }
            e = list.removeFirst();
            System.out.println("出队列：" + e);
            notFull.signal();
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        } finally {
            lock.unlock();
        }
        return e;
    }

    /**
     * 代码非常容易理解，创建了一个阻塞队列，大小为3，队列满的时候，会被阻塞，
     * 等待其他线程去消费，队列中的元素被消费之后，会唤醒生产者，生产数据进入队列。
     * 上面代码将队列大小置为1，可以实现同步阻塞队列，生产1个元素之后，生产者会被阻塞，
     * 待消费者消费队列中的元素之后，生产者才能继续工作。
     * @param args
     */
    public static void main(String[] args) {
        BlockingQueueDemo<Integer> que = new BlockingQueueDemo<>(3);
        for(int i=0; i<10; i++) {
            int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    que.enqueue(finalI);
                }
            }).start();
        }
        for(int i=0; i<10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    que.dequeue();
                }
            }).start();
        }
    }
}
