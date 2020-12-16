package com.example.demo.jvm.day16;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CountDownTwoTest {
    /**
     * 2个CountDown结合使用的示例
     * 
     * 代码中，t1、t2、t3启动之后，都阻塞在 commanderCd.await();，
     * 主线程模拟发枪准备操作耗时5秒，然后调用 commanderCd.countDown();
     * 模拟发枪操作，此方法被调用以后，阻塞在 commanderCd.await();的3个线程会向下执行。
     * 主线程调用 countDownLatch.await();之后进行等待，每个人跑完之后，
     * 调用 countDown.countDown();通知一下 countDownLatch让计数器减1，
     * 最后3个人都跑完了，主线程从 countDownLatch.await();返回继续向下执行。
     */

    public static class T extends Thread {
        int runTime;
        CountDownLatch startSign;
        CountDownLatch countDown;

        public T(String name, int runTime,
                 CountDownLatch startSign, CountDownLatch countDown) {
            super(name);
            this.runTime = runTime;
            this.startSign = startSign;
            this.countDown = countDown;
        }

        @Override
        public void run() {
            long sTime = 0;
            try {
                //等待开枪信号
                startSign.await();
                sTime = System.currentTimeMillis();
                System.out.println(sTime + " , " + this.getName() + " , 开始跑");
                TimeUnit.SECONDS.sleep(this.runTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                countDown.countDown();
            }
            long eTime = System.currentTimeMillis();
            System.out.println(sTime + " , " + this.getName() + " , 跑步结束 , 耗时（ms）：" + (eTime-sTime));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        long sTime = System.currentTimeMillis();
        System.out.println(sTime + "," + Thread.currentThread().getName() + " 开始");
        CountDownLatch startSign = new CountDownLatch(1);
        CountDownLatch countDown = new CountDownLatch(3);
        Thread t1 = new T("张三", 2, startSign, countDown);
        Thread t2 = new T("李四", 3, startSign, countDown);
        Thread t3 = new T("王五", 5, startSign, countDown);
        t1.start();
        t2.start();
        t3.start();
        TimeUnit.SECONDS.sleep(3);
        System.out.println(System.currentTimeMillis() + " 各就各位准备，开枪！！！");
        startSign.countDown();
        countDown.await();
        long eTime = System.currentTimeMillis();
        System.out.println(eTime + " , 所有人跑步结束, "
                + Thread.currentThread().getName() + " 线程结束,耗时（ms）："
                + (eTime-sTime));
    }


}
