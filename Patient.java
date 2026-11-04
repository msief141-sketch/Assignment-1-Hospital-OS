// Patient.java
// this is the producer part of the ER simulation
// every patient is a thread, once it starts the patient "arrives" at the hospital
// and then tries to get into the waiting room using in()
// if the room is full the in() method will block him (he waits outside) untill a seat is free
// after that its the doctors job to pick him up so the patient thread is basically done

public class Patient extends Thread {

    private int id;              // patient number (1, 2, 3 ...)
    private WaitingRoom wRoom;   // shared waiting room (the bounded buffer)

    public Patient(int id, WaitingRoom wRoom) {
        this.id = id;
        this.wRoom = wRoom;
        // thread name will be P1, P2 ... so its easy to print it anywhere
        setName("P" + id);
    }

    public int getPatientId() {
        return id;
    }

    // so if someone prints the patient object directly it shows P1 not Patient@1b6d3586
    @Override
    public String toString() {
        return getName();
    }

    @Override
    public void run() {
        System.out.println(getName() + " arrived.");

        // try to enter the waiting room
        // (the waiting/full message is printed inside in() itself)
        try {
            wRoom.in(this);
        } catch (Exception e) {
            System.out.println(getName() + " got interrupted while trying to enter the waiting room");
        }
        // now the patient is sitting in the waiting room
        // a doctor will take him with out() and treat him, nothing else to do here
    }
}
