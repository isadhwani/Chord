package messaging;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import models.StateValues;

public class TCPTalker extends Thread {

    public StateValues state;
    public String targetHostname;
    public boolean sendFound = false;
    public boolean sendNotFound = false;
    int port;
    String myHostname;

    public boolean close = false;


    public boolean sendJoinRequest = false;

    public boolean sendPing = false;

    public boolean forwardMessage = false;
    public boolean sendObjStored = false;



    public TCPTalker(StateValues s, String targetHostname, String myHostname, int port) {
        this.state = s;
        this.targetHostname = targetHostname;
        this.port = port;
        this.myHostname = myHostname;

    }


    boolean first = true;
    @Override
    public void run() {
//        if(first) {
//            System.out.println("Starting talker to " + targetHostname + " on port " + port);
//        }

        while (true) {
            if(close) {
                System.out.println("Closing tlaker to " + targetHostname + " on port " + port);
                System.exit(0);
            }
            try {
                Socket socket = new Socket(targetHostname, port);

                OutputStream outputStream = socket.getOutputStream();

                if (sendJoinRequest) {

                    String message = "{id: " + state.id + ", message:JOIN_REQUEST, hostname: " + myHostname + "}";

                    System.out.println("Sending JOIN_REQUEST to server: " + message);
                    byte[] messageBytes = message.getBytes();
                    outputStream.write(messageBytes);
                    outputStream.flush();
                    sendJoinRequest = false;
                    sleep(1);
                }

                if(sendPing) {
                	String message = "{id: " + state.id + ", message:PING, hostname: " + myHostname + "}";

                    System.out.println("Sending PING to " + targetHostname + ": " + message);
                    byte[] messageBytes = message.getBytes();
                    outputStream.write(messageBytes);
                    outputStream.flush();
                    sendPing = false;
                    sleep(1);
                }

                if(forwardMessage) {
                    String message = state.messageToForward;

                        System.out.println("Forwarding message to " + targetHostname + ": " + message);
                        byte[] messageBytes = message.getBytes();
                        outputStream.write(messageBytes);
                        outputStream.flush();
                        forwardMessage = false;
                        sleep(1);
                }

                if(sendObjStored) {
                    String message = "{id: " + state.id + ", message:service, status:OBJ_STORED, hostname: " + myHostname + "" +
                            ", objectID: " + state.objectID + ", clientID: " + state.clientID + "}";

                        System.out.println("Sending OBJ_STORED to " + targetHostname + ": " + message);
                        byte[] messageBytes = message.getBytes();
                        outputStream.write(messageBytes);
                        outputStream.flush();
                        sendObjStored = false;
                        sleep(1);
                }

                if(sendFound) {
                    String message = "{id: " + state.id + ", message:service, status:FOUND, hostname: " + myHostname + "" +
                            ", objectID: " + state.objectFound + "}";

                        System.out.println("Sending FOUND to " + targetHostname + ": " + message);
                        byte[] messageBytes = message.getBytes();
                        outputStream.write(messageBytes);
                        outputStream.flush();
                        sendFound = false;
                        sleep(1);
                }

                if(sendNotFound) {
                    String message = "{id: " + state.id + ", message:service, status:NOT_FOUND, hostname: " + myHostname + "" +
                            ", objectID: " + state.objectID + "}";

                        System.out.println("Sending NOT_FOUND to " + targetHostname + ": " + message);
                        byte[] messageBytes = message.getBytes();
                        outputStream.write(messageBytes);
                        outputStream.flush();
                        sendNotFound = false;
                        sleep(1);
                }
                // Close the socket
                socket.close();
            } catch(Exception e) {
                System.out.println("Connection timed out");
                System.exit(0);
            }


        }
    }

}


