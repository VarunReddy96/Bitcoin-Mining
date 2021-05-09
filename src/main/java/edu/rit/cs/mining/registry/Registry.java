package edu.rit.cs.mining.registry;

import edu.rit.cs.mining.Blockchain;
import edu.rit.cs.mining.IpNode;

import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to implement all the functionalities of a Registry. It contains a Initial Blockchain, minerslist in the network.
 *
 */

public class Registry {
    ConcurrentHashMap<String, IpNode> miners;
    HashMap<Integer, Long> timechecker; // not required
    public final Object o;
    public Blockchain blkchain; // containng the first block;
    int minercount; // number of miners in the network
    public boolean isActive;
    public Registry(){
        miners = new ConcurrentHashMap<>();
        timechecker = new HashMap<>();
        o = new Object();
        blkchain = new Blockchain();
        minercount = 0;
        isActive = true;
    }

    /**
     * This method is used to launch various threads required to receive Json requests from other miners and also to listen to heatbeats.
     *
     */

    public void start(){
        JsonRpcRequestsListener listner  = new JsonRpcRequestsListener(this);
        TimeToLiveVerify checkActiveMiners = new TimeToLiveVerify(this);
        MinerListUpdates minersListUpdates = new MinerListUpdates(this);
        MinerHeartbeatsListener heartbeatlisten = new MinerHeartbeatsListener(this);
//        Runtime.getRuntime().addShutdownHook(new Thread(){public void run(){
//            isActive = false;
//            System.exit(0);
//        }});

        listner.start();
        checkActiveMiners.start();
        minersListUpdates.start();
        heartbeatlisten.start();
        Scanner scan = new Scanner(System.in);
        System.out.println("Press q and enter to exit");
        if(scan.hasNext()){
            this.isActive = false;

        }
        System.exit(0);
    }

    public static void main(String[] args) {
        new Registry().start();
    }
}
