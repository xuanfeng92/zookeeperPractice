package com.xiaohui.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import javax.sound.midi.Soundbank;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ZookeeperCommon {

    private static final long SECOND = 1000L;

    private static ZooKeeper zooKeeper;

    public static ZooKeeper getZooKeeper(String ip){
        ZooKeeper zooKeeper = null;
        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);

            // 以下创建你zookeeper是异步的,因此使用countDownLatch,让线程阻塞
            zooKeeper = new ZooKeeper(ip, 1000, event -> {
//                if(event.getState() == Watcher.Event.KeeperState.SyncConnected){
//                    System.out.println("连接成功！");
//                    countDownLatch.countDown(); // 这里让计数器自减，使得主线程不再阻塞
//                }
                Watcher.Event.KeeperState state = event.getState();
                switch (state){
                    case SyncConnected:{
                        System.out.println("zookeeper 已连接！");
                        System.out.println("path="+ event.getPath());
                        System.out.println("eventType="+ event.getType());
                        break;
                    }
                    case Disconnected:{
                        // 测试时，可以断开网络
                        System.out.println("zookeeper 已断开！");
                        break;
                    }
                    case AuthFailed:{
                        System.out.println("zookeeper 认证失败！");
                        break;
                    }
                    case Expired:{
                        // 可以尝试增大/缩减会话连接时间
                        System.out.println("zookeeper 会话超时！");
                        break;
                    }
                }
                countDownLatch.countDown(); // 这里让计数器自减，使得主线程不再阻塞
            });
            // 主线程阻塞等待连接对象创建成功
            countDownLatch.await(10, TimeUnit.SECONDS);
            if(zooKeeper.getSessionId()>0){
                return zooKeeper;
            }else{
                System.out.println("10s内连接超时！！！");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return zooKeeper;
    }

    public static void closeZookeeper(ZooKeeper zooKeeper){
        if(null != zooKeeper){
            try {
                zooKeeper.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
