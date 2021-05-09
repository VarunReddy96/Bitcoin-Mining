package edu.rit.cs.mining;

import java.net.InetAddress;

/**
 * This class is used to represent the miner details like ipaddress and the latest timestamp of the login message received.
 *
 */

public class IpNode {

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public InetAddress ip;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long time;

    public IpNode(InetAddress add, long t){
        ip = add;
        time = t;
    }
}
