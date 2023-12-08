This program is a simplified version of the CHORD distributed hash table protocol. It is a peer-to-peer protocol that allows for the storage and retrieval of key-value pairs. The protocol is designed to be fault tolerant and scalable.


## How to run
This system consists of 3 different programs: a client, a centralized bootstrap server and several decentralized peers.
Each docker image can be built with the following docker command:   
```docker build . -f BootstrapDockerfile -t prj5-bootstrap && docker build . -f PeerDockerfile -t prj5-peer && docker build . -f ClientDockerfile -t prj5-client```

## Testcases
I've included 5 testcases in the `docker-compose-testcases-and-hostsfiles-lab5` folder. Each testcase has a `docker-compose.yml` file that can be run with the following command:   
```docker compose -f docker-compose-testcases-and-objects-lab5/docker-compose-testcase-3.yml up```

Additionally, there are text files representing preexisting data at each server. 
### Testcase 1
This first testcase demonstrates that the ring is properly constructed by the bootstrap server. Each peer joins the rings in ascending numerical order and is assigned a successor and predecessor.


### Testcase 2
The second testcase demonstrates that the ring is maintained when peers join in a random order.  When a peer joins the ring, it's neighbors are both informed of the update. When receiving an update, a peer establishes connections with its new neighbors. 

### Testcase 3
Testcase 3 is a store request. The client sends a clientID and objectID to the bootstrap server for it to forward a store request through the ring. The bootstrap server forwards the request to the first peer in the ring and the request is passed until it finds a peer with an ID greater than or equal to the objectID of the object to store. 

This testcase is hardcoded to store objectID `102` at peer `n126` with clientID `4`. It can be modified by adjusting the client code. 

### Testcase 4
Testcase 4 is a retrieve request. The client sends a clientID and objectID to the bootstrap server for it to forward a retrieve request through the ring. The bootstrap server forwards the request to the first peer in the ring and the request is passed until it finds a peer with an ID greater than or equal to the objectID of the object to retrieve. Upon finding this peer, it will iterate through its stored values and search for an entry with a matching objectID and clientID to that of the request. In a full chord system, this lookup would return the file stored with the objectID.

This testcase is hardcoded to retrieve objectID `7` at peer `n10` with clientID `2`. It can be modified by adjusting the client code.

### Testcase 5
Testcase 5 is a retrieve request for a non-existent object. The client sends a clientID and objectID to the bootstrap server for it to forward a retrieve request through the ring. The bootstrap server forwards the request to the first peer in the ring and the request is passed until it finds a peer with an ID greater than or equal to the objectID of the object to retrieve. Upon finding this peer, it will iterate through its stored values and search for an entry with a matching objectID and clientID to that of the request. When no matching entry is found, the peer will return a `NOT_FOUND` message to the bootstrap server who will forward this message to the client.

This testcase is hardcoded to retrieve objectID `7` at peer `n10` with clientID `1`. This data does not exisit in the predefined object store. It can be modified by adjusting the client code.