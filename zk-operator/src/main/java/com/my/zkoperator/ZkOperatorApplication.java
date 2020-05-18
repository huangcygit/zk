package com.my.zkoperator;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.server.quorum.QuorumPeerMain;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ZkOperatorApplication {

    public static void main(String[] args) {
        QuorumPeerMain main;
        ZooKeeper zooKeeper;
        SpringApplication.run(ZkOperatorApplication.class, args);
    }

}
