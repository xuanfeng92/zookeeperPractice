package com.xiaohui.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;

public class CuratorConnection {


    public static void main(String[] args) {
        // 创建连接对象
        CuratorFramework client = CuratorFrameworkFactory.builder()
                // 连接ip/ip集群
                .connectString("192.168.231.137:2183,192.168.231.137:2182,192.168.231.137:2181")
                // 会话超时时间
                .sessionTimeoutMs(5000)
                // 重连机制: RetryOneTime-隔一段时间连接一次
                .retryPolicy(new RetryOneTime(3000))
                // 命名空间
                .namespace("create")
                // 创建连接对象
                .build();
        // 打开连接
        client.start();

        System.out.println("启动状态："+client.isStarted());

        // 关闭连接
        client.close();
    }
}
