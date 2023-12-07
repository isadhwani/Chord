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

        ArrayList<TCPTalker> talkers = new ArrayList<>();
        for(int i : possiblePeerIds) {
            int newTalkerPort = myIndex * 10 + Utils.START_PORT + i;
            TCPTalker talk = new TCPTalker(state, "n" + i, myHostname, newTalkerPort);
            talkers.add(talk);
            //talk.start();
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
                updateConnections(state, myIndex, predecessorConn, successorConn, listeners, talkers);
                // Send each neighbor a ping
                if(!state.onlyPeer) {
                    System.out.println("Sending pings to neighbors");
                    successorConn.talker.sendPing = true;
                    predecessorConn.talker.sendPing = true;
                }
                state.updateRingConnections = false;
            }

            u.sleep(1);
        }
    }

    public static void updateConnections(StateValues state, int myPeerIndex,
                                         TCPConnection predecessorConn, TCPConnection successorConn,
                                         ArrayList<TCPListener> listeners, ArrayList<TCPTalker> talkers) {
        // Stop conns if needed
        if(predecessorConn.talker != null) {
            predecessorConn.talker.close = true;
            predecessorConn.listener.close = true;
        }
        if(successorConn.talker != null) {
            successorConn.talker.close = true;
            successorConn.listener.close = true;
        }

        if(state.predecessor == state.successor && state.successor == myPeerIndex) {
            // I am the only node in the ring
            state.onlyPeer = true;
        }

        System.out.println("Looking for talkers to " + state.predecessor + " and " + state.successor);

        for(TCPListener l : listeners) {
            if(l.targetHostname == "n" + state.predecessor) {
                predecessorConn.listener = l;
            }
            if(l.targetHostname == "n" + state.successor) {
                successorConn.listener = l;
            }
        }

        for(TCPTalker t : talkers) {
           // System.out.println("Comparing " + t.targetHostname + " to " +  "n" + state.predecessor);
            if(t.targetHostname.equals("n" + state.predecessor)) {
                predecessorConn.talker = t;
                System.out.println("Assigning predecespr talker to " + t.targetHostname);
                t.start();
            }
            if(t.targetHostname.equals("n" + state.successor)) {
                System.out.println("Assigning successsor talker  to " + t.targetHostname);

                successorConn.talker = t;
                if(!t.isAlive()) {
                    t.start();
                }
            }
        }

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