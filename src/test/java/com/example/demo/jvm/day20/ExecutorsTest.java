package com.example.demo.jvm.day20;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ExecutorsTest {
    /**
     *
     * CompletionService接口
     *
     * CompletionService相当于一个执行任务的服务，通过submit丢任务给这个服务，服务内部去执行任务，
     * 可以通过服务提供的一些方法获取服务中已经完成的任务。
     *
     * 接口内的几个方法：
     * Future<V> submit(Callable<V> task);
     * 用于向服务中提交有返回结果的任务，并返回Future对象
     * Future<V> submit(Runnable task, V result);
     * 用户向服务中提交有返回值的任务去执行，并返回Future对象
     * Future<V> take() throws InterruptedException;
     * 从服务中返回并移除一个已经完成的任务，如果获取不到，会一致阻塞到有返回值为止。此方法会响应线程中断。
     * Future<V> poll();
     * 从服务中返回并移除一个已经完成的任务，如果内部没有已经完成的任务，则返回空，此方法会立即响应。
     * Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException;
     * 尝试在指定的时间内从服务中返回并移除一个已经完成的任务，等待的时间超时还是没有获取到已完成的任务，
     * 则返回空。此方法会响应线程中断
     *
     * 通过submit向内部提交任意多个任务，通过take方法可以获取已经执行完成的任务，如果获取不到将等待。
     *
     * -------------------------------------------------------------------------------
     *
     * ExecutorCompletionService类
     *
     * ExecutorCompletionService类是CompletionService接口的具体实现。
     * 说一下其内部原理，ExecutorCompletionService创建的时候会传入一个线程池，
     * 调用submit方法传入需要执行的任务，任务由内部的线程池来处理；
     * ExecutorCompletionService内部有个阻塞队列，任意一个任务完成之后，
     * 会将任务的执行结果（Future类型）放入阻塞队列中，
     * 然后其他线程可以调用它take、poll方法从这个阻塞队列中获取一个已经完成的任务，
     * 获取任务返回结果的顺序和任务执行完成的先后顺序一致，所以最先完成的任务会先返回。
     *
     * 看一下构造方法：
     *
     * public ExecutorCompletionService(Executor executor) {
     *     if (executor == null)
     *         throw new NullPointerException();
     *     this.executor = executor;
     *     this.aes = (executor instanceof AbstractExecutorService) ?
     *         (AbstractExecutorService) executor : null;
     *     this.completionQueue = new LinkedBlockingQueue<Future<V>>();
     * }
     *
     * 构造方法需要传入一个Executor对象，这个对象表示任务执行器，所有传入的任务会被这个执行器执行。
     *
     * completionQueue是用来存储任务结果的阻塞队列，默认用采用的是LinkedBlockingQueue，
     * 也支持开发自己设置。通过submit传入需要执行的任务，任务执行完成之后，会放入completionQueue中，
     * 有兴趣的可以看一下原码，还是很好理解的。
     *
     * -------------------------------------------------------------------------------
     *
     *
     */

    public static class GoodModels {
        String name;
        long sTime;
        long eTime;

        public GoodModels(String name, long sTime, long eTime) {
            this.name = name;
            this.sTime = sTime;
            this.eTime = eTime;
        }

        @Override
        public String toString() {
            return name + ",[" + sTime + "," + eTime
                    + "],use time:{" + (eTime-sTime) + " ms }";
        }
    }

    static void moveUp(GoodModels good) throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);
        System.out.println("商品搬上楼 = [" + good + "]");
    }

    static Callable<GoodModels> buyGood(String name, int time) {
        return ()->{
            long sTime = System.currentTimeMillis();
            System.out.println(sTime + ",buy good : " + name);
            TimeUnit.SECONDS.sleep(time);
            long eTime = System.currentTimeMillis();
            System.out.println(eTime + ",good : " + name + ", arrive");
            return new GoodModels(name, sTime, eTime);
        };
    }

    /**
     * 从输出中可以看出和我们希望的结果一致，代码中下单顺序是：冰箱、洗衣机，冰箱送货耗时5秒，
     * 洗衣机送货耗时2秒，洗衣机先到的，然后被送上楼了，冰箱后到被送上楼，总共耗时12秒，
     * 和期望的方案一样。
     *
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private static void test() throws InterruptedException, ExecutionException {
        long st = System.currentTimeMillis();
        System.out.println(st + " start buy!");
        ExecutorService e = Executors.newFixedThreadPool(5);
        ExecutorCompletionService<GoodModels> ecs = new ExecutorCompletionService<>(e);
        //异步下单买冰箱
        ecs.submit(buyGood("冰箱", 5));
        //异步下单买洗衣机
        ecs.submit(buyGood("洗衣机", 2));
        e.shutdown();

        int goodNum = 2;
        for(int i=0; i<goodNum; i++) {
            GoodModels g = ecs.take().get();
            moveUp(g);
        }

        long et = System.currentTimeMillis();
        System.out.println(et + ",货物到家");
        System.out.println("总耗时：" + (et-st) + " ms");
    }

    private static <T> void solve(Executor e,
                                  Collection<Callable<T>> solvers,
                                  Consumer<T> use) throws InterruptedException, ExecutionException {
        CompletionService<T> esc = new ExecutorCompletionService<>(e);
        for(Callable<T> s : solvers) {
            esc.submit(s);
        }
        int n = solvers.size();
        for(int i=0; i<n; i++) {
            T r = esc.take().get();
            if(r != null) {
                use.accept(r);
            }
        }
    }

    /**
     * 代码中传入了一批任务进行处理，最终将所有处理完成的按任务完成的先后顺序传递给Consumer进行消费了。
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private static void test1() throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(5);
        List<Callable<Integer>> list = new ArrayList<>();
        int count = 5;
        for (int i = count; i > 0; i--) {
            int j = i*2;
            list.add(()->{
                TimeUnit.SECONDS.sleep(j);
                return j;
            });
        }
        solve(es, list, a->{
            System.out.println(System.currentTimeMillis() + ":" + a);
        });
        es.shutdown();
    }

    private static <T> T invoke(Executor e, Collection<Callable<T>> solves) throws InterruptedException, ExecutionException {
        ExecutorCompletionService<T> esc = new ExecutorCompletionService(e);
        List<Future<T>> futures = new ArrayList<>();
        for(Callable<T> s : solves) {
            futures.add(esc.submit(s));
        }
        int n = solves.size();
        try {
            for (int i = 0; i < n; i++) {
                T r = esc.take().get();
                if(r != null) {
                    return r;
                }
            }
        } finally {
            for(Future<T> f : futures) {
                f.cancel(true);
            }
        }
        return null;
    }

    /**
     * 代码中执行了5个任务，使用CompletionService执行任务，调用take方法获取最先执行完成的任务，
     * 然后返回。在finally中对所有任务发送取消操作（future.cancel(true);），
     * 从输出中可以看出只有任务1执行成功，其他任务被成功取消了，符合预期结果。
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private static void test2() throws ExecutionException, InterruptedException {
        long st = System.currentTimeMillis();
        ExecutorService es = Executors.newFixedThreadPool(5);
        List<Callable<Integer>> list = new ArrayList<>();
        int count = 5;
        for (int i = count; i > 0; i--) {
            int j = i*2;
            String taskName = "task"+i;
            list.add(()->{
                TimeUnit.SECONDS.sleep(j);
                System.out.println(taskName+" end!");
                return j;
            });
        }
        Integer r = invoke(es, list);
        System.out.println("use time:" + (System.currentTimeMillis() - st)
                + " ms, result:" + r);
        es.shutdown();
    }

    /**
     * 其实ExecutorService已经为我们提供了这样的方法(执行一批任务，先执行完的取消其他任务)，
     * 方法声明如下：
     *
     * <T> T invokeAny(Collection<? extends Callable<T>> tasks)
     *         throws InterruptedException, ExecutionException;
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private static void test3() throws ExecutionException, InterruptedException {
        long st = System.currentTimeMillis();
        ExecutorService es = Executors.newFixedThreadPool(5);
        List<Callable<Integer>> list = new ArrayList<>();
        int count = 5;
        for (int i = count; i > 0; i--) {
            int j = i*2;
            String taskName = "task "+i;
            list.add(()->{
                TimeUnit.SECONDS.sleep(j);
                System.out.println(taskName+" end!");
                return j;
            });
        }
        Integer r = es.invokeAny(list);
        System.out.println("use time:" + (System.currentTimeMillis() - st)
                + " ms, result:" + r);
        es.shutdown();
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 线程先结束的先执行
//        test();
        // 执行一批任务，然后消费执行结果
//        test1();
        // 执行一批任务，先执行完的取消其他任务
//        test2();
        test3();
    }

}
