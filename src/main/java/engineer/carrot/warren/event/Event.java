package engineer.carrot.warren.event;

public class Event {
    public long timestamp;

    public Event() {
        this.setTimestampToNow();
    }

    public void setTimestampToNow() {
        this.timestamp = System.currentTimeMillis();
    }
}
