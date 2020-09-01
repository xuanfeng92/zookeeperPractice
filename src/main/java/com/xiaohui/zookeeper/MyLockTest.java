package com.xiaohui.zookeeper;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MyLockTest {
    private void sell(){
        System.out.println("售票开始");
        try {
            // 代表复杂逻辑执行了一段时间
            TimeUnit.SECONDS.sleep(5);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("售票结束");
    }

    public void sellTicketWithLock() throws Exception {
        MyLock myLock = new MyLock();

        myLock.acquireLock();
        sell();
        // 释放锁
        myLock.releaseLock();

    }

    public static void run(Runnable runable, int threadNum){
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(30, 30,
                0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));
        try {
            for (int i = 0; i < threadNum; i++) {
                threadPoolExecutor.execute(runable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        threadPoolExecutor.shutdown();
    }

    public static void main(String[] args) throws Exception {
        MyLockTest myLockTest = new MyLockTest();
        // 方式二： 多线程多客户端测试
//        run(() -> {
//            try {
//                myLockTest.sellTicketWithLock();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        },30);

        // 方式一： 单线程多客户端测试
        for(int i =0; i<10; i++){
            myLockTest.sellTicketWithLock();
        }
    }
}
