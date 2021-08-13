package com.tesco.pma.event;

public class EventResponseSupport implements EventResponse {

    private static final long serialVersionUID = 6869989765510740444L;
    protected String eventName;
    protected String responseName;
    protected Object source;

    public EventResponseSupport(Event event, String responseName) {
        this(event.getEventName(), responseName);
    }

    public EventResponseSupport(String eventName, String responseName) {
        this(eventName, responseName, null);
    }

    public EventResponseSupport(String eventName, String responseName, Object source) {
        this.eventName = eventName;
        this.responseName = responseName;
        this.source = source;
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    @Override
    public String getResponseName() {
        return responseName;
    }

    @Override
    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }
}
