package messaging;

import java.util.List;

/**
 * Represents a Listener and talker for one process to connect to another
 */
public class TCPConnection {
    public TCPListener listener;
    public TCPTalker talker;


    public TCPConnection(TCPTalker talk, TCPListener listen) {
        this.talker = talk;
        this.listener = listen;
    }

}
