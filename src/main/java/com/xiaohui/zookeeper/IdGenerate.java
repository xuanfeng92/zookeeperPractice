package com.xiaohui.zookeeper;

import org.apache.zookeeper.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class IdGenerate {

    private static final String IP = "192.168.231.137:2181";
    private static CountDownLatch countDownLatch = new CountDownLatch(1);
    private static ZooKeeper zooKeeper;

    public static String generateId() throws Exception {
        return zooKeeper.create("/id", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }


    public static void main(String[] args) throws Exception {
        zooKeeper = new ZooKeeper(IP, 5000, new ZKWatcher());
        countDownLatch.await();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 5, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));
        for (int i = 0; i < 10; i++) {
            threadPoolExecutor.execute(() -> {
                try {
                    System.out.println(generateId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        TimeUnit.SECONDS.sleep(50);
        threadPoolExecutor.shutdown();
    }

    private static class ZKWatcher implements Watcher {
        @Override
        public void process(WatchedEvent watchedEvent) {
            countDownLatch.countDown();
            System.out.println("zk的监听器" + watchedEvent.getType());
        }
    }
}
