package com.example.demo.jvm.day6;


public class JoinYieldTest {
    /**
     * 线程结束和线程谦让
     *
     * public final void join() throws InterruptedException;
     * public final synchronized void join(long millis) throws InterruptedException;
     *
     * 第1个方法表示无限等待，它会一直只是当前线程。知道目标线程执行完毕。
     * 第2个方法有个参数，用于指定等待时间，如果超过了给定的时间目标线程还在执行，当前线程也会停止等待，而继续往下执行。
     * 比如：线程T1需要等待T2、T3完成之后才能继续执行，那么在T1线程中需要分别调用T2和T3的join()方法。
     *
     * public static native void yield();
     *
     * yield是谦让的意思，这是一个静态方法，一旦执行，它会让当前线程出让CPU，
     * 但需要注意的是，出让CPU并不是说不让当前线程执行了，当前线程在出让CPU后，
     * 还会进行CPU资源的争夺，但是能否再抢到CPU的执行权就不一定了。
     * 因此，对Thread.yield()方法的调用好像就是在说：我已经完成了一些主要的工作，
     * 我可以休息一下了，可以让CPU给其他线程一些工作机会了。
     *
     * 如果觉得一个线程不太重要，或者优先级比较低，而又担心此线程会过多的占用CPU资源，
     * 那么可以在适当的时候调用一下Thread.yield()方法，给与其他线程更多的机会。
     *
     */

    static int num = 0;

    public static class T1 extends Thread {
        public T1(String name) {
            super(name);
        }

        @Override
        public void run() {
            System.out.println(System.currentTimeMillis() + " ,start " + this.getName());
            for(int i=0; i<10; i++) {
                num++;
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(System.currentTimeMillis() + " ,end " + this.getName());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new T1("t1");
        t1.start();
        // 主线程等待t1完成之后才继续执行的。
        t1.join();
        System.out.println(System.currentTimeMillis() + " ,num = " + num);
    }
}
