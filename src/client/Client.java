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

        System.out.println("Delaying for " + delay * 1000 + "milis...");
        Utils.sleep(delay * 1000);
        System.out.println("Starting client on testcase : " + testcase);
        listen.start();
        talker.start();

        switch(testcase) {
            case "3":
                System.out.println("Starting client");
                state.objectIDToStore = 123;
                state.clientToStoreAt = 12; // Expected to store at server 50
                talker.sendStore = true;

                break;
            case "4":
                break;
            case "5":
                break;
            default:
                System.out.println("Invalid testcase");
                System.exit(0);
            //Do some stuff

        }



    }
}
