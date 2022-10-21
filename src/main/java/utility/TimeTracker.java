package utility;

public class TimeTracker {

    private long startTime;
    private long endTime;
    private boolean isStarted;

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

}
