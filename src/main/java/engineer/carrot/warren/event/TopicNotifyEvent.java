package engineer.carrot.warren.event;

public class TopicNotifyEvent extends Event {
    public String forChannel;
    public String contents;

    public TopicNotifyEvent(String forChannel, String contents) {
        super();

        this.forChannel = forChannel;
        this.contents = contents;
    }
}
