package messaging;

import java.io.OutputStream;
import java.net.Socket;

import models.BootstrapState;


public class BootstrapTalker extends Thread {
    public boolean forwardServerMessageToClient;
    BootstrapState state;
    public String targetHostname;
    int port;
    String myHostname;

    public boolean sendJoinResponse = false;

    public boolean updatePredecessorNeighbors = false;

    public boolean updateSuccessorNeighbors = false;

    public boolean forwardClientMessageToServer = false;



    public BootstrapTalker(BootstrapState s, String targetHostname, String myHostname, int port) {
        this.state = s;
        this.targetHostname = targetHostname;
        this.port = port;
        this.myHostname = myHostname;    }


    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = new Socket(targetHostname, port);

                OutputStream outputStream = socket.getOutputStream();

                if (sendJoinResponse) {
                    String message = "{id: 0, message:JOIN_RESPONSE, predecessor: " + state.predecessor + ", successor: " + state.successor + "}";

                    System.out.println("Sending JOIN_RESPONSE to server: " + message);
                    byte[] messageBytes = message.getBytes();
                    outputStream.write(messageBytes);
                    outputStream.flush();
                    sendJoinResponse = false;
                }

                if(updatePredecessorNeighbors) {
                    String message = "{id: 0, message:UPDATE_NEIGHBORS, previous: " + state.predecessorPrev + ", next: " + state.predecessorNext + "}";
                    System.out.println("Sending UPDATE_NEIGHBORS to " + targetHostname + ": " + message);
                    byte[] messageBytes = message.getBytes();
                    outputStream.write(messageBytes);
                    outputStream.flush();
                    updatePredecessorNeighbors = false;
                }

                if(updateSuccessorNeighbors) {
                    String message = "{id: 0, message:UPDATE_NEIGHBORS, previous: " + state.successorPrev + ", next: " + state.successorNext + "}";
                    System.out.println("Sending UPDATE_NEIGHBORS to " + targetHostname + ": " + message);
                    byte[] messageBytes = message.getBytes();
                    outputStream.write(messageBytes);
                    outputStream.flush();
                    updateSuccessorNeighbors = false;
                }

                if(forwardClientMessageToServer) {
                    String message = state.clientRequest;
                    System.out.println("Sending STORE_REQUEST to " + targetHostname + ": " + message);
                    byte[] messageBytes = message.getBytes();
                    outputStream.write(messageBytes);
                    outputStream.flush();
                    forwardClientMessageToServer = false;
                }

                if(forwardServerMessageToClient) {
                    String message = state.messageToForward;
                    System.out.println("Sending SERVICE message to " + targetHostname + ": " + message);
                    byte[] messageBytes = message.getBytes();
                    outputStream.write(messageBytes);
                    outputStream.flush();
                    forwardServerMessageToClient = false;
                }

                sleep(1);

                // Close the socket
                socket.close();
            } catch (Exception e) {
                System.out.println("Exception in BootstrapTalker: " + e.getMessage());
                System.exit(0);
            }
        }
    }

}


