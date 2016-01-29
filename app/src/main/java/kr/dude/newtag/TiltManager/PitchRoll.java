package kr.dude.newtag.TiltManager;

/**
 * Created by madcat on 1/13/16.
 */
public class PitchRoll {
    private int pitch;
    private int roll;
    private long currentTime;

    public PitchRoll(int pitch, int roll, long currentTime) {
        this.pitch = pitch;
        this.roll = roll;
        this.currentTime = currentTime;
    }

    public int getPitch() {
        return pitch;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }

    public int getRoll() {
        return roll;
    }

    public void setRoll(int roll) {
        this.roll = roll;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }
}
