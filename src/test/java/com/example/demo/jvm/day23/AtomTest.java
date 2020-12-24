package com.example.demo.jvm.day23;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.atomic.AtomicStampedReference;

public class AtomTest {
    /**
     * JUC中原子类
     * JUC中的原子类都是都是依靠volatile、CAS、Unsafe类配合来实现的
     *
     * JUC中原子类介绍
     * 什么是原子操作？
     * atomic 翻译成中文是原子的意思。在化学上，我们知道原子是构成一般物质的最小单位，
     * 在化学反应中是不可分割的。在我们这里 atomic 是指一个操作是不可中断的。
     * 即使是在多个线程一起执行的时候，一个操作一旦开始，就不会被其他线程干扰，
     * 所以，所谓原子类说简单点就是具有原子操作特征的类，原子操作类提供了一些修改数据的方法，
     * 这些方法都是原子操作的，在多线程情况下可以确保被修改数据的正确性。
     *
     * JUC中对原子操作提供了强大的支持，这些类位于java.util.concurrent.atomic包中
     *
     * 基本类型原子类
     * 使用原子的方式更新基本类型
     * AtomicInteger：int类型原子类
     * AtomicLong：long类型原子类
     * AtomicBoolean ：boolean类型原子类
     *
     * -----------------------------------------------------------------------------
     *
     * AtomicInteger 类常用方法
     *
     * public final int get() //获取当前的值
     * public final int getAndSet(int newValue)//获取当前的值，并设置新的值
     * public final int getAndIncrement()//获取当前的值，并自增
     * public final int getAndDecrement() //获取当前的值，并自减
     * public final int getAndAdd(int delta) //获取当前的值，并加上预期的值
     * boolean compareAndSet(int expect, int update) //如果输入的数值等于预期值，
     * 则以原子方式将该值设置为输入值（update）
     * public final void lazySet(int newValue)//最终设置为newValue,
     * 使用 lazySet 设置之后可能导致其他线程在之后的一小段时间内还是可以读到旧的值。
     *
     *
     *
     * 部分源码
     *
     * private static final Unsafe unsafe = Unsafe.getUnsafe();
     * private static final long valueOffset;
     *
     * static {
     *     try {
     *         valueOffset = unsafe.objectFieldOffset
     *             (AtomicInteger.class.getDeclaredField("value"));
     *     } catch (Exception ex) { throw new Error(ex); }
     * }
     *
     * private volatile int value;
     *
     *
     * 2个关键字段说明：
     * value：使用volatile修饰，可以确保value在多线程中的可见性。
     * valueOffset：value属性在AtomicInteger中的偏移量，通过这个偏移量可以快速定位到value字段，
     * 这个是实现AtomicInteger的关键。
     *
     *
     *
     * getAndIncrement源码：
     *
     * public final int getAndIncrement() {
     *     return unsafe.getAndAddInt(this, valueOffset, 1);
     * }
     * 内部调用的是Unsafe类中的getAndAddInt方法，我们看一下getAndAddInt源码：
     *
     * public final int getAndAddInt(Object var1, long var2, int var4) {
     *     int var5;
     *     do {
     *         var5 = this.getIntVolatile(var1, var2);
     *     } while(!this.compareAndSwapInt(var1, var2, var5, var5 + var4));
     *
     *     return var5;
     * }
     *
     * 说明：
     * this.getIntVolatile：可以确保从主内存中获取变量最新的值。
     * compareAndSwapInt：CAS操作，CAS的原理是拿期望的值和原本的值作比较，如果相同则更新成新的值，
     * 可以确保在多线程情况下只有一个线程会操作成功，不成功的返回false。
     * 上面有个do-while循环，compareAndSwapInt返回false之后，会再次从主内存中获取变量的值，
     * 继续做CAS操作，直到成功为止。
     *
     * getAndAddInt操作相当于线程安全的count++操作，如同：
     * synchronize(lock){
     *    count++;
     * }
     *
     * count++操作实际上是被拆分为3步骤执行：
     * 1.获取count的值，记做A：A=count
     * 2.将A的值+1，得到B：B = A+1
     * 3.让B赋值给count：count = B
     * 多线程情况下会出现线程安全的问题，导致数据不准确。
     *
     * synchronize的方式会导致占时无法获取锁的线程处于阻塞状态，性能比较低。
     * CAS的性能比synchronize要快很多。
     *
     * -----------------------------------------------------------------------------
     *
     * 数组类型原子类介绍
     * 使用原子的方式更新数组里的某个元素，可以确保修改数组中数据的线程安全性。
     * 1.AtomicIntegerArray：整形数组原子操作类
     * 2.AtomicLongArray：长整形数组原子操作类
     * 3.AtomicReferenceArray ：引用类型数组原子操作类
     *
     * -----------------------------------------------------------------------------
     *
     * 引用类型原子类介绍
     * 基本类型原子类只能更新一个变量，如果需要原子更新多个变量，需要使用 引用类型原子类。
     * AtomicReference：引用类型原子类
     * AtomicStampedRerence：原子更新引用类型里的字段原子类
     * AtomicMarkableReference ：原子更新带有标记位的引用类型
     *
     * AtomicReference 和 AtomicInteger 非常类似，不同之处在于 AtomicInteger是对整数的封装，
     * 而AtomicReference则是对应普通的对象引用，它可以确保你在修改对象引用时的线程安全性。
     * 在介绍AtomicReference的同时，我们先来了解一个有关原子操作逻辑上的不足。
     *
     * ABA问题
     * 之前我们说过，线程判断被修改对象是否可以正确写入的条件是对象的当前值和期望值是否一致。
     * 这个逻辑从一般意义上来说是正确的，但是可能出现一个小小的例外，就是当你获得当前数据后，
     * 在准备修改为新值钱，对象的值被其他线程连续修改了两次，而经过这2次修改后，对象的值又恢复为旧值，
     * 这样，当前线程就无法正确判断这个对象究竟是否被修改过，这就是所谓的ABA问题，可能会引发一些问题。
     *
     * -----------------------------------------------------------------------------
     *
     * 使用AtomicStampedRerence解决ABA的问题
     * AtomicReference无法解决上述问题的根本原因是，对象在被修改过程中丢失了状态信息，
     * 比如充值20元的时候，需要同时标记一个状态，用来标注用户被充值过。
     * 因此我们只要能够记录对象在修改过程中的状态值，
     * 就可以很好地解决对象被反复修改导致线程无法正确判断对象状态的问题。
     *
     * AtomicStampedRerence正是这么做的，他内部不仅维护了对象的值，
     * 还维护了一个时间戳（我们这里把他称为时间戳，实际上它可以使用任何一个整形来表示状态值），
     * 当AtomicStampedRerence对应的数值被修改时，除了更新数据本身外，还必须要更新时间戳。
     * 当AtomicStampedRerence设置对象值时，对象值及时间戳都必须满足期望值，写入才会成功。
     * 因此，即使对象值被反复读写，写回原值，只要时间戳发生变量，就能防止不恰当的写入。
     *
     * AtomicStampedRerence的几个Api在AtomicReference的基础上新增了有关时间戳的信息。
     *
     * //比较设置，参数依次为：期望值、写入新值、期望时间戳、新时间戳
     * public boolean compareAndSet(V expectedReference, V newReference, int expectedStamp, int newStamp);
     * //获得当前对象引用
     * public V getReference();
     * //获得当前时间戳
     * public int getStamp();
     * //设置当前对象引用和时间戳
     * public void set(V newReference, int newStamp);
     *
     * -----------------------------------------------------------------------------
     *
     * 对象的属性修改原子类介绍
     * 如果需要原子更新某个类里的某个字段时，需要用到对象的属性修改原子类。
     * AtomicIntegerFieldUpdater：原子更新整形字段的值
     * AtomicLongFieldUpdater：原子更新长整形字段的值
     * AtomicReferenceFieldUpdater ：原子更新应用类型字段的值
     *
     * 要想原子地更新对象的属性需要两步：
     * 第一步，因为对象的属性修改类型原子类都是抽象类，所以每次使用都必须使用静态方法
     * newUpdater()创建一个更新器，并且需要设置想要更新的类和属性。
     * 第二步，更新的对象属性必须使用 public volatile 修饰符。
     *
     * 上面三个类提供的方法几乎相同，所以我们这里以AtomicReferenceFieldUpdater为例子来介绍。
     * 调用AtomicReferenceFieldUpdater静态方法newUpdater创建AtomicReferenceFieldUpdater对象
     *
     * public static <U, W> AtomicReferenceFieldUpdater<U, W> newUpdater(Class<U> tclass, Class<W> vclass, String fieldName)
     *
     * 说明:
     * 三个参数
     * tclass：需要操作的字段所在的类
     * vclass：操作字段的类型
     * fieldName：字段名称
     *
     * -----------------------------------------------------------------------------
     *
     *
     */

