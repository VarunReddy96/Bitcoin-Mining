package edu.rit.cs.mining.registry;

import edu.rit.cs.mining.Config;

import java.net.ServerSocket;
//name: MinerRequestsListner

/**
 * This class is used to Listen for any JsonRequests from other Miners.
 */

public class JsonRpcRequestsListener extends Thread {

    public Registry reg;

    public JsonRpcRequestsListener(Registry r) {
        reg = r;
    }

    /**
     * This method listens on a predefined port and when receives a request will launch a new thread to analyze the request and
     * starts hearing again.
     */

    public void run() {

        try(ServerSocket serversocket = new ServerSocket(Config.REGISTRY_PORT)){
            while(this.reg.isActive){
                new JsonRpcRequestsBackend(serversocket.accept(), this.reg).start();
            }
        }catch (Exception e){
            System.out.println("error in Heart Listner!!!!!!!!!!!!!!");
            System.exit(-1);
        }


//        try {
//            while (true) {
//
//                ServerSocket servsocket = new ServerSocket(Config.REGISTRY_PORT);
//                new JsonRpcRequestsBackend(servsocket.accept(), this.reg).start();
//                servsocket.close();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println(e.getMessage());
//            System.out.println("Error in accpeting sockets in JsonRpcLsitner");
//            System.exit(-4);
//        }
    }

}
