package edu.rit.cs.mining.miner;

import edu.rit.cs.mining.Config;

import java.net.ServerSocket;

public class MinerJsonRpcNotificationListener extends Thread{


    public Miner miner;
    public MinerJsonRpcNotificationListener(Miner r){
        miner = r;
    }

    /**
     * This method listens on a predefined port and when receives a request will launch a new thread to analyze the request and
     * starts hearing again.
     *
     */

    public void run(){

        try(ServerSocket serversocket = new ServerSocket(Config.MINER_PORT_NOTIFICATIONS)){
            while(this.miner.isMining){
                System.out.println("received new Message in miner notification");
                new MinerJsonRpcNotificationBackend(serversocket.accept(), this.miner).start();
            }
        }catch (Exception e){
            System.out.println("error in Notification Listener in Miner!!!!!!!!!!!!!!");
            System.exit(-1);
        }

//        try {
//            while (true) {
//
//                    //System.out.println("Received message from another Miner in MinerListner");
//
//                    ServerSocket servsocket = new ServerSocket(Config.MINER_PORT_NOTIFICATIONS);
//                    new MinerJsonRpcNotificationBackend(servsocket.accept(), this.miner).start();
//                    servsocket.close();
//
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//            System.out.println(e.getMessage());
//            System.out.println("Error in AllJSonRequestsHandler in accepting port");
//            System.exit(-3);
//        }
    }
}
