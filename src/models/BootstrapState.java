package models;

import java.util.concurrent.Semaphore;

public class BootstrapState {
    public boolean receivedStoreRequest = false;
    public String clientRequest;
    private Semaphore mutex = new Semaphore(1);
    public boolean receivedJoinRequest = false;
    public int joinRequesterIndex;

    public int predecessor;
    public int successor;

    public int predecessorPrev;
    public int predecessorNext;

    public int successorPrev;
    public int successorNext;

    public boolean getReceivedJoinRequest() {
        try {
            mutex.acquire();
            return this.receivedJoinRequest;
        } catch (InterruptedException e) {
            // exception handling code
            throw new IllegalStateException("wait for release");
        } finally {
            mutex.release();
        }
    }

}
