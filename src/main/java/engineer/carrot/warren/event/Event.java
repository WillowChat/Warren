package engineer.carrot.warren.event;

public class Event {
    private long timestamp;

    public Event() {
        this.setTimestampToNow();
    }

    public void setTimestampToNow() {
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getPrettyString() {
        return this.getClass().getName();
    }
}
