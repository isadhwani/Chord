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

        BootstrapListener clientListen = new BootstrapListener(s, myHostname, "client", Utils.CLIENT_TALK_PORT);
        BootstrapTalker clientTalker = new BootstrapTalker(s, "client", myHostname, Utils.CLIENT_LISTEN_PORT);

        clientListen.start();

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
                if(predecessorIndex < 0 || successorIndex < 0) {
                    // TODO: Find what occasionally causes this
                    System.out.println("Something has gone horribly wrong, please restart the whole program...");
                    System.out.println("predecessorIndex: " + predecessorIndex);
                    System.out.println("successorIndex: " + successorIndex);
                    System.out.println("index: " + index);
                    System.out.println("Join requester index: " + s.joinRequesterIndex);
                    System.exit(0);
                }

                s.predecessor = currentRing.get(predecessorIndex);
                s.successor = currentRing.get(successorIndex);

                //System.out.println("predecessorIndex for peer " + s.joinRequesterIndex + ": " + s.predecessor);
                //System.out.println("successorIndex for peer " + s.joinRequesterIndex + ": " + s.successor);

                if(s.predecessor != s.joinRequesterIndex) {
                    // Update predecessor's neighbors
                    s.predecessorPrev = currentRing.get((predecessorIndex - 1 + currentRing.size()) % currentRing.size());
                    s.predecessorNext = currentRing.get((predecessorIndex + 1) % currentRing.size());
                    getTalker(connections, s.predecessor).updatePredecessorNeighbors = true;
                }
                if(s.predecessor != s.joinRequesterIndex && s.successor != s.predecessor) {
                    // Update successor's neighbors
                    s.successorPrev = currentRing.get((successorIndex - 1 + currentRing.size()) % currentRing.size());
                    s.successorNext = currentRing.get((successorIndex + 1) % currentRing.size());
                    getTalker(connections, s.successor).updateSuccessorNeighbors = true;
                }

                startTalker(connections, s.joinRequesterIndex);
                s.receivedJoinRequest = false;

                System.out.println("CURRENT RING: " + currentRing);
            } if(s.receivedClientRequest) {
                //clientTalker.start();
                // Always send store request to first peer. This peer will always be n1
                connections.get(0).talker.forwardClientMessageToServer = true;
                s.receivedClientRequest = false;

            } else if(s.forwardToClient) {
                clientTalker.start();
                clientTalker.forwardServerMessageToClient = true;
                s.forwardToClient = false;
            }
            u.sleep(1);
        }
    }

    public static void startTalker(List<BootstrapConnection> connections, int joinRequesterIndex) {
        for(BootstrapConnection conn : connections) {
            //System.out.println("conn.talker.targetHostname: " + conn.talker.targetHostname);
            if(conn.talker.targetHostname.equals("n" + joinRequesterIndex)) {
                //System.out.println("Starting talker to " + conn.talker.targetHostname);
                if(!conn.talker.isAlive())
                    try {
                        conn.talker.start();
                    } catch(IllegalThreadStateException e) {
                        System.out.println("Talker already started");
                    }
                conn.talker.sendJoinResponse = true;
            }
        }
    }

    public static BootstrapTalker getTalker(List<BootstrapConnection> connections, int targetID) {
        for(BootstrapConnection conn : connections) {
            //System.out.println("conn.talker.targetHostname: " + conn.talker.targetHostname);
            if(conn.talker.targetHostname.equals("n" + targetID)) {
                return conn.talker;
            }
        }
        throw new IllegalStateException("No talker found for " + targetID);
    }

}
