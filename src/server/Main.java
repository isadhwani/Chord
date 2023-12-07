package server;

import messaging.TCPConnection;
import messaging.TCPListener;
import messaging.TCPTalker;
import models.StateValues;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        System.out.println("CMD ARGS: " + Arrays.toString(args));
        int[] possiblePeerIds = {1, 5, 10, 50, 66, 100, 126};

        Utils u = new Utils();

        String bootstrap = "";
        String objects = "";
        int delay = -1;

        for (int i = 0; i < args.length; i++) {
            if ("-b".equals(args[i]) && i + 1 < args.length) {
                bootstrap = args[i + 1];
            } else if ("-d".equals(args[i]) && i + 1 < args.length) {
                try {
                    delay = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    // Handle invalid startDelay input
                    System.err.println("Invalid delay value");
                }
            } else if ("-o".equals(args[i]) && i + 1 < args.length) {
                objects = args[i + 1];
            }
        }

        String myHostname = getMyHostname();
        StateValues state = new StateValues();
        int myIndex = Utils.extractNumberFromTarget(myHostname, "n");
        state.id = myIndex;

        int talkerPort = myIndex * 10 + u.START_PORT + 0; // bootstrap server is index 0
        int listenPort = 0 * 10 + u.START_PORT + myIndex;
        TCPListener bootstrapListen = new TCPListener(state, myHostname, bootstrap, listenPort);
        TCPTalker bootstrapTalk = new TCPTalker(state, bootstrap, myHostname, talkerPort);
        TCPConnection bootstrapConn = new TCPConnection(bootstrapTalk, bootstrapListen);


        ArrayList<TCPListener> listeners = new ArrayList<>();
        // Start listeners for all possible peers

        for(int i : possiblePeerIds) {
            int newListenPort = i * 10 + Utils.START_PORT + myIndex;
            TCPListener listen = new TCPListener(state, myHostname, "n" + i, newListenPort);
            listeners.add(listen);
            listen.start();
        }


        TCPTalker predecessorTalk = null;
        TCPListener predecessorListen = null;

        TCPTalker successorTalk = null;
        TCPListener successorListen = null;

        TCPConnection predecessorConn = new TCPConnection(predecessorTalk, predecessorListen);
        TCPConnection successorConn = new TCPConnection(successorTalk, successorListen);



        while(true) {
            if (!state.joinedRing) {
                u.sleep(delay * 1000);
                bootstrapConn.talker.start();
                bootstrapConn.listener.start();
                bootstrapConn.talker.sendJoinRequest = true;
                state.joinedRing = true;
            }
            if(state.updateRingConnections) {
                updateConnections(state, myIndex, predecessorConn, successorConn);
                // Send each neighbor a ping
                System.out.println("Sending pings to neighbors");
                successorConn.talker.sendPing = true;
                predecessorConn.talker.sendPing = true;
            }

            u.sleep(1);
        }
    }

    public static void updateConnections(StateValues state, int myPeerIndex,
                                         TCPConnection predecessorConn, TCPConnection successorConn) {
        // Stop conns if needed
        if(predecessorConn.talker != null) {
            predecessorConn.talker.close = true;
            predecessorConn.listener.close = true;
        }
        if(successorConn.talker != null) {
            successorConn.talker.close = true;
            successorConn.listener.close = true;
        }

        // Start new connections
        int talkerPortPredecessor = myPeerIndex * 10 + Utils.START_PORT + state.predecessor;
        int listenPortPredecessor = state.predecessor * 10 + Utils.START_PORT + myPeerIndex;

        int talkerPortSuccessor = myPeerIndex * 10 + Utils.START_PORT + state.successor;
        int listenPortSuccessor = state.successor * 10 + Utils.START_PORT + myPeerIndex;

        TCPListener predecessorListen = new TCPListener(state, "n" + myPeerIndex, "n" + state.predecessor, listenPortPredecessor);
        TCPTalker predecessorTalk = new TCPTalker(state, "n" + state.predecessor, "n" + myPeerIndex, talkerPortPredecessor);
        predecessorConn = new TCPConnection(predecessorTalk, predecessorListen);

        if(state.predecessor != state.successor && state.predecessor != myPeerIndex) {


        }
        TCPListener successorListen = new TCPListener(state, "n" + myPeerIndex, "n" + state.successor, listenPortSuccessor);
        TCPTalker successorTalk = new TCPTalker(state, "n" + state.successor, "n" + myPeerIndex, talkerPortSuccessor);
        successorConn = new TCPConnection(successorTalk, successorListen);

        System.out.println("Starting talker from " + myPeerIndex + " to " + state.predecessor + " on port " + talkerPortPredecessor);
        System.out.println("Starting listener from " + state.predecessor + " on port " + listenPortPredecessor);

        System.out.println("Starting talker from " + myPeerIndex + " to " + state.successor + " on port " + talkerPortSuccessor);
        System.out.println("Starting listener from " + state.successor + " on port " + listenPortSuccessor);

        predecessorConn.listener.start();
        successorConn.listener.start();

        successorConn.talker.start();
        predecessorConn.talker.start();
    }


    public static String getMyHostname() {
        String ret;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            ret = localHost.getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return ret;
    }

}