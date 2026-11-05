class TRooms {
    Semaphore s = new Semaphore(2);

    public boolean tryOcc(long ms) {
        return s.tryAcq(ms);
    }

    public void free() {
        s.rel();
    }
}