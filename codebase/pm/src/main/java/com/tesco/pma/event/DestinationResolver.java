package com.tesco.pma.event;

/**
 * Need to resolve actual destination (e.g. kafka topic name) by code.
 *
 *
 */
public interface DestinationResolver {

    String resolveDestination(String channelName);
    
    String resolveChannelName(String desstination);
}
