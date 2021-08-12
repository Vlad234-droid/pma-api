package com.tesco.pma.event.controller.journal;

import java.util.Date;
import java.util.Objects;

import com.tesco.pma.api.Identified;
import com.tesco.pma.event.Event;

public class EventRecord implements Identified<String> {

    private static final long serialVersionUID = -3738916789038425766L;

    private Event event;
    private String destination;
    private EventRecordStatus status = EventRecordStatus.REGISTERED;
    private Date insertTime = new Date();
    private Date statusUpdateTime = insertTime;

    public EventRecord(Event event, String destination) {
        Objects.requireNonNull(event, "Event is undefined");
        Objects.requireNonNull(destination, "Destination is undefined");
        this.event = event;
        this.destination = destination;
    }

    /**
     * Mybatis usage only
     * @param body event body
     * @param destination destination
     * @throws Exception any exception
     */
    @SuppressWarnings("unused")
    private EventRecord(String body, String destination) throws Exception {
        this(EventRecordUtil.createEvent(body), destination);
    }

    @Override
    public String getId() {
        return event.getEventId();
    }

    public Event getEvent() {
        return event;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public EventRecordStatus getStatus() {
        return status;
    }

    public void setStatus(EventRecordStatus status) {
        this.status = status;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    public Date getStatusUpdateTime() {
        return statusUpdateTime;
    }

    public void setStatusUpdateTime(Date statusUpdateTime) {
        this.statusUpdateTime = statusUpdateTime;
    }

    /**
     * Mybatis usage only
     * @return body
     * @throws Exception any exception
     */
    @SuppressWarnings("unused")
    private String getBody() throws Exception {
        return EventRecordUtil.getEventBody(event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event.getEventId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EventRecord other = (EventRecord) obj;
        return Objects.equals(event.getEventId(), other.event.getEventId());
    }

    @Override
    public String toString() {
        return "EventRecord {event=" + event + ", destination=" + destination + ", status=" + status + ", insertTime=" + insertTime
                + ", statusUpdateTime=" + statusUpdateTime + "}";
    }

}
