package edu.rit.cs.mining.registry;

import edu.rit.cs.mining.Config;

import java.net.ServerSocket;
// change it to


/**
 * This class is used to Listen for any heartbeat JsonRequests from other Miners.
 */
public class MinerHeartbeatsListener extends Thread {

    public Registry reg;

    public MinerHeartbeatsListener(Registry r) {
        reg = r;
    }

    /**
     * This method listens on a predefined port and when receives a request will launch a new thread to analyze the request and
     * starts hearing again.
     */
    public void run() {

        try(ServerSocket serversocket = new ServerSocket(Config.MINER_Heartbeat_LISTNER_PORT)){
            while(this.reg.isActive){
                new JsonRpcNotificationBackend(serversocket.accept(), this.reg).start();
            }
        }catch (Exception e){
            System.out.println("error in Heart Listner!!!!!!!!!!!!!!");
            System.exit(-1);
        }

//        try {
//            //ServerSocket servsocket = new ServerSocket(Config.MINER_Heartbeat_LISTNER_PORT);
//            //ServerSocket serversocket = null;
//            while (true) {
//
////                try{
////                    s = new ServerSocket(Config.MINER_Heartbeat_LISTNER_PORT);
////                    new JsonRpcNotificationBackend(s.accept(), this.reg).start();
////                }catch (Exception e){
////
////                    e.printStackTrace();
////                    System.out.println(e.getMessage());
////                    System.err.println("Could not listen on port " + Config.MINER_Heartbeat_LISTNER_PORT +" in MinerHeatBeat");
////                    System.exit(-5);
////                }
//
//
//
////                ServerSocket serversocket = new ServerSocket(Config.MINER_Heartbeat_LISTNER_PORT);
////
////                serversocket.setSoTimeout(5000);
////                System.out.println("HeartBeat received in Listner");
////                new JsonRpcNotificationBackend(serversocket.accept(), this.reg).start();
//                //serversocket.close();
//            }
//
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//            System.out.println(e.getMessage());
//            System.err.println("Could not listen on port " + Config.MINER_Heartbeat_LISTNER_PORT + " in MinerHeatBeat");
//            System.exit(-5);
//        }

    }
}
