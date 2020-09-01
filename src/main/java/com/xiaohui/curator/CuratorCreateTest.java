package com.xiaohui.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CuratorCreateTest {

    CuratorFramework client;
    String ip = "192.168.231.137:2183,192.168.231.137:2182,192.168.231.137:2181";
    @Before
    public void before(){
        // 重连策略： 每隔1s连接一次，连接3次
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        // 创建连接对象
        client = CuratorFrameworkFactory.builder()
                // 连接ip/ip集群
                .connectString(ip)
                // 会话超时时间
                .sessionTimeoutMs(5000)
                // 重连机制
                .retryPolicy(retryPolicy)
                // 命名空间 指定父节点，如果没有，则会创建
                .namespace("create2")
                // 创建连接对象
                .build();
        // 打开连接
        client.start();
        System.out.println("curator启动状态："+client.isStarted());
    }


    @Test
    public void create1() throws Exception {
        // 新增节点
        client.create()
                // 指定节点类型
                .withMode(CreateMode.PERSISTENT)
                // 指定权限
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                // 创建节点数据
                .forPath("/node1","node1".getBytes());
        System.out.println("结束！");
    }

    @Test
    public void create2() throws Exception {
        // 自定义权限列表
        List<ACL> list = new ArrayList<>();
        // 授权模式和授权对象
        Id id = new Id("ip", "192.168.231.138");
        list.add(new ACL(ZooDefs.Perms.ALL, id));

        client.create()
                .withMode(CreateMode.PERSISTENT)
                .withACL(list)
                .forPath("/node2", "node2".getBytes());
        System.out.println("结束！");
    }

    @Test
    public void create3() throws Exception {
        // 递归创建节点
        client.create()
                // 支持递归创建节点
                .creatingParentsIfNeeded()
                // 指定节点类型
                .withMode(CreateMode.PERSISTENT)
                // 指定权限
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                // 创建节点数据
                .forPath("/node3/node3_1","node3".getBytes());
        System.out.println("结束！");
    }

    @Test
    public void create4() throws Exception {
        // 异步方式创建节点
        client.create()
                // 支持递归创建节点
                .creatingParentsIfNeeded()
                // 指定节点类型
                .withMode(CreateMode.PERSISTENT)
                // 指定权限
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                // 异步调用
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                        System.out.println("异步创建成功!");
                    }
                })
                // 创建节点数据
                .forPath("/node4/node4_2","node4".getBytes());
        TimeUnit.SECONDS.sleep(5);
        System.out.println("结束！");
    }
    @After
    public void after(){
        client.close();
    }
}
