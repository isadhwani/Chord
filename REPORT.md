My chord implementation relies on the simplified version of chord described in the assignment description. Specifically, it does not actually store anything nor is the ring maintained in the way the algorithm was written. All ring updates are handled by the bootstrap server.


The biggest issue in my code is thread safety in my state classes. While I've carefully considered the control flow of the program and data is not updated at the same time by multiple places, my data is clearly not protected, and it could cause expected errors.

I could fix this by making all the data in my state classes private and add getters/setters for each field. I'd add a mutex lock only give read/write access to one thread at a time. I'd also write an access method for requesting data when another thread has the lock. It will wait until the lock becomes available and returns the requested data. 
