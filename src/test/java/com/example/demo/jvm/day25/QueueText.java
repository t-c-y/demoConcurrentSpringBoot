package com.example.demo.jvm.day25;

public class QueueText {
    /**
     *
     * Queue接口
     * 队列是一种先进先出（FIFO）的数据结构，java中用Queue接口来表示队列。
     *
     * Queue接口中定义了6个方法：
     *
     * public interface Queue<E> extends Collection<E> {
     *     boolean add(e);
     *     boolean offer(E e);
     *     E remove();
     *     E poll();
     *     E element();
     *     E peek();
     * }
     *
     * 每个Queue方法都有两种形式：
     * （1）如果操作失败则抛出异常，
     * （2）如果操作失败，则返回特殊值（null或false，具体取决于操作），接口的常规结构如下表所示。
     *
     * 操作类型	抛出异常	返回特殊值
     * 插入	add(e)	offer(e)
     * 移除	remove()	poll()
     * 检查	element()	peek()
     *
     * Queue从Collection继承的add方法插入一个元素，除非它违反了队列的容量限制，
     * 在这种情况下它会抛出IllegalStateException；
     * offer方法与add不同之处仅在于它通过返回false来表示插入元素失败。
     *
     * remove和poll方法都移除并返回队列的头部，确切地移除哪个元素是由具体的实现来决定的，
     * 仅当队列为空时，remove和poll方法的行为才有所不同，在这些情况下，
     * remove抛出NoSuchElementException，而poll返回null。
     *
     * element和peek方法返回队列头部的元素，但不移除，它们之间的差异与remove和poll的方式完全相同，
     * 如果队列为空，则element抛出NoSuchElementException，而peek返回null。
     *
     * 队列一般不要插入空元素。
     *
     * BlockingQueue接口
     * BlockingQueue位于juc中，熟称阻塞队列， 阻塞队列首先它是一个队列，继承Queue接口，
     * 是队列就会遵循先进先出（FIFO）的原则，又因为它是阻塞的，故与普通的队列有两点区别：
     * 1.当一个线程向队列里面添加数据时，如果队列是满的，那么将阻塞该线程，暂停添加数据
     * 2.当一个线程从队列里面取出数据时，如果队列是空的，那么将阻塞该线程，暂停取出数据
     *
     * BlockingQueue相关方法：
     * 操作类型	抛出异常	  返回特殊值	一直阻塞	超时退出
     * 插入	    add(e)	  offer(e)	put(e)	offer(e,timeuout,unit)
     * 移除	   remove()	  poll()	take()	poll(timeout,unit)
     * 检查	   element()  peek()	不支持	不支持
     * 重点，再来解释一下，加深印象：
     *
     * 3个可能会有异常的方法，add、remove、element；这3个方法不会阻塞
     * （是说队列满或者空的情况下是否会阻塞）；队列满的情况下，add抛出异常；队列为空情况下，
     * remove、element抛出异常
     *
     * offer、poll、peek 也不会阻塞（是说队列满或者空的情况下是否会阻塞）；队列满的情况下，
     * offer返回false；队列为空的情况下，pool、peek返回null
     *
     * 队列满的情况下，调用put方法会导致当前线程阻塞
     * 队列为空的情况下，调用take方法会导致当前线程阻塞
     *
     * offer(e,timeuout,unit)，超时之前，插入成功返回true，否者返回false
     * poll(timeout,unit)，超时之前，获取到头部元素并将其移除，返回true，否者返回false
     *
     * 以上一些方法希望大家都记住，方便以后使用
     *
     * BlockingQueue常见的实现类
     *
     * ArrayBlockingQueue
     * 基于数组的阻塞队列实现，其内部维护一个定长的数组，用于存储队列元素。
     * 线程阻塞的实现是通过ReentrantLock来完成的，数据的插入与取出共用同一个锁，
     * 因此ArrayBlockingQueue并不能实现生产、消费同时进行。而且在创建ArrayBlockingQueue时，
     * 我们还可以控制对象的内部锁是否采用公平锁，默认采用非公平锁。
     *
     * LinkedBlockingQueue
     * 基于单向链表的阻塞队列实现，在初始化LinkedBlockingQueue的时候可以指定大小，
     * 也可以不指定，默认类似一个无限大小的容量（Integer.MAX_VALUE），
     * 不指队列容量大小也是会有风险的，一旦数据生产速度大于消费速度，系统内存将有可能被消耗殆尽，
     * 因此要谨慎操作。另外LinkedBlockingQueue中用于阻塞生产者、消费者的锁是两个（锁分离），
     * 因此生产与消费是可以同时进行的。
     *
     * PriorityBlockingQueue
     * 一个支持优先级排序的无界阻塞队列，进入队列的元素会按照优先级进行排序
     *
     * SynchronousQueue
     * 同步阻塞队列，SynchronousQueue没有容量，与其他BlockingQueue不同，
     * SynchronousQueue是一个不存储元素的BlockingQueue，每一个put操作必须要等待一个take操作，
     * 否则不能继续添加元素，反之亦然
     *
     * DelayQueue
     * DelayQueue是一个支持延时获取元素的无界阻塞队列，里面的元素全部都是“可延期”的元素，
     * 列头的元素是最先“到期”的元素，如果队列里面没有元素到期，是不能从列头获取元素的，
     * 哪怕有元素也不行，也就是说只有在延迟期到时才能够从队列中取元素
     *
     * LinkedTransferQueue
     * LinkedTransferQueue是基于链表的FIFO无界阻塞队列，它出现在JDK7中，
     * Doug Lea 大神说LinkedTransferQueue是一个聪明的队列，
     * 它是ConcurrentLinkedQueue、SynchronousQueue(公平模式下)、
     * 无界的LinkedBlockingQueues等的超集，
     * LinkedTransferQueue包含了
     * ConcurrentLinkedQueue、SynchronousQueue、LinkedBlockingQueues三种队列的功能
     *
     * ----------------------------------------------------------------------------
     *
     * LinkedTransferQueue
     * LinkedTransferQueue是一个由链表结构组成的无界阻塞TransferQueue队列。
     * 相对于其他阻塞队列，LinkedTransferQueue多了tryTransfer和transfer方法。
     *
     * LinkedTransferQueue类继承自AbstractQueue抽象类，并且实现了TransferQueue接口：
     * public interface TransferQueue<E> extends BlockingQueue<E> {
     *     // 如果存在一个消费者已经等待接收它，则立即传送指定的元素，否则返回false，并且不进入队列。
     *     boolean tryTransfer(E e);
     *     // 如果存在一个消费者已经等待接收它，则立即传送指定的元素，否则等待直到元素被消费者接收。
     *     void transfer(E e) throws InterruptedException;
     *     // 在上述方法的基础上设置超时时间
     *     boolean tryTransfer(E e, long timeout, TimeUnit unit)
     *         throws InterruptedException;
     *     // 如果至少有一位消费者在等待，则返回true
     *     boolean hasWaitingConsumer();
     *     // 获取所有等待获取元素的消费线程数量
     *     int getWaitingConsumerCount();
     * }
     *
     * 再看一下上面的这些方法，transfer(E e)方法和SynchronousQueue的put方法类似，
     * 都需要等待消费者取走元素，否者一直等待。
     * 其他方法和ArrayBlockingQueue、LinkedBlockingQueue中的方法类似。
     *
     * 总结
     * 1.重点需要了解BlockingQueue中的所有方法，以及他们的区别
     * 2.重点掌握ArrayBlockingQueue、LinkedBlockingQueue、PriorityBlockingQueue、
     * DelayQueue的使用场景
     * 3.需要处理的任务有优先级的，使用PriorityBlockingQueue
     * 4.处理的任务需要延时处理的，使用DelayQueue
     *
     * ----------------------------------------------------------------------------
     *
     *
     */
}
