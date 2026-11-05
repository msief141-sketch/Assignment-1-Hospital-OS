/** A producer that arrives and attempts to enter the bounded waiting room. */
public class Patient extends Thread {
    private final int patientId;
    private final WaitingRoom waitingRoom;

    public Patient(int patientId, WaitingRoom waitingRoom) {
        super("P" + patientId);
        this.patientId = patientId;
        this.waitingRoom = waitingRoom;
    }

    public int getPatientId() {
        return patientId;
    }

    @Override
    public void run() {
        Hospital.log(getName() + " arrived.");
        try {
            waitingRoom.put(this);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Hospital.log(getName() + " was interrupted while waiting to enter the waiting room.");
        }
    }
}
