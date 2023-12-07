package messaging;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import models.StateValues;

public class TCPTalker extends Thread {

    public StateValues state;
    public String targetHostname;
    int port;
    String myHostname;

    public boolean close = false;


    public boolean sendJoinRequest = false;

    public boolean sendPing = false;



    public TCPTalker(StateValues s, String targetHostname, String myHostname, int port) {
        this.state = s;
        this.targetHostname = targetHostname;
        this.port = port;
        this.myHostname = myHostname;
    }


    @Override
    public void run() {
        while (true) {
            if(close) {
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

                    System.out.println("Sending PING to server: " + message);
                    byte[] messageBytes = message.getBytes();
                    outputStream.write(messageBytes);
                    outputStream.flush();
                    sendPing = false;
                    sleep(1);
                }
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


