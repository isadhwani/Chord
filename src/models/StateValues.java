package models;

import java.util.concurrent.Semaphore;

public class StateValues {
    public boolean joinedRing = false;
    public int id;

    public int predecessor = -1;
    public int successor = -1;

    public boolean onlyPeer = false;

    public boolean updateRingConnections = false;
    public int objectIDToStore;
    public boolean storeData = false;
    public int clientToStoreAt;
    public String messageToForward;
    public boolean forwardMessage = false;
    private Semaphore mutex = new Semaphore(1);


    public int getX() {
        try {
            mutex.acquire();
            return 1;
        } catch (InterruptedException e) {
            // exception handling code
            throw new IllegalStateException("wait for release");
        } finally {
            mutex.release();
        }
    }
}
