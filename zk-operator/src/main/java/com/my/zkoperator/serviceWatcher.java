package com.my.zkoperator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class serviceWatcher  implements CuratorWatcher {

    private static final String nodes = "192.168.13.128:2181";

    private static String path = "/zk/service";

    static CuratorFramework client = null;

    static ExecutorService executorService = Executors.newFixedThreadPool(4);

    static HashMap<String,List<String>> serviceMap;

    static List<String> overService = new ArrayList<>();

    static HashMap<String,List<String>> map = null;

    static HashMap<String,List<String>> sourceMap = new HashMap<>();

    public static HashMap<String,List<String>> source(){
        map = new HashMap<>();
        map.put("A",CollectionUtils.arrayToList(new String[]{"B","C","D"}));
        map.put("B",CollectionUtils.arrayToList(new String[]{"E"}));
        map.put("C",CollectionUtils.arrayToList(new String[]{"D"}));
        map.put("D",CollectionUtils.arrayToList(new String[]{"G"}));
        map.put("E",CollectionUtils.arrayToList(new String[]{"C","F"}));
        map.put("F",CollectionUtils.arrayToList(new String[]{"C","D","G","I"}));
        map.put("G",CollectionUtils.arrayToList(new String[]{"H"}));
        map.put("H",null);
        map.put("I",CollectionUtils.arrayToList(new String[]{"G","H"}));
        map.put("M",null);
        return map;
    }

    static {
        client = CuratorFrameworkFactory.builder()
                .connectString(nodes)
                .connectionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .build();

        client.start();
    }

    public static void main(String[] args) throws Exception {
        serviceWatcher watcher = new serviceWatcher();
        List<String> root = new ArrayList<>();
        serviceMap = watcher.trans(root,serviceWatcher.source());

        Iterator<String> mapItr = serviceMap.keySet().iterator();
        while (mapItr.hasNext()) {
            String key = mapItr.next();
            List<String> value = serviceMap.get(key);
            System.out.println(key + " --> " + value);

            client.create().creatingParentsIfNeeded().forPath(path + "/" + key,key.getBytes());
            client.getData().usingWatcher(watcher).forPath(path + "/" + key);
        }

        for (String r : root){
            ServiceTask task = new ServiceTask(r,client);
            executorService.execute(task);
        }

//        client.create().creatingParentsIfNeeded().forPath(path + "/" + "test","key".getBytes());
//        client.getData().usingWatcher(watcher).forPath(path + "/" + "test");
//        client.delete().forPath(path + "/" + "test");

        System.in.read();
    }



    public HashMap<String,List<String>> trans(List<String> root,HashMap<String,List<String>> source){
        HashMap<String, List<String>> map = new HashMap<>();
        Iterator<String> itr = source.keySet().iterator();
        while (itr.hasNext()){
            String key = itr.next();
            List<String> value = source.get(key);
            map.put(key,new ArrayList<String>());
            if (CollectionUtils.isEmpty(value)){
                root.add(key);
            }
        }

        Iterator<String> mapItr = map.keySet().iterator();

        while (mapItr.hasNext()){
            String key = mapItr.next();
            List<String> value = map.get(key);

            itr = source.keySet().iterator();
            while (itr.hasNext()){
                String sk = itr.next();
                List<String> sValue = source.get(sk);

                if (!CollectionUtils.isEmpty(sValue) && sValue.contains(key)){
                    value.add(sk);
                }
            }
        }

        return map;
    }

    @Override
    public void process(WatchedEvent watchedEvent) throws Exception {
        System.out.println("--------------------------------------------------");
        System.out.println(watchedEvent.getType());
        String result = watchedEvent.getPath().replace(path + "/","");
        System.out.println(result);

        List<String> strings = serviceMap.get(result);
        if (!CollectionUtils.isEmpty(strings)){
            for (String service : strings){
                synchronized (ServiceTask.class){
                    List<String> over = sourceMap.get(service);
                    if (over == null){
                        over = new ArrayList<>();
                    }
                    if (!over.contains(result)){
                        over.add(result);
                    }

                    sourceMap.put(service,over);

                    if (over.size() == map.get(service).size() && !overService.contains(service)){
                        ServiceTask task = new ServiceTask(service,client);
                        executorService.execute(task);
                    }
                }
            }
        }
    }
}
