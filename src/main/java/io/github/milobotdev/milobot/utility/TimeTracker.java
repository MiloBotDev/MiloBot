package io.github.milobotdev.milobot.utility;

public class TimeTracker {

    private long startTime;
    private long endTime;
    private boolean isStarted;
    private final int durationSeconds;
    private final boolean isDurationSet;

    public TimeTracker() {
        this.startTime = 0;
        this.endTime = 0;
        this.isStarted = false;
        this.durationSeconds = 0;
        this.isDurationSet = false;
    }

    public TimeTracker(int durationSeconds) {
        this.startTime = 0;
        this.endTime = 0;
        this.isStarted = false;
        this.durationSeconds = durationSeconds;
        this.isDurationSet = true;
    }

    public void start() {
        if (isStarted) {
            throw new IllegalStateException("Timer is already started");
        } else {
            startTime = System.currentTimeMillis();
            isStarted = true;
        }
    }

    public void stop() {
        if (!isStarted) {
            throw new IllegalStateException("Timer is not started");
        } else {
            endTime = System.currentTimeMillis();
            isStarted = false;
        }
    }

    public void reset() {
        startTime = 0;
        endTime = 0;
        isStarted = false;
    }

    public long getElapsedTime() {
        if (isStarted) {
            return System.currentTimeMillis() - startTime;
        } else {
            return endTime - startTime;
        }
    }

    public long getElapsedTimeSecs() {
        if (isStarted) {
            return (System.currentTimeMillis() - startTime) / 1000;
        } else {
            return (endTime - startTime) / 1000;
        }
    }

    public long getElapsedTimeMins() {
        if (isStarted) {
            return (System.currentTimeMillis() - startTime) / 60000;
        } else {
            return (endTime - startTime) / 60000;
        }
    }

    public boolean isTimeSecondsPastDuration() {
        if(isDurationSet) {
            System.out.println(getElapsedTimeSecs() >= durationSeconds);
            return getElapsedTimeSecs() >= durationSeconds;
        } else {
            throw new IllegalStateException("Duration is not set");
        }
    }

    public long timeSecondsTillDuration() {
        if(isDurationSet) {
            return durationSeconds - getElapsedTimeSecs();
        } else {
            throw new IllegalStateException("Duration is not set");
        }
    }

}
