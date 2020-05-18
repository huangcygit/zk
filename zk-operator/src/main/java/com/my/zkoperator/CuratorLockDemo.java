package com.my.zkoperator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class CuratorLockDemo {
    private static final String nodes = "192.168.13.128:2181";

    private static int count = 0;

    public static void main(String[] args) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(nodes)
                .connectionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .build();

        client.start();

        InterProcessMutex lock = new InterProcessMutex(client,"/zk/locks");
        for (int i = 0; i < 100; i++){
            Thread t = new Thread(() -> {
                try {
                    lock.acquire();
                    for (int j = 0; j < 1000; j++){
                        count++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        lock.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
            t.join();
        }

        System.out.println(count);

    }
}
