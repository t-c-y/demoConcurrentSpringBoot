package com.example.demo.jvm.day1;

public class DeadlockTest {
    /**
     * 编写死锁例子
     * 下边的例子代码，启动后，程序不会输出结果，卡住了。
     *
     * #查看java进程pid
     * ps -ef | grep DeadlockTest
     * #查看死锁信息
     * jstack <pid>
     *
     * 然后通过jstack方式查看信息如下：两个线程互相锁住对方需要的资源，互相不释放，造成死锁。
     *
     *
     *
     * "thread2" #11 prio=5 os_prio=31 tid=0x00007f9c42909000 nid=0x3f03 waiting for monitor entry [0x00007000097be000]
     *    java.lang.Thread.State: BLOCKED (on object monitor)
     *         at com.example.demo.jvm.day1.DeadlockTest$SynAddRunalbe.run(DeadlockTest.java:79)
     *         - waiting to lock <0x00000007957b9ee0> (a com.example.demo.jvm.day1.DeadlockTest$Obj1)
     *         - locked <0x00000007957be378> (a com.example.demo.jvm.day1.DeadlockTest$Obj2)
     *         at java.lang.Thread.run(Thread.java:748)
     *
     * "thread1" #10 prio=5 os_prio=31 tid=0x00007f9c42908000 nid=0x3e03 waiting for monitor entry [0x00007000096bb000]
     *    java.lang.Thread.State: BLOCKED (on object monitor)
     *         at com.example.demo.jvm.day1.DeadlockTest$SynAddRunalbe.run(DeadlockTest.java:72)
     *         - waiting to lock <0x00000007957be378> (a com.example.demo.jvm.day1.DeadlockTest$Obj2)
     *         - locked <0x00000007957b9ee0> (a com.example.demo.jvm.day1.DeadlockTest$Obj1)
     *         at java.lang.Thread.run(Thread.java:748)
     *
     *
     *
     *
     */

    public static void main(String[] args) {
        Obj1 o1 = new Obj1();
        Obj2 o2 = new Obj2();
        int a=1, b=5;
        Thread t1 = new Thread(new SynAddRunalbe(a,b,o1,o2,true));
        t1.setName("thread1");
        Thread t2 = new Thread(new SynAddRunalbe(a,b,o1,o2,false));
        t2.setName("thread2");
        t1.start();
        t2.start();
    }

    public static class SynAddRunalbe implements Runnable {

        private int a;
        private int b;
        private Obj1 obj1;
        private Obj2 obj2;
        private boolean flg;

        public SynAddRunalbe (int a, int b, Obj1 obj1, Obj2 obj2, boolean flg) {
            this.a = a;
            this.b = b;
            this.obj1 = obj1;
            this.obj2 = obj2;
            this.flg = flg;
        }

        @Override
        public void run() {

            try {
                if (flg) {
                    synchronized (obj1) {
                        Thread.sleep(100);
                        synchronized (obj2) {
                            System.out.println("a=" + a + " b=" + b + " a+b="+a+b);
                        }
                    }
                } else {
                    synchronized (obj2) {
                        Thread.sleep(100);
                        synchronized (obj1) {
                            System.out.println("a=" + a + " b=" + b + " a+b="+a+b);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static class Obj1 {

    }

    public static class Obj2 {

    }

}