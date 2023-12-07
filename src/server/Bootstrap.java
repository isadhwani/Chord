package server;

import messaging.*;
import models.BootstrapState;
import java.util.*;

public class Bootstrap {

    public static void main(String[] args) {
//        System.out.println("CMD ARGS: " + Arrays.toString(args));
        Utils u = new Utils();

        /**
         * Start listeners for all peers with possible IDs 1, 5, 10, 50, 66, 100, 126
         * Keep track of sorted ring
         */

        int myPeerIndex = 0;
        final int START_PORT = 5000;
        int[] possibleIds = {1, 5, 10, 50, 66, 100, 126};
        List<Integer> currentRing = new ArrayList<Integer>();
        BootstrapState s = new BootstrapState();
        String myHostname = "bootstrap";
        List<BootstrapConnection> connections = new ArrayList<BootstrapConnection>();


        for(int i : possibleIds) {
            int talkerPort = myPeerIndex * 10 + START_PORT + i;
            int listenPort = i * 10 + START_PORT + myPeerIndex;

            BootstrapListener listen = new BootstrapListener(s, myHostname, "n" + i, listenPort);
            BootstrapTalker talk = new BootstrapTalker(s, "n" + i, myHostname, talkerPort);
            BootstrapConnection conn = new BootstrapConnection(talk, listen);
            listen.start();
            connections.add(conn);
        }

        while(true) {

            if(s.receivedJoinRequest) {
                //System.out.println("Received join request from n" + s.joinRequesterIndex);
                //System.out.println("Adding n" + s.joinRequesterIndex + " to ring");
                // Start talker to ID of join requester
                currentRing.add(s.joinRequesterIndex);
                Collections.sort(currentRing);
                int index = Collections.binarySearch(currentRing, s.joinRequesterIndex);

                int predecessorIndex = (index - 1 + currentRing.size()) % currentRing.size();
                int successorIndex = (index + 1) % currentRing.size();

                s.predecessor = currentRing.get(predecessorIndex);
                s.successor = currentRing.get(successorIndex);
                startTalker(connections, s.joinRequesterIndex);
                s.receivedJoinRequest = false;

                System.out.println("Current ring: " + currentRing);
            }
            u.sleep(1);
        }
    }

    public static void startTalker(List<BootstrapConnection> connections, int joinRequesterIndex) {
        for(BootstrapConnection conn : connections) {
            //System.out.println("conn.talker.targetHostname: " + conn.talker.targetHostname);
            if(conn.talker.targetHostname.equals("n" + joinRequesterIndex)) {
                System.out.println("Starting talker to " + conn.talker.targetHostname);
                conn.talker.start();
                conn.talker.sendJoinResponse = true;
            }
        }
    }

}
