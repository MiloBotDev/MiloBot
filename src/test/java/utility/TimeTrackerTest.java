package utility;

import org.junit.jupiter.api.Test;
import io.github.milobotdev.milobot.utility.TimeTracker;

import static org.junit.jupiter.api.Assertions.*;

class TimeTrackerTest {

    @Test
    void getElapsedTimeSecs() {
        TimeTracker timeTracker = new TimeTracker();
        timeTracker.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timeTracker.stop();
        assertEquals(1, timeTracker.getElapsedTimeSecs());
        timeTracker.reset();

        timeTracker.start();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timeTracker.stop();
        assertEquals(10, timeTracker.getElapsedTimeSecs());
    }
}