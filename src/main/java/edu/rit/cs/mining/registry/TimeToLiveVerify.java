package edu.rit.cs.mining.registry;

import edu.rit.cs.mining.Config;

import java.util.Iterator;

/**
 * This class is used to verify if a miner is still active in the network by checking if the last heatbeat timestamp exceeds more than 30 seconds.
 */

public class TimeToLiveVerify extends Thread {

    public Registry reg;

    public TimeToLiveVerify(Registry re) {
        this.reg = re;
    }

    public void run() {
        try {
            while (true) {
                Iterator<String> iterator = this.reg.miners.keySet().iterator();
                while (iterator.hasNext()) {
                    String minerid = iterator.next();
                    if (System.currentTimeMillis() - this.reg.miners.get(minerid).getTime() > Config.ECHO_TIMEOUT) {
                        this.reg.miners.remove(minerid);
                        synchronized (this.reg.o) {

                            System.out.println("Removed miner with minerid: "+minerid);

                            this.reg.minercount--;
                        }

                    }
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
