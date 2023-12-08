package messaging;

import models.BootstrapState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class BootstrapListener extends Thread {
    BootstrapState state;
    String myHostname;
    String targetHostname;
    int port;


    public BootstrapListener(BootstrapState s, String myHostname, String targetHostname, int port) {
        this.port = port;
        this.myHostname = myHostname;
        this.state = s;
        this.targetHostname = targetHostname;
    }

    @Override
    public void run() {
//        System.out.println("Starting listener from " + targetHostname + " on port " + port);
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            //System.out.println("Server is listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                //System.out.println("Client connected: " + clientSocket.getInetAddress().getHostName());

                // Create a new thread to handle the client communication
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


    private void handleClient(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String receivedMessages;
            while ((receivedMessages = reader.readLine()) != null) {
                //System.out.println("Received from " + clientSocket.getInetAddress().getHostName() + ": " + receivedMessages);
                System.out.println("Received: " + receivedMessages);

                String[] messages = receivedMessages.split("\\}\\{");

                // if multiple messages are received at once, iterate through them
                for (String message : messages) {

                    Map<String, String> decoded = decodeJSON(message);

                    String msgType = decoded.get("message");

                    if (msgType.equals("JOIN_REQUEST")) {
                       // System.out.println("Received join request from " + decoded.get("hostname"));
                        state.receivedJoinRequest = true;
                        state.joinRequesterIndex = Integer.parseInt(decoded.get("id"));
                    } else if (msgType.equals("client") ) {//&& decoded.get("operationType").equals("STORE")) {
                        state.receivedClientRequest = true;
                        state.clientRequest = message;
                    } else if(msgType.equals("service")) {
                        state.forwardToClient = true;
                        state.messageToForward = message;
                    }

                }
                sleep(1);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public static Map<String, String> decodeJSON(String jsonString) {
        Map<String, String> resultMap = new HashMap<>();

        // Remove curly braces from the JSON string
        jsonString = jsonString.substring(1, jsonString.length() - 1);

        // Split the string into key-value pairs
        String[] keyValuePairs = jsonString.split(",");

        for (String pair : keyValuePairs) {
            // Split each pair into key and value
            String[] entry = pair.split(":");

            // Trim whitespace from key and value
            String key = entry[0].trim();
            String value = entry[1].trim();

            String stringWithoutSpaces = value.replaceAll("\\s", "");

            resultMap.put(key, stringWithoutSpaces);
        }

        return resultMap;
    }
}


