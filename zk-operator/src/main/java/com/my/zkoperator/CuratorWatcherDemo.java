package com.my.zkoperator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;

import java.io.IOException;

public class CuratorWatcherDemo {
    private static final String nodes = "192.168.13.128:2181";

    public static void main(String[] args) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(nodes)
                .connectionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .build();

        client.start();

        //监控当前节点的 增/删/改
        watcherCurrentNode(client);

        //监控直接子节点的 增/删/改
        watcherChildNode(client);

        //监控自己及所有子节点 增/删/改
        watcherClildAndCurrent(client);

        System.in.read();

    }

    private static void watcherClildAndCurrent(CuratorFramework client) throws Exception {
        TreeCache treeCache = new TreeCache(client,"/zk/demo");
        TreeCacheListener listener = new TreeCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent event) throws Exception {
                switch (event.getType()){
                    case NODE_ADDED:
                    {
                        System.out.println("NODE_ADDED: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                        break;
                    }
                    case NODE_UPDATED:
                    {
                        System.out.println("NODE_UPDATED: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                        break;
                    }

                    case NODE_REMOVED:
                    {
                        System.out.println("NODE_REMOVED: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                        break;
                    }
                }
            }
        };
        treeCache.getListenable().addListener(listener);
        treeCache.start();
    }

    private static void watcherChildNode(CuratorFramework client) throws Exception {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client,"/zk/demo",true);
        PathChildrenCacheListener listener = new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()){
                    case CHILD_ADDED:
                    {
                        System.out.println("Node added: " + ZKPaths.getNodeFromPath(event.getData().getPath()) + "->"  + event.getData().getData());
                        break;
                    }

                    case CHILD_UPDATED:
                    {
                        System.out.println("Node changed: " + ZKPaths.getNodeFromPath(event.getData().getPath()) + "->" + event.getData().getData());
                        break;
                    }

                    case CHILD_REMOVED:
                    {
                        System.out.println("Node removed: " + ZKPaths.getNodeFromPath(event.getData().getPath()) + "->"  + event.getData().getData());
                        break;
                    }
                }
            }
        };
        pathChildrenCache.getListenable().addListener(listener);
        pathChildrenCache.start();
    }

    private static void watcherCurrentNode(CuratorFramework client) throws Exception {
        NodeCache nodeCache = new NodeCache(client,"/zk/demo",false);
        NodeCacheListener listener = new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                System.out.println("currentnode up : ");
            }
        };

        nodeCache.getListenable().addListener(listener);
        nodeCache.start();
    }
}
