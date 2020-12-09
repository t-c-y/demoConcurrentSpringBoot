package com.example.demo.jvm.day27;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ApiOptimizeTest {
    /**
     * 接口调优
     * 很多时候接口的逻辑，整个过程是按顺序执行的，
     * 实际上多个查询之间是没有任何依赖关系，
     * 所以说多个查询可以同时执行，那我们对这多个步骤采用多线程并行执行
     *
     * 整个优化过程：
     * 1.先列出无依赖的一些操作
     * 2.将这些操作改为并行的方式
     *
     * 用到的技术有：
     * 1.线程池相关知识
     * 2.Executors、Future相关知识
     *
     * 总结：
     * 1.对于无依赖的操作尽量采用并行方式去执行，可以很好的提升接口的性能
     * 2.大家可以在你们的系统中试试这种方法，感受一下效果，会让你感觉很爽
     */

    public Map<String, Object> getUserInfo(Integer userId) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(200);
        Map<String, Object> result = new HashMap<>();
        result.put("id", userId);
        result.put("name", "名字"+userId);
        result.put("sex", userId%2);
        return result;
    }

    public String getMsg(String id) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(200);
        return "说明" + id + " , 详细内容如下。。。";
    }

    public List<String> getInfoList(long goodsId) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(200);
        return Arrays.asList("图1", "图2", "图3");
    }

    /**
     * 普通方式获取信息集合
     * @param userId
     * @param msgId
     * @param goodsId
     * @return
     * @throws InterruptedException
     */
    public Map<String, Object> getDetailsSigle(Integer userId, String msgId, long goodsId) throws InterruptedException {
        Map<String, Object> result = new HashMap<>();
        result.put("user", getUserInfo(userId));
        result.put("msg", getMsg(msgId));
        result.put("infoList", getInfoList(goodsId));
        return result;
    }

    private static final  ExecutorService es = Executors.newFixedThreadPool(50);

    /**
     * 并行方式获取信息集合
     * @param userId
     * @param msgId
     * @param goodsId
     * @return
     * @throws InterruptedException
     */
    public Map<String, Object> getDetails(Integer userId, String msgId, long goodsId)
            throws InterruptedException, ExecutionException, TimeoutException {
        Map<String, Object> result = new HashMap<>();
        Future<Map<String, Object>> userFuture =  es.submit(()->getUserInfo(userId));
        Future<String> msgFuture = es.submit(() -> getMsg(msgId));
        Future<List<String>> infoFuture = es.submit(() -> getInfoList(goodsId));
        result.put("user", userFuture.get(1, TimeUnit.SECONDS));
        result.put("msg", msgFuture.get(1, TimeUnit.SECONDS));
        result.put("infoList", infoFuture.get(1, TimeUnit.SECONDS));
        return result;
    }

    /**
     * 并行方式获取信息集合
     * @param userId
     * @param msgId
     * @param goodsId
     * @return
     * @throws InterruptedException
     */
    public Callable<Map<String, Object>> getDetailsAsc(Integer userId,
                                                       String msgId,
                                                       long goodsId) {
        return () -> {
            Map<String, Object> result = new HashMap<>();
            Future<Map<String, Object>> userFuture =  es.submit(()->getUserInfo(userId));
            Future<String> msgFuture = es.submit(() -> getMsg(msgId));
            Future<List<String>> infoFuture = es.submit(() -> getInfoList(goodsId));
            result.put("user", userFuture.get(1, TimeUnit.SECONDS));
            result.put("infoList", infoFuture.get(1, TimeUnit.SECONDS));
            result.put("msg", msgFuture.get(1, TimeUnit.SECONDS));
            return result;
        };
    }

    public static void main(String[] args)
            throws InterruptedException, ExecutionException {
        //接口速度对比
//        test1();
        //测试循环并行
//        test2();
        //测试循环并行
        test3();
    }

    public static void test3() {
        long st = System.currentTimeMillis();
        System.out.println(st + "开始!");
        ApiOptimizeTest api = new ApiOptimizeTest();
        //创建ExecutorCompletionService对象
        ExecutorCompletionService<Map<String, Object>> executorCompletionService = new ExecutorCompletionService<>(es);
        ExecutorCompletionService<Map<String, Object>> executorCompletionService1 = new ExecutorCompletionService<>(es);
        int count = 10;
        for(int i = 1; i <= count; i++) {
            //异步查询
            executorCompletionService.submit(api.getDetailsAsc(i, i + "", i));
        }
        int count1 = 20;
        for(int i = 11; i <= count1; i++) {
            //异步查询
            executorCompletionService1.submit(api.getDetailsAsc(i, i + "", i));
        }


        Map<String, Object> result = new HashMap<>();
        //购买商品的数量
        for (int i = 0; i < count; i++) {
            try {
                //可以获取到最先到的商品
                Map<String, Object> infoMap = executorCompletionService.take().get();
                result.put("info"+i, infoMap);
            } catch (Exception e) {
//                e.printStackTrace();
                result.put("info"+i, null);
            }
        }
        for (int i = 11; i < count1; i++) {
            try {
                //可以获取到最先到的商品
                Map<String, Object> infoMap = executorCompletionService1.take().get();
                result.put("info"+i, infoMap);
            } catch (Exception e) {
//                e.printStackTrace();
                result.put("info"+i, null);
            }
        }
        es.shutdown();
        System.out.println("result=" + result
                + " ，耗时（ms）= " + (System.currentTimeMillis() - st));
    }

    public static void test2() throws InterruptedException, ExecutionException {
        ApiOptimizeTest api = new ApiOptimizeTest();
        Map<String, Object> result = new HashMap<>();
        long starTime = System.currentTimeMillis();
        for(int i=0; i<10; i++) {
            int finalI = i;
            try {
                Future<Map<String, Object>> infoFuture = es.submit(() -> api.getDetails(finalI, finalI+"", finalI));
                result.put("info"+i, infoFuture.get(1, TimeUnit.SECONDS));
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
        es.shutdown();
        System.out.println("result=" + result
                + " ，耗时（ms）= " + (System.currentTimeMillis() - starTime));
    }

    private static void test1() throws InterruptedException, ExecutionException {
        ApiOptimizeTest api = new ApiOptimizeTest();
        for (int i=0; i<3; i++) {
            System.out.println(i + " api调用开始啦！！！");
            long starTime = System.currentTimeMillis();
            System.out.println("普通info = {" + api.getDetailsSigle(1, "1", 1L) + "}"
                    + " ，耗时（ms）= " + (System.currentTimeMillis() - starTime));
            System.out.println("======================================================================");
            starTime = System.currentTimeMillis();
            try {
                System.out.println("并行info = {" + api.getDetails(1, "1", 1L) + "}"
                        + " ，耗时（ms）= " + (System.currentTimeMillis() - starTime));
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            System.out.println();
        }
        es.shutdown();
    }

}
