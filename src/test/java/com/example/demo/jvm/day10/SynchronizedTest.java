package com.example.demo.jvm.day10;

public class SynchronizedTest {
    /**
     * 测试synchronized
     *
     * 什么是线程安全？
     * 当多个线程去访问同一个类（对象或方法）的时候，
     * 该类都能表现出正常的行为（与自己预想的结果一致），
     * 那我们就可以所这个类是线程安全的。
     *
     * 造成线程安全问题的主要诱因有两点：
     * 一是存在共享数据(也称临界资源)
     * 二是存在多条线程共同操作共享数据
     *
     * 关键字 synchronized 可以保证在同一个时刻，
     * 只有一个线程可以执行某个方法或者某个代码块(主要是对方法或者代码块中存在共享数据的操作)，
     * 同时我们还应该注意到synchronized另外一个重要的作用，
     * synchronized可保证一个线程的变化(主要是共享数据的变化)
     * 被其他线程所看到（保证可见性，完全可以替代volatile功能）
     *
     * synchronized主要有3种使用方式
     * 1.修饰实例方法，作用于当前实例，进入同步代码前需要先获取实例的锁
     * 2.修饰静态方法，作用于类的Class对象，进入修饰的静态方法前需要先获取类的Class对象的锁
     * 3.修饰代码块，需要指定加锁对象(记做lockobj)，在进入同步代码块前需要先获取lockobj的锁
     *
     * 分析代码是否互斥的方法，先找出synchronized作用的对象是谁，
     * 如果多个线程操作的方法中synchronized作用的锁对象一样，
     * 那么这些线程同时异步执行这些方法就是互斥的
     *
     */

    public static void main(String[] args) throws InterruptedException {
        //线程不安全
//        test1();
        //线程安全-作用于静态方法（作用于class对象）
//        test2();
        //线程安全-作用于class对象
//        test3();
        //线程安全-作用于实例对象
//        test4();
        //线程安全-作用于同步代码块
        test5();
    }

    /**
     * 在某些情况下，我们编写的方法体可能比较大，同时存在一些比较耗时的操作，
     * 而需要同步的代码又只有一小部分，如果直接对整个方法进行同步操作，
     * 可能会得不偿失，此时我们可以使用同步代码块的方式对需要同步的代码进行包裹，
     * 这样就无需对整个方法进行同步操作了
     *
     * 从代码看出，将synchronized作用于一个给定的实例对象instance，
     * 即当前实例对象就是锁对象，
     * 每次当线程进入synchronized包裹的代码块时就会要求当前线程持有instance实例对象锁，
     * 如果当前有其他线程正持有该对象锁，那么新到的线程就必须等待，
     * 这样也就保证了每次只有一个线程执行i++;操作。
     * 当然除了instance作为对象外，我们还可以使用this对象(代表当前实例)或者当前类的class对象作为锁
     *
     * @throws InterruptedException
     */
    public static void test5() throws InterruptedException {
        Thread t1 = new Thread(r);
        Thread t2 = new Thread(r);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("num="+num);
    }

    static R2 r = new R2();

    /**
     * synchronized同步代码块
     */
    public static class R2 implements Runnable {

        @Override
        public void run() {
            synchronized (r) {
                for(int i=0; i<10000; i++) {
                    num++;
                }
            }
        }
    }

    /**
     * 所谓实例对象锁就是用synchronized修饰实例对象的实例方法，注意是实例方法，不是静态方法
     *
     * 实例方法上加synchronized，线程安全的前提是，多个线程操作的是同一个实例，
     * 如果多个线程作用于不同的实例，那么线程安全是无法保证的
     *
     * 同一个实例的多个实例方法上有synchronized，这些方法都是互斥的，
     * 同一时间只允许一个线程操作同一个实例的其中的一个synchronized方法
     *
     * @throws InterruptedException
     */
    public static void test4() throws InterruptedException {
        SynchronizedTest st = new SynchronizedTest();
        Thread t1 = new Thread(new R1(st), "t1");
        Thread t2 = new Thread(new R1(st), "t2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(" num= " + st.num1);
    }

    public int num1 = 0;

    /**
     * synchronized作用于实例对象
     */
    public synchronized void add() {
        num1++;
    }

    public static class R1 implements Runnable {

        SynchronizedTest st;

        public R1(SynchronizedTest st) {
            this.st = st;
        }

        @Override
        public void run() {
            for(int i=0;i<10000;i++) {
                st.add();
            }
        }
    }

    /**
     * 测试线程安全
     * @throws InterruptedException
     */
    public static void test3() throws InterruptedException {
        Thread t1 = new T3();
        Thread t2 = new T3();
        Thread t3 = new T3();
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
        System.out.println("num = [" + num + "]");
    }

    /**
     * 测试线程安全
     * @throws InterruptedException
     */
    public static void test2() throws InterruptedException {
        Thread t1 = new T2();
        Thread t2 = new T2();
        Thread t3 = new T2();
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
        System.out.println("num = [" + num + "]");
    }

    /**
     * 测试线程不安全
     * @throws InterruptedException
     */
    public static void test1() throws InterruptedException {
        Thread t1 = new T1();
        Thread t2 = new T1();
        Thread t3 = new T1();
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
        System.out.println("num = [" + num + "]");
    }

    public static int num = 0;

    /**
     * 线程不安全
     */
    public static void m1() {
        for(int i=0; i<10000; i++) {
            num++;
        }
    }

    /**
     * 线程安全
     * synchronized作用于当前类Class对象
     */
    public static synchronized void m2() {
        for(int i=0; i<10000; i++) {
            num++;
        }
    }

    public static void m3() {
        synchronized (SynchronizedTest.class) {
            for(int i=0;i<10000;i++) {
                num++;
            }
        }
    }

    public static class T1 extends Thread {
        @Override
        public void run() {
            SynchronizedTest.m1();
        }
    }

    public static class T2 extends Thread {
        @Override
        public void run() {
            SynchronizedTest.m2();
        }
    }

    public static class T3 extends Thread {
        @Override
        public void run() {
            SynchronizedTest.m3();
        }
    }
}
