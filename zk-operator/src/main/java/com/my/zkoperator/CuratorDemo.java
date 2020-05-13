package com.my.zkoperator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

public class CuratorDemo {
    private static final String nodes = "192.168.13.128:2181";

    public static void main(String[] args) {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(nodes)
                .connectionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .build();

        client.start();

        //创建节点
        createNode(client);

//        updateNode(client);

//        String value = getNode(client);
//        System.out.println(value);

//        deleteNode(client);

    }

    private static void deleteNode(CuratorFramework client) {
        try {
            client.delete().forPath("/zk/demo");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getNode(CuratorFramework client) {
        try {
            byte[] obj = client.getData().forPath("/zk/demo");
            return new String(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void updateNode(CuratorFramework client) {
        try {
            client.setData().forPath("/zk/demo","demo".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createNode(CuratorFramework client) {
        try {
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath("/zk/demo1","demo1".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
