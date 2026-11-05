public class Semaphore {
    protected int value;

    public Semaphore() {
        this.value = 0;
    }

    public Semaphore(int initial) {
        this.value = initial;
    }

    public synchronized void acquire() throws InterruptedException {
        while (value <= 0) {
            wait();
        }
        value--;
    }

    public synchronized void acquire(String waitingMessage) throws InterruptedException {
        if (value <= 0 && waitingMessage != null) {
            Hospital.log(waitingMessage);
        }
        while (value <= 0) {
            wait();
        }
        value--;
    }

    public synchronized boolean tryAcquire(long timeoutMillis) throws InterruptedException {
        long end = System.currentTimeMillis() + timeoutMillis;
        while (value <= 0) {
            long rem = end - System.currentTimeMillis();
            if (rem <= 0) {
                return false;
            }
            wait(rem);
        }
        value--;
        return true;
    }

    public synchronized void release() {
        value++;
        notifyAll();
    }
}