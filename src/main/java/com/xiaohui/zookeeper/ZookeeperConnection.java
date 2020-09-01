package com.xiaohui.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

public class ZookeeperConnection {
    public static void main(String[] args) {
        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);

            // 以下创建你zookeeper是异步的,因此使用countDownLatch,让线程阻塞
            ZooKeeper zooKeeper = new ZooKeeper("192.168.231.137:2181", 1000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if(event.getState() == Event.KeeperState.SyncConnected){
                        System.out.println("连接成功！");
                        countDownLatch.countDown(); // 这里让计数器自减，使得主线程不再阻塞
                    }
                }
            });
            // 主线程阻塞等待连接对象创建成功
            countDownLatch.await();
            System.out.println("会话ID"+zooKeeper.getSessionId());
            zooKeeper.close(); // 资源释放
        }catch (Exception e){

        }
    }
}
