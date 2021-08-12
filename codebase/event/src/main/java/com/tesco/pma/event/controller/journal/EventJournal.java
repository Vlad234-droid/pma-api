package com.tesco.pma.event.controller.journal;

import java.util.Collection;
import java.util.Collections;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.api.RetryFilter;
import com.tesco.pma.event.Event;

public interface EventJournal {

    /**
     * Registers event for specific destination
     * @param event an event
     * @param destination destination path
     * @throws EventJournalException any exceptions
     */
    default void registerEvent(Event event, String destination) throws EventJournalException {
        registerEvents(Collections.singletonList(event), destination);
    }

    /**
     * Registers events for specific destination
     * @param events events
     * @param destination destination path
     * @throws EventJournalException any exceptions
     */
    void registerEvents(Collection<Event> events, String destination) throws EventJournalException;

    /**
     * Marks event as sent
     * @param id event's identifier
     * @return result
     * @throws EventJournalException any exceptions
     */
    default boolean markEventAsSent(String id) throws EventJournalException {
        return markEventsAsSent(Collections.singletonList(id));
    }

    /**
     * Marks events as sent
     * @param ids identifiers
     * @return result
     * @throws EventJournalException any exceptions
     */
    boolean markEventsAsSent(Collection<String> ids) throws EventJournalException;
    
    /**
     * Read event record by id
     * @param id identifier
     * @return event resord
     * @throws EventJournalException any exceptions
     */
    EventRecord readEvent(String id) throws EventJournalException;

    /**
     * Read event records for specific destinations by status and retry filters
     * @param destinations destinations
     * @param statusFilter statuses
     * @param retryFilter retry configuration
     * @return events' records
     * @throws EventJournalException any exception
     */
    Collection<EventRecord> readEventsByDestinations(Collection<String> destinations, DictionaryFilter<EventRecordStatus> statusFilter,
                                               RetryFilter retryFilter) throws EventJournalException;

    /**
     * Increment retry count and set last retry time for specified {@link EventRecord} ids based on status filter
     * 
     * @param ids identifiers
     * @param statusFilter statuses
     * @return number of successfully updated records
     * @throws EventJournalException any exception
     */
    int incrementRetryCount(Collection<String> ids, DictionaryFilter<EventRecordStatus> statusFilter) throws EventJournalException;

}
