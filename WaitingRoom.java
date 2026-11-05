public class WaitingRoom {

    private final Patient[] buffer;

    private int inIndex = 0;
    private int outIndex = 0;

    private volatile boolean closed = false;

    private final Semaphore mutex = new Semaphore(1);
    private final Semaphore empty;
    private final Semaphore full = new Semaphore(0);

    public WaitingRoom(int capacity) {

        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Waiting room capacity must be positive."
            );
        }

        buffer = new Patient[capacity];
        empty = new Semaphore(capacity);
    }

    // Patient or Doctor adds a patient to the waiting room.
    public void put(Patient patient) throws InterruptedException {

        empty.acquire(
                "The waiting room is full.\nP"
                + patient.getPatientId()
                + " is waiting outside for an available seat."
        );

        boolean inserted = false;

        try {
            mutex.acquire();

            try {
                if (closed) {
                    throw new IllegalStateException(
                            "Waiting room is closed."
                    );
                }

                buffer[inIndex] = patient;

                inIndex = (inIndex + 1) % buffer.length;

                inserted = true;

                Hospital.log(
                        "P" + patient.getPatientId()
                        + " entered the waiting room."
                );

            } finally {
                mutex.release();
            }

        } finally {
            // Return the empty slot if insertion did not happen.
            if (!inserted) {
                empty.release();
            }
        }

        full.release();
    }

    // Doctor takes the next patient.
    // Returns null when the room is closed and empty.
    public Patient take() throws InterruptedException {

        full.acquire();

        boolean tokenHandled = false;

        try {
            mutex.acquire();

            try {
                Patient patient = buffer[outIndex];

                if (patient == null) {

                    if (closed) {
                        tokenHandled = true;
                        return null;
                    }

                    throw new IllegalStateException(
                            "Waiting room buffer is inconsistent."
                    );
                }

                buffer[outIndex] = null;

                outIndex = (outIndex + 1) % buffer.length;

                tokenHandled = true;

                empty.release();

                return patient;

            } finally {
                mutex.release();
            }

        } finally {
            // Restore the full permit if taking was interrupted.
            if (!tokenHandled) {
                full.release();
            }
        }
    }

    // Wake all doctors so they can terminate.
    public synchronized void close(int numberOfDoctors) {

        if (closed) {
            return;
        }

        closed = true;

        for (int i = 0; i < numberOfDoctors; i++) {
            full.release();
        }
    }
}