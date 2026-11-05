
// WRoom: the waiting room, implemented as a Bounded Buffer
// (circular array + 3 semaphores: mutex, empty, full).
// Producers: Patient threads (and Doctors when they send a patient back) call in(p).
// Consumers: Doctor threads call out().

public class WRoom {
 
    private final Patient[] buffer;   // circular array, one slot = one seat
    private final int capacity;
    private int inIndex = 0;          // next free slot (producer side)
    private int outIndex = 0;         // next slot to take from (consumer side)
 
    private final Semaphore mutex;    // mutual exclusion on buffer/indices
    private final Semaphore empty;    // number of free seats
    private final Semaphore full;     // number of patients ready to be treated
 
    public WRoom(int capacity) {
        this.capacity = capacity;
        this.buffer = new Patient[capacity];
        this.mutex = new Semaphore(1);
        this.empty = new Semaphore(capacity);
        this.full = new Semaphore(0);
    }
 

    public void in(Patient p) {
        // Prints "full / waiting outside" only if there is no free seat, then waits.
        empty.acq("The waiting room is full.\n" + p + " is waiting outside for an available seat.");
 
        mutex.acq();
        buffer[inIndex] = p;
        inIndex = (inIndex + 1) % capacity;
        System.out.println(p + " entered the waiting room.");
        mutex.rel();
 
        full.rel();   // a patient is now available for the doctors
    }
 
  
    public Patient out() {
        full.acq();   // wait for a patient
 
        mutex.acq();
        Patient p = buffer[outIndex];
        buffer[outIndex] = null;
        outIndex = (outIndex + 1) % capacity;
        mutex.rel();
 
        empty.rel();  // a seat is now free
        return p;
    }
}
