package engineer.carrot.warren.warren.event;

public class Event {
    private long timestamp;

    Event() {
        this.setTimestampToNow();
    }

    private void setTimestampToNow() {
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getPrettyString() {
        return this.getClass().getName();
    }
}
