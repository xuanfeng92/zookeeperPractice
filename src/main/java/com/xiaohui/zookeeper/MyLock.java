package com.xiaohui.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MyLock {

    String ip = "192.168.231.137:2181";

    // 计数器对象
    CountDownLatch countDownLatch = new CountDownLatch(1);

    ZooKeeper zooKeeper;

    private static final String LOCK_ROOT_PATH = "/Locks";
    private static final String LOCK_NODE_NAME = "Lock_";

    private String lockPath; // 临时存储锁的路径

    public MyLock(){
        try {
            if(null == zooKeeper){
                zooKeeper = new ZooKeeper(ip, 5000, new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        if(event.getType() == Event.EventType.None){
                            if(event.getState() == Event.KeeperState.SyncConnected){
                                System.out.println("连接成功！");
                                countDownLatch.countDown();
                            }
                        }
                    }
                });
            }else{
                countDownLatch.countDown();
            }

            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void acquireLock() throws Exception {
        // 创建当前请求锁节点
        createLock();
        // 尝试获取锁
        attemptLock();
    }

    // 监视器对象。监视上一个节点是否被删除
    Watcher watcher = new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            if(event.getType() == Event.EventType.NodeDeleted){
                synchronized (this){
                    notifyAll();
                }
            }
        }
    };

    // 尝试获取锁
    private void attemptLock() throws Exception {
        // 获取 Locks下的所有子节点
        List<String> children = zooKeeper.getChildren(LOCK_ROOT_PATH, false);
        Collections.sort(children); // 递增排序的，所以第一个是最小的

        int index = children.indexOf(lockPath.substring(LOCK_ROOT_PATH.length() + 1)); // 获取lockPath 所在的索引位置
        if(index == 0){// 如果index=0，说明当前只有自己，表示已经获取到锁
            System.out.println("获取锁成功！");
        }else if(index >0){
            String path = children.get(index - 1);//获取上一个节点路径
            Stat exists = zooKeeper.exists(LOCK_ROOT_PATH + "/" + path, watcher);
            if(exists == null){ // 执行上两步骤中，有可能已经删除了当前的锁，因此做一个判断
                attemptLock();
            }else{
                // 同步当前客户端
                synchronized (watcher){
                    watcher.wait();
                }
                attemptLock();
            }
        }

    }

    // 创建当前请求锁节点
    private void createLock() throws Exception {
        Stat exists = zooKeeper.exists(LOCK_ROOT_PATH, false);
        if(exists == null){
            // 创建一个永久的父节点
            zooKeeper.create(LOCK_ROOT_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        // 创建临时的有序节点
        lockPath = zooKeeper.create(LOCK_ROOT_PATH+ "/"+ LOCK_NODE_NAME, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("节点创建成功："+ lockPath);
    }

    // 释放锁
    public void releaseLock() throws Exception {
        zooKeeper.delete(this.lockPath, -1);
        zooKeeper.close();
        System.out.println("锁已经释放："+this.lockPath);
    }

    public void closeZookeeper(){
        if(null != zooKeeper){
            try {
                zooKeeper.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}