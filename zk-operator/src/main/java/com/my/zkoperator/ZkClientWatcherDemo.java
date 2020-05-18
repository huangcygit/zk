package com.my.zkoperator;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;

public class ZkClientWatcherDemo implements Watcher {

    private static final String nodes = "192.168.13.128:2181";

    private static ZooKeeper zooKeeper;

    static {
        try {
            zooKeeper = new ZooKeeper(nodes,5000,new ZkClientWatcherDemo());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        System.out.println("event type : " + watchedEvent.getType());
        if(watchedEvent.getType() != Event.EventType.None){
            try {
                zooKeeper.exists(watchedEvent.getPath(),true);
//                zooKeeper.getChildren(watchedEvent.getPath(),true);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        String path = "/zk/watchet";
        if (zooKeeper.exists(path,false) == null){
            zooKeeper.create(path,"demo".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
        }

        Thread.sleep(1000);
        Stat stat=zooKeeper.exists(path,true);

       zooKeeper.getChildren(path,true);

        System.in.read();
    }
}
