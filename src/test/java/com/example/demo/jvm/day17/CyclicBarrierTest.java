package com.example.demo.jvm.day17;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CyclicBarrierTest {
    /**
     * 测试CyclicBarrier
     * CyclicBarrier通常称为循环屏障。它和CountDownLatch很相似，
     * 都可以使线程先等待然后再执行。不过CountDownLatch是使一批线程等待
     * 另一批线程执行完后再执行；而CyclicBarrier只是使等待的线程达到一定数目
     * 后再让它们继续执行
     *
     * 每调用一次await()方法都将使阻塞的线程数+1，只有阻塞的线程数达到设定值时屏障才会打开，
     * 允许阻塞的所有线程继续执行。除此之外，CyclicBarrier还有几点需要注意的地方:
     * 1.CyclicBarrier的计数器可以重置而CountDownLatch不行，
     * 这意味着CyclicBarrier实例可以被重复使用而CountDownLatch只能被使用一次。
     * 而这也是循环屏障循环二字的语义所在。
     * 2.CyclicBarrier允许用户自定义barrierAction操作，这是个可选操作，
     * 可以在创建CyclicBarrier对象时指定；一旦用户在创建CyclicBarrier对象时
     * 设置了barrierAction参数，则在阻塞线程数达到设定值屏障打开前，
     * 会调用barrierAction的run()方法完成用户自定义的操作。
     *
     * CountDownLatch示例
     * 主管相当于 CountDownLatch，干活的小弟相当于做事情的线程。
     * 老板交给主管了一个任务，让主管搞完之后立即上报给老板。主管下面有10个小弟，
     * 接到任务之后将任务划分为10个小任务分给每个小弟去干，主管一直处于等待状态
     * （主管会调用await()方法，此方法会阻塞当前线程），让每个小弟干完之后通知一下主管
     * （调用countDown()方法通知主管，此方法会立即返回），主管等到所有的小弟都做完了，
     * 会被唤醒，从await()方法上苏醒，然后将结果反馈给老板。期间主管会等待，
     * 会等待所有小弟将结果汇报给自己。
     *
     * 而CyclicBarrier是一批线程让自己等待，等待所有的线程都准备好了，自己才能继续。
     *
     */

    public static CyclicBarrier cyclicBarrier = new CyclicBarrier(10);

    public static class T extends Thread {

        int sleep;

        public T(String name, int sleep) {
            super(name);
            this.sleep = sleep;
        }

        @Override
        public void run() {
            long sTime = 0, eTime = 0;
            try {
                TimeUnit.SECONDS.sleep(sleep);
                sTime = System.currentTimeMillis();
                System.out.println(this.getName() + " 到了");
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            eTime = System.currentTimeMillis();
            System.out.println(this.getName() + ",sleep:" + sleep
                    + " 等待了" + (eTime - sTime) +" ms, 开始吃饭了");
        }
    }

    /**
     * 代码中模拟了10个员工上桌吃饭的场景，等待所有员工都到齐了才能开发，
     * 可以看到第10个员工最慢，前面的都在等待第10个员工，员工1等待了9秒，
     * 上面代码中调用cyclicBarrier.await();会让当前线程等待。
     * 当10个员工都调用了cyclicBarrier.await();之后，
     * 所有处于等待中的员工都会被唤醒，然后继续运行。
     */
    private static void test1() {
        for(int i=1; i<11; i++) {
            new T("员工" + i, i).start();
        }
    }

    public static class T1 extends Thread {

        int sleep;

        public T1(String name, int sleep) {
            super(name);
            this.sleep = sleep;
        }

        private void eat() {
            try {
                TimeUnit.SECONDS.sleep(sleep);
                long sTime = System.currentTimeMillis();
                cyclicBarrier.await();
                long eTime = System.currentTimeMillis();
                System.out.println(this.getName() + ",sleep:" + sleep
                        + " 等待了" + (eTime - sTime) +" ms, 开始吃饭了");
                //模拟吃饭耗时
                TimeUnit.SECONDS.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }

        private void drive() {
            try {
                long sTime = System.currentTimeMillis();
                cyclicBarrier.await();
                long eTime = System.currentTimeMillis();
                System.out.println(this.getName() + ",sleep:" + sleep
                        + " 等待了" + (eTime - sTime) +" ms, 去下一个景点");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            this.eat();
            this.drive();
        }
    }

    /**
     * 代码中CyclicBarrier相当于使用了2次，第一次用于等待所有人到达后开饭，
     * 第二次用于等待所有人上车后驱车去下一景点。注意一些先到的员工会在其他人到达之前，
     * 都处于等待状态（cyclicBarrier.await();会让当前线程阻塞），无法干其他事情，
     * 等到最后一个人到了会唤醒所有人，然后继续。
     *
     * CyclicBarrier内部相当于有个计数器（构造方法传入的），每次调用await();后，
     * 计数器会减1，并且await()方法会让当前线程阻塞，等待计数器减为0的时候，
     * 所有在await()上等待的线程被唤醒，然后继续向下执行，
     * 此时计数器又会被还原为创建时的值，然后可以继续再次使用。
     *
     */
    private static void test2() {
        for(int i=1; i<11; i++) {
            new T1("同事"+i, i).start();
        }
    }

    public static CyclicBarrier cyclicBarrier1 = new CyclicBarrier(10, ()->{
        //模拟倒酒
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + " 说不好意思，大家久等了");
    });

    public static class T2 extends Thread {

        int sleep;

        public T2(String name, int sleep) {
            super(name);
            this.sleep = sleep;
        }

        @Override
        public void run() {
            try {
                TimeUnit.SECONDS.sleep(sleep);
                long sTime = System.currentTimeMillis()+1;
                cyclicBarrier1.await();
                long eTime = System.currentTimeMillis()+1;
                System.out.println(this.getName() + ",sleep:" + sleep
                        + " 等待了" + (eTime - sTime) +" ms, 开始吃饭了");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 代码中创建CyclicBarrier对象时，多传入了一个参数（内部是倒酒操作），
     * 先到的人先等待，待所有人都到齐之后，需要先给大家倒酒，
     * 然后唤醒所有等待中的人让大家开饭。从输出结果中我们发现，
     * 倒酒操作是由最后一个人操作的，最后一个人倒酒完毕之后，
     * 才唤醒所有等待中的其他员工，让大家开饭。
     */
    private static void test3() {
        for(int i=1; i<11; i++) {
            new T2("同事"+i, i).start();
        }
    }

    /**
     * 输出的信息看着有点乱，给大家理一理，员工5遇到急事，拿起筷子就是吃，这样好么，
     * 当然不好，他这么做了，后面看他这么做了都跟着这么做（这种场景是不是很熟悉，
     * 有一个人拿起筷子先吃起来，其他人都跟着上了），直接不等其他人了，拿起筷子就开吃了。
     * CyclicBarrier遇到这种情况就是这么处理的。前面4个员工都在await()处等待着，
     * 员工5也在await()上等待着，等了1秒（TimeUnit.SECONDS.sleep(1);），
     * 接了个电话，然后给员工5发送中断信号后（t.interrupt();），
     * 员工5的await()方法会触发InterruptedException异常，此时其他等待中的前4个员工，
     * 看着5开吃了，自己立即也不等了，内部从await()方法中触发BrokenBarrierException异常，
     * 然后也开吃了，后面的6/7/8/9/10员工来了以后发现大家都开吃了，自己也不等了，
     * 6-10员工调用await()直接抛出了BrokenBarrierException异常，然后继续向下。
     *
     * 结论：
     * 1.内部有一个人把规则破坏了（接收到中断信号），其他人都不按规则来了，不会等待了
     * 2.接收到中断信号的线程，await方法会触发InterruptedException异常，
     * 然后被唤醒向下运行
     * 3.其他等待中 或者后面到达的线程，会在await()方法上
     * 触发`BrokenBarrierException`异常，然后继续执行
     *
     * @throws InterruptedException
     */
    private static void test4() throws InterruptedException {
        for(int i=1; i<11; i++) {
            Thread t = new T("同事"+i, i);
            t.start();
            if(i==5) {
                TimeUnit.SECONDS.sleep(5);
                System.out.println(t.getName()+" 说我有点急事，先干饭啦");
                t.interrupt();
                TimeUnit.SECONDS.sleep(2);
            }
        }
    }

    public static class T3 extends Thread {

        int sleep;

        public T3(String name, int sleep) {
            super(name);
            this.sleep = sleep;
        }

        @Override
        public void run() {
            long sTime = 0, eTime = 0;
            try {
                TimeUnit.SECONDS.sleep(sleep);
                sTime = System.currentTimeMillis();
                System.out.println(this.getName() + " 到了");
                if("同事1".equals(this.getName())) {
                    cyclicBarrier.await(5, TimeUnit.SECONDS);
                } else {
                    cyclicBarrier.await();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            eTime = System.currentTimeMillis();
            System.out.println(this.getName() + ",sleep:" + sleep
                    + " 等待了" + (eTime - sTime) +" ms, 开始吃饭了");
        }
    }

    /**
     * 从输出结果中我们可以看到：1等待5秒之后，开吃了，其他等待人都开吃了，
     * 后面来的人不等待，直接开吃了。
     * 员工1调用有参await方法等待5秒之后，触发了TimeoutException异常，
     * 然后继续向下运行，其他的在5开吃之前已经等了一会的的几个员工，他们看到5开吃了，
     * 自己立即不等待了，也开吃了（他们的await抛出了BrokenBarrierException异常）；
     * 还有几个员工在5开吃之后到达的，他们直接不等待了，
     * 直接抛出BrokenBarrierException异常，然后也开吃了。
     *
     * 结论：
     * 1.等待超时的方法
     * public int await(long timeout, TimeUnit unit) throws InterruptedException,BrokenBarrierException,TimeoutException
     * 2.内部有一个人把规则破坏了（等待超时），其他人都不按规则来了，不会等待了
     * 3.等待超时的线程，await方法会触发TimeoutException异常，然后被唤醒向下运行
     * 4.其他等待中或者后面到达的线程，会在await()方法上触发
     * `BrokenBarrierException`异常，然后继续执行
     *
     */
    private static void test5() {
        for(int i=1; i<11; i++) {
            new T3("同事"+i, i).start();
        }
    }

    //是否重建规则
    public static boolean guizhe = false;

    public static class T4 extends Thread {

        int sleep;

        public T4(String name, int sleep) {
            super(name);
            this.sleep = sleep;
        }

        @Override
        public void run() {
            long sTime = 0, eTime = 0;
            try {
                TimeUnit.SECONDS.sleep(sleep);
                sTime = System.currentTimeMillis();
                System.out.println(this.getName() + " 到了");
                if(!guizhe) {
                    if("同事1".equals(this.getName())) {
                        cyclicBarrier.await(5, TimeUnit.SECONDS);
                    } else {
                        cyclicBarrier.await();
                    }
                } else {
                    cyclicBarrier.await();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            eTime = System.currentTimeMillis();
            System.out.println(this.getName() + ",sleep:" + sleep
                    + " 等待了" + (eTime - sTime) +" ms, 开始吃饭了");
        }
    }

    /**
     * 第一次规则被打乱了，过了一会导游重建了规则（cyclicBarrier.reset();），
     * 接着又重来来了一次模拟等待吃饭的操作，正常了。
     * @throws InterruptedException
     */
    private static void test6() throws InterruptedException {
        for(int i=1; i<11; i++) {
            new T4("同事" + i, i).start();
        }

        TimeUnit.SECONDS.sleep(15);
        cyclicBarrier.reset();
        System.out.println("----------你们太皮了，必须按照规则来------------");
        guizhe = true;
        for(int i=1; i<11; i++) {
            new T4("同事" + i, i).start();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        //简单测试CyclicBarrier
//        test1();
        //测试CyclicBarrier重复使用
//        test2();
        //测试CyclicBarrier.await内部操作
//        test3();
        //测试CyclicBarrier.await时，中断
//        test4();
        //测试CyclicBarrier.await 超时
//        test5();
        //测试重建规则
        test6();
    }
}
