import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class Doctor extends Thread {
    private static final long ROOM_TIMEOUT_MS = 3_000;

    private final int doctorId;
    private final WaitingRoom waitingRoom;
    private final TreatmentRoom treatmentRoom;
    private final CountDownLatch allPatientsTreated;

    public Doctor(int doctorId, WaitingRoom waitingRoom,
                  TreatmentRoom treatmentRoom, CountDownLatch allPatientsTreated) {
        super("Doctor-" + doctorId);
        this.doctorId = doctorId;
        this.waitingRoom = waitingRoom;
        this.treatmentRoom = treatmentRoom;
        this.allPatientsTreated = allPatientsTreated;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Patient patient = waitingRoom.take();
                if (patient == null) {
                    break;
                }

                Hospital.log("Doctor " + doctorId + " is assigned to P"
                        + patient.getPatientId() + ".");
                Hospital.log("Doctor " + doctorId + " is trying to acquire a treatment room.");

                boolean acquired = treatmentRoom.acquireWithin(ROOM_TIMEOUT_MS);

                if (!acquired) {
                    Hospital.log("Doctor " + doctorId + " could not acquire a treatment room.");
                    Hospital.log("Doctor " + doctorId + " is sending P"
                            + patient.getPatientId() + " back to the waiting room.");
                    try {
                        waitingRoom.put(patient);
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        Hospital.log("Doctor " + doctorId + " was interrupted while returning P"
                                + patient.getPatientId() + " to the waiting room.");
                        break;
                    }
                    Hospital.log("Doctor " + doctorId + " is now available.");
                    continue;
                }

                try {
                    Hospital.log("Doctor " + doctorId + " acquired a treatment room.");
                    Hospital.log("Doctor " + doctorId + " begins treating P"
                            + patient.getPatientId() + ".");

                    // Random treatment duration: 1–5 seconds.
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1_000, 5_001));

                    Hospital.log("Doctor " + doctorId + " finished treating P"
                            + patient.getPatientId() + ".");
                } finally {
                    treatmentRoom.release();
                    Hospital.log("Doctor " + doctorId + " released the treatment room.");
                }

                Hospital.log("Doctor " + doctorId + " is now available.");
                Hospital.log("P" + patient.getPatientId() + " left.");
                allPatientsTreated.countDown();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Hospital.log("Doctor " + doctorId + " was interrupted.");
        }
    }
}
