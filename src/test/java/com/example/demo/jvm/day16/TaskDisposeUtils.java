package com.example.demo.jvm.day16;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TaskDisposeUtils {
    /**
     * 手写一个并行处理任务的工具类
     */

    //并行线程数
    public static final int POOL_SIZE;

    static {
        POOL_SIZE = Math.max(Runtime.getRuntime().availableProcessors(), 5);
    }

    public static <T> void dispose(List<T> taskList, Consumer<T> consumer) throws InterruptedException {
        dispose(true, POOL_SIZE, taskList, consumer);
    }

    public static <T> void dispose(boolean moreThread,
                                   int poolSize,
                                   List<T> taskList,
                                   Consumer<T> consume) throws InterruptedException {
        if(moreThread && poolSize > 1) {
            poolSize = Math.min(poolSize, taskList.size());
            ExecutorService executorService = null;
            try {
                executorService = Executors.newFixedThreadPool(poolSize);
                CountDownLatch countDown = new CountDownLatch(taskList.size());
                for (T item : taskList) {
                    executorService.execute(() -> {
                        try {
                            consume.accept(item);
                        } finally {
                            countDown.countDown();
                        }
                    });
                }
                countDown.await();
            } finally {
                if(executorService != null) {
                    executorService.shutdown();
                }
            }
        } else {
            for(T item : taskList) {
                consume.accept(item);
            }
        }
    }

    /**
     * TaskDisposeUtils是一个并行处理的工具类，可以传入n个任务内部使用线程池进行处理，
     * 等待所有任务都处理完成之后，方法才会返回。
     * 比如我们发送短信，系统中有1万条短信，我们使用上面的工具，
     * 每次取100条并行发送，待100个都处理完毕之后，再取一批按照同样的逻辑发送。
     *
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        //生成1-10的10个数字
        List<Integer> list = Stream.iterate(1, a -> a+1)
                .limit(10).collect(Collectors.toList());
        //启动多线程
        TaskDisposeUtils.dispose(list, item -> {
            try {
                long sTime = System.currentTimeMillis();
                TimeUnit.SECONDS.sleep(item);
                long eTime = System.currentTimeMillis();
                System.out.println(eTime + " ,task " + item + " end, useTime(ms):" + (eTime-sTime));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        System.out.println(list + " all tasks end");
    }

}
