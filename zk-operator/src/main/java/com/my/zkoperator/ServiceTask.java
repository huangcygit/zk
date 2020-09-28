package com.my.zkoperator;

import lombok.SneakyThrows;
import org.apache.curator.framework.CuratorFramework;

import java.util.ArrayList;
import java.util.List;

public class ServiceTask implements Runnable {

    private String name;

    CuratorFramework client;

    private static String path = "/zk/service";

    public ServiceTask(String name,CuratorFramework client) {
        this.name = name;
        this.client = client;
    }

    @Override
    public void run() {
        try {
//            if ("I".equals(name)){
//                Thread.sleep(200);
//            }
            System.out.println("任务" + name + "执行");
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            serviceWatcher.overService.add(name);
            client.delete().forPath(path + "/" + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
