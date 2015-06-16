package engineer.carrot.warren.warren;

import engineer.carrot.warren.warren.event.Event;

public interface IEventSink {
    void postEvent(Event event);
}
