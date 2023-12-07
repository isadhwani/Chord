package messaging;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import models.BootstrapState;


public class BootstrapTalker extends Thread {
    BootstrapState state;
    public String targetHostname;
    int port;
    String myHostname;

    public boolean sendJoinResponse = false;


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


