public class WaitingRoom {
    private final Patient[] buffer;
    private int inIndex;
    private int outIndex;
    private boolean closed;

    private final Semaphore mutex = new Semaphore(1);
    private final Semaphore empty;
    private final Semaphore full = new Semaphore(0);

    public WaitingRoom(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Waiting room capacity must be positive.");
        }
        buffer = new Patient[capacity];
        empty = new Semaphore(capacity);
    }

    public void put(Patient patient) throws InterruptedException {
        empty.acquire("The waiting room is full.\nP" + patient.getPatientId()
                + " is waiting outside for an available seat.");
        mutex.acquire();
        try {
            if (closed) {
                empty.release();
                throw new IllegalStateException("Cannot add a patient after the waiting room is closed.");
            }
            buffer[inIndex] = patient;
            inIndex = (inIndex + 1) % buffer.length;
            Hospital.log("P" + patient.getPatientId() + " entered the waiting room.");
        } finally {
            mutex.release();
        }
        full.release();
    }

    /** Returns null after close once all queued patients have been removed. */
    public Patient take() throws InterruptedException {
        full.acquire();
        mutex.acquire();
        try {
            if (closed && buffer[outIndex] == null) return null;
            Patient patient = buffer[outIndex];
            if (patient == null) {
                if (closed) return null;
                throw new IllegalStateException("Waiting room buffer is inconsistent.");
            }
            buffer[outIndex] = null;
            outIndex = (outIndex + 1) % buffer.length;
            empty.release();
            return patient;
        } finally {
            mutex.release();
        }
    }

    public void close(int numberOfDoctors) {
        synchronized (this) {
            closed = true;
        }
        for (int i = 0; i < numberOfDoctors; i++) full.release();
    }
}
