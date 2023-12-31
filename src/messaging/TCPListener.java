package messaging;

import models.StateValues;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class TCPListener extends Thread {
    StateValues state;
    String myHostname;
    public String targetHostname;
    int port;

    public boolean close = false;


    public TCPListener(StateValues s, String myHostname, String targetHostname, int port) {
        this.port = port;
        this.myHostname = myHostname;
        this.state = s;
        this.targetHostname = targetHostname;
    }

    @Override
    public void run() {
        //System.out.println("Starting listener from " + targetHostname + " on port " + port);
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
                if(close) {
                    System.out.println("closing listener on " + targetHostname + " on port " + port);
                    System.exit(0);
                }
                //System.out.println("Received from " + clientSocket.getInetAddress().getHostName() + ": " + receivedMessages);
                System.out.println("Received: " + receivedMessages);

                String[] messages = receivedMessages.split("\\}\\{");

                // if multiple messages are received at once, iterate through them
                for (String message : messages) {

                    Map<String, String> decoded = decodeJSON(message);

                    String msgType = decoded.get("message");

                    if (msgType.equals("JOIN_RESPONSE")) {
                        //System.out.println("Received proposal, determining if i can send prepare ack...");
                        int predecessor = Integer.parseInt(decoded.get("predecessor"));
                        int successor = Integer.parseInt(decoded.get("successor"));
                        state.predecessor = predecessor;
                        state.successor = successor;
                        state.joinedRing = true;
                        state.updateRingConnections = true;
                        System.out.println("Joined ring with predecessor: " + predecessor + ", successor: " + successor );
                    } else if (msgType.equals("UPDATE_NEIGHBORS")) {
                        int previous = Integer.parseInt(decoded.get("previous"));
                        int next = Integer.parseInt(decoded.get("next"));
                        state.predecessor = previous;
                        state.successor = next;
                        state.updateRingConnections = true;
                        System.out.println("Updated ring with predecessor: " + previous + ", successor: " + next );
                    } else if (msgType.equals("client")) {
                        if(decoded.get("operationType").equals("STORE")) {
                            if(Integer.parseInt(decoded.get("objectID")) <= state.id) {
                                state.objectID = Integer.parseInt(decoded.get("objectID"));
                                state.clientID = Integer.parseInt(decoded.get("clientID"));
                                state.storeData = true;
                            } else {
                                state.messageToForward = message;
                                state.forwardMessage = true;

                            }
                        } else if(decoded.get("operationType").equals("RETRIEVE")) {
                            if(Integer.parseInt(decoded.get("objectID")) <= state.id) {
                                state.objectID = Integer.parseInt(decoded.get("objectID"));
                                state.clientID = Integer.parseInt(decoded.get("clientID"));
                                state.lookupData = true;
                            } else {
                                state.messageToForward = message;
                                state.forwardMessage = true;
                            }
                        }

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


