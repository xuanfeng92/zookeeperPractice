package com.xiaohui.zookeeper;


import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.sound.midi.Soundbank;
import java.util.concurrent.TimeUnit;

public class ZookeeperTest {

    private ZooKeeper zooKeeper;

    @Before
    public void before(){
        System.out.println("--before--");
        zooKeeper = ZookeeperCommon.getZooKeeper("192.168.231.137:2181");
        System.out.println("--open zookeeper--");
    }

    @Test
    public void createtest1(){

        try {
            System.out.println("--start create--");
            // 同步方式
            Assert.assertNotNull(zooKeeper);
            if(zooKeeper.exists("/create/node1",false) == null){
                String result = zooKeeper.create("/create/node1", "123456".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                System.out.println("--result--:"+result);
            }else{
                System.out.println("--/create/node1-- 已存在");
            }

        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createTest2() throws  Exception{
        try {
            Assert.assertNotNull(zooKeeper);
            Stat exists = zooKeeper.exists("/create/node2", false);
            if(null == exists){
                zooKeeper.create("/create/node3", "node2".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, new AsyncCallback.StringCallback(){
                    /**
                     * @param rc 状态，0 则为成功，以下的所有示例都是如此
                     * @param path 路径
                     * @param ctx 上下文参数
                     * @param name 路径
                     */
                    public void processResult(int rc, String path, Object ctx, String name){
                        System.out.println(rc + " " + path + " " + name +  " " + ctx);
                    }
                }, "I am context");
                // 由于是异步的，这里休眠一段时间
                TimeUnit.SECONDS.sleep(5);
            }else{
                System.out.println("--/create/node2 已存在--");
            }

            System.out.println("结束");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void setData1() throws Exception{
        // arg1:节点的路径
        // arg2:修改的数据
        // arg3:数据的版本号 -1 代表版本号不参与更新
        // 同步方法
        Assert.assertNotNull(zooKeeper);
        Stat stat = zooKeeper.setData("/create/node2","node2-change".getBytes(),-1);
        System.out.println("version:"+stat.getAversion());
        System.out.println("transactionID:"+stat.getCzxid());
        System.out.println("stat:"+stat.toString());
    }

    @Test
    public void setData2() throws Exception{
        // arg1:节点的路径
        // arg2:修改的数据
        // arg3:数据的版本号 -1 代表版本号不参与更新
        // 异步方法
        Assert.assertNotNull(zooKeeper);
        zooKeeper.setData("/create/node2","node2-change3333".getBytes(),-1, (rc, path, ctx, stat) -> {
            System.out.println("rc:"+rc);
            System.out.println("path:"+path);
            System.out.println("ctx:"+ctx);
            System.out.println("stat:"+stat);
        },"my context");
        TimeUnit.SECONDS.sleep(5); // 由于是异步，此时阻塞几秒
        System.out.println("--结束--");
    }

    @Test
    public void getData1() throws Exception {
        Stat stat = new Stat();
        Assert.assertNotNull(zooKeeper);
        byte[] data = zooKeeper.getData("/create/node2", false, stat);
        System.out.println("data:"+new String(data));
        // 判空
        System.out.println("transactionID:"+stat.getCzxid());
        System.out.println("version:"+stat.getAversion());
    }

    @Test
    public void getData2() throws Exception {
        Assert.assertNotNull(zooKeeper);
        zooKeeper.getData("/create/node2", false, (rc, path, ctx, bytes, stat) -> {
            // 判空
            System.out.println(rc + " " + path
                    + " " + ctx + " " + new String(bytes) + " " +
                    stat.getCzxid());
        }, "I am context");
        TimeUnit.SECONDS.sleep(3);
    }

    @After
    public void after(){
        System.out.println("--after--");
        ZookeeperCommon.closeZookeeper(zooKeeper);
        System.out.println("--close zookeeper--");
    }
}
