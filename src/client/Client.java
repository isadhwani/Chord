package client;

import messaging.ClientListener;
import messaging.ClientTalker;
import models.ClientState;
import server.Utils;
public class Client {
    public static void main(String[] args) {
        String bootstrap = "";
        String testcase = "";
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
            } else if ("-t".equals(args[i]) && i + 1 < args.length) {
                testcase = args[i + 1];
            }
        }

        ClientState state = new ClientState();
        ClientListener listen = new ClientListener(state, "client", bootstrap, Utils.CLIENT_LISTEN_PORT);
        ClientTalker talker = new ClientTalker(state, bootstrap, "client", Utils.CLIENT_TALK_PORT);

        Utils.sleep(delay * 1000);
        System.out.println("Starting client on testcase : " + testcase);
        listen.start();
        talker.start();

        switch(testcase) {
            case "3":
                System.out.println("Executing testcase3");
                state.objectIDToStore = 102; // expected to store at peer 126
                state.clientToStoreAt = 4;
                talker.sendStore = true;

                break;
            case "4":
                System.out.println("Executing testcase4");
                state.objectIDToRetrieve = 7; // expected at server 10
                state.clientIDToRetrieve = 2; // expected to print everything from client 1 (6)
                talker.sendRetrieve = true;
                break;
            case "5":
                state.objectIDToRetrieve = 7; // expected at server 10
                state.clientIDToRetrieve = 1; // expected to print everything from client 1 (6)
                talker.sendRetrieve = true;
                break;
            default:
                System.out.println("Invalid testcase");
                System.exit(0);
            //Do some stuff

        }



    }
}
