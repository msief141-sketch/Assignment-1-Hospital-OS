/** A small interruptible counting semaphore used by the simulation. */
public class Semaphore {
    private int permits;

    public Semaphore() {
        this(0);
    }

    public Semaphore(int initialPermits) {
        if (initialPermits < 0) {
            throw new IllegalArgumentException("Semaphore permits cannot be negative.");
        }
        permits = initialPermits;
    }

    public synchronized void acquire() throws InterruptedException {
        while (permits == 0) {
            wait();
        }
        permits--;
    }

    public synchronized void acquire(String waitingMessage) throws InterruptedException {
        if (permits == 0 && waitingMessage != null) {
            Hospital.log(waitingMessage);
        }
        acquire();
    }

    public synchronized boolean tryAcquire(long timeoutMillis) throws InterruptedException {
        if (timeoutMillis < 0) {
            throw new IllegalArgumentException("Timeout cannot be negative.");
        }
        long deadline = System.nanoTime() + timeoutMillis * 1_000_000L;
        long remaining = timeoutMillis;
        while (permits == 0) {
            if (remaining <= 0) {
                return false;
            }
            long millis = remaining / 1_000_000L;
            int nanos = (int) (remaining % 1_000_000L);
            wait(millis, nanos);
            remaining = (deadline - System.nanoTime()) / 1_000_000L;
        }
        permits--;
        return true;
    }

    public synchronized void release() {
        permits++;
        notifyAll();
    }
}
