package com.example.demo.jvm.day30;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

public class CompletableFutureTest {
    /**
     *
     * CompletableFuture是java8中新增的一个类，算是对Future的一种增强，用起来很方便，
     * 也是会经常用到的一个工具类，熟悉一下。
     *
     * CompletionStage接口
     * CompletionStage代表异步计算过程中的某一个阶段，一个阶段完成以后可能会触发另外一个阶段
     *
     * 一个阶段的计算执行可以是一个Function，Consumer或者Runnable。
     * 比如：stage.thenApply(x -> square(x)).thenAccept(x
     *         -> System.out.print(x)).thenRun(() -> System.out.println())
     *
     * 一个阶段的执行可能是被单个阶段的完成触发，也可能是由多个阶段一起触发
     *
     * CompletableFuture类
     * 在Java8中，CompletableFuture提供了非常强大的Future的扩展功能，
     * 可以帮助我们简化异步编程的复杂性，并且提供了函数式编程的能力，可以通过回调的方式处理计算结果，
     * 也提供了转换和组合 CompletableFuture 的方法。
     *
     * 它可能代表一个明确完成的Future，也有可能代表一个完成阶段（ CompletionStage ），
     * 它支持在计算完成以后触发一些函数或执行某些动作。
     *
     * 它实现了Future和CompletionStage接口
     *
     */


