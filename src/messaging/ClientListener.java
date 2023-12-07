package messaging;

import models.ClientState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ClientListener extends Thread {
    ClientState state;
    String myHostname;
    public String targetHostname;
    int port;

    public boolean close = false;


    public ClientListener(ClientState s, String myHostname, String targetHostname, int port) {
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

                    }


                }
                sleep(1);
            }

        } catch (Exception e) {
            System.out.println("Exception in ClientListener: " + e.getMessage());
            System.exit(0);
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


