
package com.atc.simulation;

public class SimulationClock implements Runnable {

    private long elapsedSeconds;
    private volatile boolean running;
    private boolean paused;

    public SimulationClock() {
        elapsedSeconds = 0;
        running = false;
        paused = false;
    }

    @Override
    public void run() {

        running = true;

        while (running) {
            try {
                synchronized (this) {
                    while (paused && running) {
                        wait();
                    }
                }

                if (!running) {
                    break;
                }

                Thread.sleep(1000);

                synchronized (this) {
                    if (running && !paused) {
                        elapsedSeconds++;
                        System.out.println("Simulation Time: " + elapsedSeconds + " seconds");
                    }
                }

            } catch (InterruptedException e) {
                running = false;
                Thread.currentThread().interrupt();
            }
        }
    }

    public synchronized void awaitIfPaused() throws InterruptedException {
        while (paused && running) {
            wait();
        }
    }

    public synchronized long getElapsedSeconds() {
        return elapsedSeconds;
    }

    public synchronized void pauseClock() {
        if (running) {
            paused = true;
        }
    }

    public synchronized void resumeClock() {
        paused = false;
        notifyAll();
    }

    public synchronized boolean isPaused() {
        return paused;
    }

    public void stopClock() {
        running = false;

        synchronized (this) {
            paused = false;
            notifyAll();
        }
    }

    public boolean isRunning() {
        return running;
    }
}