    /**
     *
     * runAsync 和 supplyAsync方法
     * CompletableFuture 提供了四个静态方法来创建一个异步操作。
     *
     * public static CompletableFuture<Void> runAsync(Runnable runnable)
     * public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor)
     * public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)
     * public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)
     *
     * 没有指定Executor的方法会使用ForkJoinPool.commonPool() 作为它的线程池执行异步代码。
     * 如果指定线程池，则使用指定的线程池运行。以下所有的方法都类同。
     *
     * runAsync方法不支持返回值。
     * supplyAsync可以支持返回值。
     *
     */
    private static void runAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = CompletableFuture.runAsync(()->{
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("run end ...");
        });
        future.get();
    }

    private static void supplyAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(()->{
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("run end ...");
            return System.currentTimeMillis();
        });

        long time = future.get();
        System.out.println("time="+time);
    }

    /**
     *
     * 计算结果完成时的回调方法
     * 当CompletableFuture的计算结果完成，或者抛出异常的时候，可以执行特定的Action。
     * 主要是下面的方法：
     *
     * public CompletableFuture<T> whenComplete(BiConsumer<? super T,? super Throwable> action)
     * public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T,? super Throwable> action)
     * public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T,? super Throwable> action, Executor executor)
     * public CompletableFuture<T> exceptionally(Function<Throwable,? extends T> fn)
     *
     * 可以看到Action的类型是BiConsumer它可以处理正常的计算结果，或者异常情况。
     *
     * whenComplete 和 whenCompleteAsync 的区别：
     * whenComplete：是执行当前任务的线程执行继续执行 whenComplete 的任务。
     * whenCompleteAsync：是执行把 whenCompleteAsync 这个任务继续提交给线程池来进行执行。
     *
     */
    private static void whenComplete() throws InterruptedException {
        CompletableFuture<Void> future = CompletableFuture.runAsync(()->{
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(new Random().nextInt() % 2 == 0) {
                int i = 12/0;
            }
            System.out.println("run end ...");
        });
//        future.whenCompleteAsync(new BiConsumer<Void, Throwable>() {
//            @Override
//            public void accept(Void aVoid, Throwable throwable) {
//                System.out.println("aVoid = [" + aVoid + "], throwable = [" + throwable + "]");
//                System.out.println("执行完毕！！！");
//            }
//        });
        future.whenComplete(new BiConsumer<Void, Throwable>() {
            @Override
            public void accept(Void aVoid, Throwable throwable) {
                System.out.println("aVoid = [" + aVoid + "], throwable = [" + throwable + "]");
                System.out.println("执行完毕！！！");
            }
        });
        future.exceptionally(new Function<Throwable, Void>() {
            @Override
            public Void apply(Throwable throwable) {
                System.out.println("执行失败 = [" + throwable.getMessage() + "]");
                return null;
            }
        });
        TimeUnit.SECONDS.sleep(3);
    }

    /**
     * thenApply 方法
     * 当一个线程依赖另一个线程时，可以使用 thenApply 方法来把这两个线程串行化。
     *
     * public <U> CompletableFuture<U> thenApply(Function<? super T,? extends U> fn)
     * public <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn)
     * public <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn, Executor executor)
     *
     * Function
     * T：上一个任务返回结果的类型
     * U：当前任务的返回值类型
     *
     */
    private static void thenApply() throws ExecutionException, InterruptedException {
//        CompletableFuture<Long> future = CompletableFuture.supplyAsync(()->{
//            long result = new Random().nextInt(100);
//            System.out.println("r1="+result);
//            return result;
//        })
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(new Supplier<Long>() {
            @Override
            public Long get() {
                long result = new Random().nextInt(100);
                System.out.println("r1="+result);
                return result;
            }
        })
        .thenApply(new Function<Long, Long>() {
            @Override
            public Long apply(Long aLong) {
                System.out.println("aLong = [" + aLong + "]");
                long r = aLong*2;
                System.out.println("r2="+r);
                return r;
            }
        });
        long result = future.get();
        System.out.println("result="+result);
    }

    /**
     *
     * handle 方法
     *
     * handle 是执行任务完成时对结果的处理。
     * handle 方法和 thenApply 方法处理方式基本一样。不同的是 handle 是在任务完成后再执行，
     * 还可以处理异常的任务。thenApply 只可以执行正常的任务，任务出现异常则不执行 thenApply 方法。
     *
     * public <U> CompletionStage<U> handle(BiFunction<? super T, Throwable, ? extends U> fn);
     * public <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn);
     * public <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn,Executor executor);
     *
     * 从示例中可以看出，在 handle 中可以根据任务是否有异常来进行做相应的后续处理操作。
     * 而 thenApply 方法，如果上个任务出现错误，则不会执行 thenApply 方法。
     *
     */
    private static void handle() throws ExecutionException, InterruptedException {
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(new Supplier<Long>() {
            @Override
            public Long get() {
                long result = new Random().nextInt(100);
                if(result%2 == 0) {
                    int i = 10/0;
                }
                System.out.println("result=" + result);
                return result;
            }
        })
        .handle(new BiFunction<Long, Throwable, Long>() {
            @Override
            public Long apply(Long aLong, Throwable throwable) {
                long result = -1;
                if(throwable == null) {
                    result = aLong * 2;
                } else {
                    System.out.println("aLong = [" + aLong + "], throwable = [" + throwable + "]");
                    System.out.println("throwable = [" + throwable.getMessage() + "]");
                }
                return result;
            }
        });

        long result = future.get();
        System.out.println("final result=" + result);
    }

    /**
     *
     * thenAccept 消费处理结果
     * 接收任务的处理结果，并消费处理，无返回结果。
     * public CompletionStage<Void> thenAccept(Consumer<? super T> action);
     * public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action);
     * public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action,Executor executor);
     *
     * 从示例代码中可以看出，该方法只是消费执行完成的任务，
     * 并可以根据上面的任务返回的结果进行处理。并没有后续的输错操作。
     *
     */
    private static void thenAccept() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                return new Random().nextInt(10);
            }
        }).thenAccept(integer -> {
            System.out.println("integer="+integer);
        });
        future.get();
    }

    /**
     * thenRun 方法
     * 跟 thenAccept 方法不一样的是，不关心任务的处理结果。只要上面的任务执行完成，
     * 就开始执行 thenAccept 。
     *
     * public CompletionStage<Void> thenRun(Runnable action);
     * public CompletionStage<Void> thenRunAsync(Runnable action);
     * public CompletionStage<Void> thenRunAsync(Runnable action,Executor executor);
     *
     * 该方法同 thenAccept 方法类似。不同的是上个任务处理完成后，
     * 并不会把计算的结果传给 thenRun 方法。只是处理玩任务后，执行 thenAccept 的后续操作。
     *
     */
    private static void thenRun() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                return new Random().nextInt(10);
            }
        }).thenRun(()->{
            System.out.println("run end ...");
        });
        future.get();
    }

    /**
     *
     * thenCombine  合并任务
     * thenCombine 会把 两个 CompletionStage 的任务都执行完成后，
     * 把两个任务的结果一块交给 thenCombine 来处理。
     *
     * public <U,V> CompletionStage<V> thenCombine(CompletionStage<? extends U> other,BiFunction<? super T,? super U,? extends V> fn);
     * public <U,V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other,BiFunction<? super T,? super U,? extends V> fn);
     * public <U,V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other,BiFunction<? super T,? super U,? extends V> fn,Executor executor);
     *
     *
     *
     */
    private static void thenCombine() throws ExecutionException, InterruptedException {
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                return "hello1";
            }
        });
        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                return "hello2";
            }
        });
        CompletableFuture<String> result = f1.thenCombine(f2, new BiFunction<String, String, String>() {
            @Override
            public String apply(String s, String s2) {
                return s + " - " + s2;
            }
        });

        System.out.println("result = " + result.get());
    }

    /**
     *
     * thenAcceptBoth
     * 当两个CompletionStage都执行完成后，把结果一块交给thenAcceptBoth来进行消耗
     *
     * public <U> CompletionStage<Void> thenAcceptBoth(CompletionStage<? extends U> other,BiConsumer<? super T, ? super U> action);
     * public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,BiConsumer<? super T, ? super U> action);
     * public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,BiConsumer<? super T, ? super U> action,     Executor executor);
     *
     */
    private static void thenAcceptBoth() throws InterruptedException {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                int t = new Random().nextInt(3);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("t1="+t);
                return t;
            }
        });
        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(new Supplier<Integer> (){
            @Override
            public Integer get() {
                int t = new Random().nextInt(3);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("t2="+t);
                return t;
            }
        });
        f1.thenAcceptBoth(f2, new BiConsumer<Integer, Integer>() {
            @Override
            public void accept(Integer integer, Integer integer2) {
                System.out.println("integer = [" + integer + "], integer2 = [" + integer2 + "]");
            }
        });
        TimeUnit.SECONDS.sleep(2);
    }

    /**
     *
     * applyToEither 方法
     * 两个CompletionStage，谁执行返回的结果快，
     * 我就用那个CompletionStage的结果进行下一步的转化操作。
     *
     * public <U> CompletionStage<U> applyToEither(CompletionStage<? extends T> other,Function<? super T, U> fn);
     * public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> other,Function<? super T, U> fn);
     * public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> other,Function<? super T, U> fn,Executor executor);
     *
     */
    private static void applyToEither() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                int r = new Random().nextInt(3);
                try {
                    TimeUnit.SECONDS.sleep(r);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("t1="+r);
                return r;
            }
        });
        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(new Supplier<Integer> () {
            @Override
            public Integer get() {
                int r = new Random().nextInt(3);
                try {
                    TimeUnit.SECONDS.sleep(r);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("t2="+r);
                return r;
            }
        });
        CompletableFuture<Integer> result = f1.applyToEither(f2, new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) {
                System.out.println("integer = [" + integer + "]");
                return integer*2;
            }
        });
        System.out.println("result="+result.get());
    }

    /**
     * acceptEither 方法
     * 两个CompletionStage，谁执行返回的结果快，
     * 我就用那个CompletionStage的结果进行下一步的消耗操作。
     *
     * public CompletionStage<Void> acceptEither(CompletionStage<? extends T> other,Consumer<? super T> action);
     * public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> other,Consumer<? super T> action);
     * public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> other,Consumer<? super T> action,Executor executor);
     */
    private static void acceptEither() throws InterruptedException {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                int r = new Random().nextInt(3);
                try {
                    TimeUnit.SECONDS.sleep(r);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("t11="+r);
                return r;
            }
        });
        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(new Supplier<Integer> () {
            @Override
            public Integer get() {
                int r = new Random().nextInt(3);
                try {
                    TimeUnit.SECONDS.sleep(r);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("t22="+r);
                return r;
            }
        });
        f1.acceptEither(f2, new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                System.out.println("integer = [" + integer + "]");
            }
        });
        TimeUnit.SECONDS.sleep(3);
    }

    /**
     *
     * runAfterEither 方法
     * 两个CompletionStage，任何一个完成了都会执行下一步的操作（Runnable）
     *
     * public CompletionStage<Void> runAfterEither(CompletionStage<?> other,Runnable action);
     * public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other,Runnable action);
     * public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other,Runnable action,Executor executor);
     *
     */
    private static void runAfterEither() throws InterruptedException {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                int r = new Random().nextInt(3);
                try {
                    TimeUnit.SECONDS.sleep(r);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("t111="+r);
                return r;
            }
        });
        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(new Supplier<Integer> () {
            @Override
            public Integer get() {
                int r = new Random().nextInt(3);
                try {
                    TimeUnit.SECONDS.sleep(r);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("t222="+r);
                return r;
            }
        });
        f1.runAfterEither(f2,()->{
            System.out.println("上面已经执行完了一个。。。");
        });
        TimeUnit.SECONDS.sleep(3);
    }

    /**
     * runAfterBoth
     * 两个CompletionStage，都完成了计算才会执行下一步的操作（Runnable）
     *
     * public CompletionStage<Void> runAfterBoth(CompletionStage<?> other,Runnable action);
     * public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other,Runnable action);
     * public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other,Runnable action,Executor executor);
     *
     */
    private static void runAfterBoth() throws InterruptedException {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                int r = new Random().nextInt(3);
                try {
                    TimeUnit.SECONDS.sleep(r);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("t1111="+r);
                return r;
            }
        });
        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(new Supplier<Integer> () {
            @Override
            public Integer get() {
                int r = new Random().nextInt(3);
                try {
                    TimeUnit.SECONDS.sleep(r);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("t2222="+r);
                return r;
            }
        });
        f1.runAfterBoth(f2,()->{
            System.out.println("上面全已经执行完了。。。");
        });
        TimeUnit.SECONDS.sleep(3);
    }

    /**
     *
     * thenCompose 方法
     * thenCompose 方法允许你对两个 CompletionStage 进行流水线操作，
     * 第一个操作完成时，将其结果作为参数传递给第二个操作。
     *
     * public <U> CompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn);
     * public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) ;
     * public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn, Executor executor) ;
     *
     */
    private static void thenCompose() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> f = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                int t = new Random().nextInt(3);
                System.out.println("t1="+t);
                return t;
            }
        }).thenCompose(new Function<Integer, CompletionStage<Integer>>() {
            @Override
            public CompletionStage<Integer> apply(Integer integer) {
                System.out.println("integer = [" + integer + "]");
                return CompletableFuture.supplyAsync(new Supplier<Integer>() {
                    @Override
                    public Integer get() {
                        int p = integer * 2;
                        System.out.println("p="+p);
                        return p;
                    }
                }) ;
            }
        });
        System.out.println("f result=" + f.get());
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 无返回值
//        runAsync();
        // 有返回值
//        supplyAsync();
        // 计算结果完成时的回调方法
//        whenComplete();
        // 一个线程依赖另一个线程
//        thenApply();
        // 执行任务完成时对结果的处理
//        handle();
        // 接收任务的处理结果，并消费处理，无返回结果。
//        thenAccept();
        // 同 thenAccept 方法类似,只是处理玩任务后，执行 thenAccept 的后续操作。
//        thenRun();
        // 合并任务
//        thenCombine();
        // thenAcceptBoth来进行消耗
//        thenAcceptBoth();
        // 返回的结果快的先操作
//        applyToEither();
        // 返回的结果快的先消费
//        acceptEither();
        // 任何一个完成了都会执行下一步的操作
//        runAfterEither();
        // 多个任务完成
//        runAfterBoth();
        // 传递给第二个操作
        thenCompose();
    }



}
