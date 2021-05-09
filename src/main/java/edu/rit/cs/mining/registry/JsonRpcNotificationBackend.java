package edu.rit.cs.mining.registry;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.server.Dispatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class JsonRpcNotificationBackend extends Thread {
    public Registry reg;
    public Socket socket;
    public Dispatcher dispatcher;
    private BufferedReader in;
    private PrintWriter out;
    public JsonRpcNotificationBackend(Socket socket, Registry r) {
        try {
            this.reg = r;
            this.socket = socket;
            this.dispatcher = new Dispatcher();
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out = new PrintWriter(socket.getOutputStream(), true);


            dispatcher.register(new JsonRpcRequestsHandler.HeartbeatAnalyzer(this.reg,this.socket));
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    /**
     * This method parses the Json requests received.
     *
     */


    public void run(){
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out = new PrintWriter(socket.getOutputStream(), true);

            // read request
            String line;
            line = in.readLine();
            //System.out.println(line);
            StringBuilder raw = new StringBuilder();
            raw.append("" + line);
            boolean isPost = line.startsWith("POST");
            int contentLength = 0;
            while (!(line = in.readLine()).equals("")) {
                //System.out.println(line);
                raw.append('\n' + line);
                if (isPost) {
                    final String contentHeader = "Content-Length: ";
                    if (line.startsWith(contentHeader)) {
                        contentLength = Integer.parseInt(line.substring(contentHeader.length()));
                    }
                }
            }
            StringBuilder body = new StringBuilder();
            if (isPost) {
                int c = 0;
                for (int i = 0; i < contentLength; i++) {
                    try {
                        c = in.read();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    body.append((char) c);
                }
            }

            //System.out.println(body.toString());
            JSONRPC2Notification request = null;
            try {
                request = JSONRPC2Notification.parse(body.toString());
            } catch (JSONRPC2ParseException e) {
                e.printStackTrace();
            }
            //JSONRPC2Response resp = dispatcher.process(request, null);
            dispatcher.process(request, null);
            socket.close();

        }catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }


}
