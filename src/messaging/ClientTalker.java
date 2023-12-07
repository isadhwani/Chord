package messaging;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


import models.ClientState;


public class ClientTalker extends Thread {
    public boolean sendStore;
    ClientState state;
    public String targetHostname;
    int port;
    String myHostname;




    public ClientTalker(ClientState s, String targetHostname, String myHostname, int port) {
        this.state = s;
        this.targetHostname = targetHostname;
        this.port = port;
        this.myHostname = myHostname;
    }


    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = new Socket(targetHostname, port);

                OutputStream outputStream = socket.getOutputStream();


                if(sendStore) {
                    /**
                     * reqID, operationType,
                     * objectID, and clientID. reqID must be monotonically increasing per client, operationType is STORE
                     * or RETRIEVE, objectID and client ID are numbered from 1 to 127.
                     */
                    String message = "{message:client, reqID:" + state.requestID + ", operationType: STORE, objectID: " + state.objectIDToStore + ", clientID: " + state.clientToStoreAt + "}";
                    System.out.println("Sending STORE to " + targetHostname + ": " + message);
                    byte[] messageBytes = message.getBytes();
                    outputStream.write(messageBytes);
                    outputStream.flush();
                    sendStore = false;
                }



                sleep(1);

                // Close the socket
                socket.close();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}


