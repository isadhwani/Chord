package messaging;

public class BootstrapConnection {
    public BootstrapListener listener;
    public BootstrapTalker talker;

    public BootstrapConnection(BootstrapTalker talk, BootstrapListener listen) {
        this.talker = talk;
        this.listener = listen;
    }
}