    static AtomicIntegerArray pageRequest = new AtomicIntegerArray(new int[10]);

    private static void request(int page) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(5);
        int pageIndex = page - 1;
        pageRequest.incrementAndGet(pageIndex);
    }

    /**
     *
     * 代码中将10个面的访问量放在了一个int类型的数组中，数组大小为10，
     * 然后通过AtomicIntegerArray来操作数组中的每个元素，可以确保操作数据的原子性，
     * 每次访问会调用incrementAndGet，此方法需要传入数组的下标，然后对指定的元素做原子+1操作。
     * 输出结果都是1000，可以看出对于数组中元素的并发修改是线程安全的。如果线程不安全，
     * 则部分数据可能会小于1000。
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
                    for(int page = 1; page <= 10; page++) {
                        for (int j = 0; j < 10; j++) {
                            request(page);
                        }
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
        for (int i = 0; i < 10; i++) {
            System.out.println(Thread.currentThread().getName() + ",i:"
                    + i + ", use time:" + (et-st) + ", count:" + pageRequest.get(i));
        }
    }

    static int accountMoney = 19;
    static AtomicReference<Integer> money = new AtomicReference<>(accountMoney);

    static void recharge() {
        for(int i = 0; i < 2; i++) {
            new Thread(()->{
                for (int j = 0; j < 5; j++) {
                    Integer m = money.get();
                    if(m == accountMoney) {
                        if(money.compareAndSet(m, m+20)) {
                            System.out.println("当前余额："
                                    +m+"，小于20，充值20成功，余额："+money.get());
                        }
                    }
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    static void consume() {
        for (int j = 0; j < 5; j++) {
            Integer m = money.get();
            if(m > 20) {
                if(money.compareAndSet(m, m-20)) {
                    System.out.println("当前余额："+m+"，成功消费，余额："+money.get());
                }
            }
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * 从输出中可以看到，这个账户被先后反复多次充值。其原因是账户余额被反复修改，
     * 修改后的值和原有的数值19一样，使得CAS操作无法正确判断当前数据是否被修改过（是否被加过20）。
     * 虽然这种情况出现的概率不大，但是依然是有可能出现的，因此，当业务上确实可能出现这种情况时，
     * 我们必须多加防范。JDK也为我们考虑到了这种情况，使用AtomicStampedReference可以很好地解决这个问题。
     *
     */
    private static void test1() {
        recharge();
        consume();
    }

    static AtomicStampedReference<Integer> moneyStamped = new AtomicStampedReference<>(accountMoney, 0);

    static void recharge1() {
        for(int i = 0; i < 2; i++) {
            int stamp = moneyStamped.getStamp();
            new Thread(()->{
                for (int j = 0; j < 5; j++) {
                    Integer m = moneyStamped.getReference();
                    if(m == accountMoney) {
                        if(moneyStamped.compareAndSet(m, m+20, stamp, stamp+1)) {
                            System.out.println("当前标记：" + moneyStamped.getStamp()
                                    + "，当前余额：" +m+"，小于20，充值20成功，余额："
                                    + moneyStamped.getReference());
                        }
                    }
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    static void consume1() {
        for (int j = 0; j < 5; j++) {
            int stamp = moneyStamped.getStamp();
            Integer m = moneyStamped.getReference();
            if(m > 20) {
                if(moneyStamped.compareAndSet(m, m-20, stamp, stamp+1)) {
                    System.out.println("当前标记：" + moneyStamped.getStamp()
                            + "，当前余额："+m+"，成功消费，余额："
                            + moneyStamped.getReference());
                }
            }
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * 关于这个时间戳的，在数据库修改数据中也有类似的用法，比如2个编辑同时编辑一篇文章，同时提交，
     * 只允许一个用户提交成功，提示另外一个用户：博客已被其他人修改，如何实现呢？
     *
     * 博客表：t_blog（id,content,stamp)，stamp默认值为0，每次更新+1
     *
     * A、B 二个编辑同时对一篇文章进行编辑，stamp都为0，当点击提交的时候，
     * 将stamp和id作为条件更新博客内容，执行的sql如下：
     * update t_blog set content = 更新的内容,
     * stamp = stamp+1 where id = 博客id and stamp = 0;
     *
     * 这条update会返回影响的行数，只有一个会返回1，表示更新成功，另外一个提交者返回0，
     * 表示需要修改的数据已经不满足条件了，被其他用户给修改了。这种修改数据的方式也叫乐观锁。
     *
     */
    private static void test2() {
        recharge1();
        consume1();
    }

    static AtomTest atomTest = new AtomTest();
    volatile Boolean isInit = Boolean.FALSE;
    AtomicReferenceFieldUpdater<AtomTest, Boolean> updater
            = AtomicReferenceFieldUpdater.newUpdater(AtomTest.class, Boolean.class, "isInit");

    public void init() {
        if(updater.compareAndSet(atomTest, Boolean.FALSE, Boolean.TRUE)) {
            System.out.println(System.currentTimeMillis() + ", name : "
                    + Thread.currentThread().getName() + " , 开始初始化了！！！");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(System.currentTimeMillis() + ", name : "
                    + Thread.currentThread().getName() + " , 初始化结束了！！！");
        } else {
            System.out.println(System.currentTimeMillis() + ", name : "
                    + Thread.currentThread().getName() + " , 已经有其他线程初始化了！！！");
        }
    }

    /**
     *
     * 多线程并发调用一个类的初始化方法，如果未被初始化过，将执行初始化工作，要求只能初始化一次
     * 说明：
     * isInit属性必须要volatille修饰，可以确保变量的可见性
     * 可以看出多线程同时执行init()方法，只有一个线程执行了初始化的操作，其他线程跳过了。
     * 多个线程同时到达updater.compareAndSet，只有一个会成功。
     *
     */
    private static void test3() {
        for (int i = 0; i < 5; i++) {
            new Thread(()->{
                atomTest.init();
            }).start();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 测试数组原子类
//        test();
        // 测试AtomicReference
//        test1();
        // 测试AtomicStampedReference
//        test2();
        // 对象的属性修改原子类
        test3();
    }



}
