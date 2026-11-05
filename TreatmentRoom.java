/** The department-wide pool of two treatment rooms. */
public class TreatmentRoom {
    private final Semaphore availableRooms = new Semaphore(2);

    public boolean acquireWithin(long timeoutMillis) throws InterruptedException {
        return availableRooms.tryAcquire(timeoutMillis);
    }

    public void release() {
        availableRooms.release();
    }
}
