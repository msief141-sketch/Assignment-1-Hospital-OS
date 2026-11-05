class Semaphore {
    protected int value = 0;

    public Semaphore() {
        value = 0;
    }

    public Semaphore(int initial) {
        value = initial;
    }

    public synchronized void acq() {
        while (value <= 0) {
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }
        value--;
    }

    public synchronized void acq(String msg) {
        if (value <= 0 && msg != null) {
            System.out.println(msg);
        }
        while (value <= 0) {
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }
        value--;
    }

    public synchronized boolean tryAcq(long ms) {
        long end = System.currentTimeMillis() + ms;
        while (value <= 0) {
            long rem = end - System.currentTimeMillis();
            if (rem <= 0) {
                return false;
            }
            try {
                wait(rem);
            } catch (InterruptedException ex) {
            }
        }
        value--;
        return true;
    }

    public synchronized void rel() {
        value++;
        notifyAll();
    }
}