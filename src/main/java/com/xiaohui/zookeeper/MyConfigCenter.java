package com.xiaohui.zookeeper;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MyConfigCenter implements Watcher {

    private String ip = "192.168.231.137:2181";
    private CountDownLatch countDownLatch;
    private  ZooKeeper zooKeeper;
    private String url ;
    private String username ;
    private  String password ;
    @Override
    public void process(WatchedEvent event) {
        // none表示客户端处于连接正常中
        Watcher.Event.KeeperState state = event.getState();
        try {
            if(event.getType() == Event.EventType.None){
                switch (state){
                    case SyncConnected:{
                        System.out.println("zookeeper 已连接！");
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
            }else if(event.getType() == Event.EventType.NodeDataChanged){
                // 一有数据变化，为该路径重新加入监听。则重新获取数据
                System.out.println("change path:"+ event.getPath());
                // 重新读取配置信息
//                url = new String(zooKeeper.getData("/config/url", true, null));
//                username = new String(zooKeeper.getData("/config/username", true, null));
//                password = new String(zooKeeper.getData("/config/password", true, null));
                initValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        countDownLatch.countDown(); // 这里让计数器自减，使得主线程不再阻塞
    }

    public MyConfigCenter(){
        initValue();
    }

    // 连接zookeeper服务器，读取配置信息
    public void initValue(){
        try {
            countDownLatch = new CountDownLatch(1);
            // 保持zooKeeper连接
            if(null == zooKeeper){
                zooKeeper = new ZooKeeper(ip, 1000, this);
            }else{
                countDownLatch.countDown();
            }
            // 阻塞连接，等待连接成功
            countDownLatch.await();
            // 读取配置信息
            this.url = new String(zooKeeper.getData("/config/url", true, null));
            this.username = new String(zooKeeper.getData("/config/username", true, null));
            this.password = new String(zooKeeper.getData("/config/password", true, null));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MyConfigCenter myConfigCenter = new MyConfigCenter();
        try {
            while (true){
                System.out.println("url:"+myConfigCenter.getUrl());
                System.out.println("username:"+myConfigCenter.getUsername());
                System.out.println("password:"+myConfigCenter.getPassword());
                TimeUnit.SECONDS.sleep(5);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }
}
