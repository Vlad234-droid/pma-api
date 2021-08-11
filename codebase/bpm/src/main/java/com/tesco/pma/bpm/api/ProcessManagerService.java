package com.tesco.pma.bpm.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import com.tesco.pma.exceptions.InitializationException;

/**
 * Interface for managing process deployment and execution
 */
public interface ProcessManagerService {

    List<String> listProcesses();

    /**
     * Deploy process archive to bpm database
     *
     * @param path Path to process archive (e.g. *.bar)
     * @return process key
     * @throws InitializationException initialization failing
     * @throws FileNotFoundException file not found
     */
    String deployProcess(String path) throws FileNotFoundException, InitializationException;

    /**
     * Deploy process archive to bpm database
     *
     * @param resource {@link File resource} to process archive (e.g. *.bar)
     * @return process key
     * @throws InitializationException initialization failing
     * @throws FileNotFoundException file not found
     */
    String deployProcess(File resource) throws FileNotFoundException, InitializationException;

    /**
     * Undeploy all version of process by processKey
     *
     * @param processName Process key
     * @throws InitializationException initialization failing
     */
    void undeployProcess(String processName) throws InitializationException;

    /**
     * Undeploy concrete process version (e.g. processName-version)
     *
     * @param processName Process key
     * @param version process version
     * @throws InitializationException initialization failing
     */
    void undeployProcess(String processName, String version) throws InitializationException;

    /**
     * Run latest process version with variables
     *
     * @param processName Process key
     * @param varMap Map of variables for starting process
     * @return process instance id
     * @throws ProcessExecutionException runtime exception
     */
    String runProcess(String processName, Map<String, ?> varMap) throws ProcessExecutionException;

    /**
     * Run latest process version
     *
     * @param processName Process key
     * @return process instance id
     * @throws ProcessExecutionException runtime exception
     */
    String runProcess(String processName) throws ProcessExecutionException;

    /**
     * Run concrete process version with variables (e.g. processName-version)
     *
     * @param processName Process key
     * @param varMap Map of variables for starting process
     * @param version Version of process
     * @return process instance id
     * @throws ProcessExecutionException runtime exception
     */
    String runProcess(String processName, String version, Map<String, ?> varMap) throws ProcessExecutionException;

    /**
     * Run concrete process version with variables (e.g. processName-version)
     *
     * @param processName Process key
     * @param version Version of process
     * @return process instance id
     * @throws ProcessExecutionException runtime exception
     */
    String runProcess(String processName, String version) throws ProcessExecutionException;

    /**
     * Run concrete process with messageName
     *
     * @param eventName Message name
     * @param varMap Map of variables for starting process
     * @return process instance id
     * @throws ProcessExecutionException runtime exception
     */
    String runProcessByEvent(String eventName, Map<String, ?> varMap) throws ProcessExecutionException;

    /**
     * Run concrete process with messageName
     *
     * @param eventName event name
     * @return process key
     * @throws ProcessExecutionException runtime exception
     */
    String runProcessByEvent(String eventName) throws ProcessExecutionException;

    /**
     * Notifies the process engine that a message event with name 'messageName' has
     * been received and has been correlated to an execution with id 'executionId'
     * The waiting execution is notified synchronously.
     *
     * @param code of the message event
     * @param instanceId id of the execution to deliver the message to
     * @throws ProcessExecutionException if no such execution exists or if the execution has not subscribed to the event
    */
    void signalEvent(String code, String instanceId) throws ProcessExecutionException;

    /**
     * Notifies the process engine that a message event with the name 'messageName' has
     * been received and has been correlated to an execution with id 'executionId'.
     * The waiting execution is notified synchronously.
     *
     * @param code of the message event
     * @param instanceId to deliver the event
     * @param processVariables a map of variables added to the execution
     * @throws ProcessExecutionException if no such execution exists or if the execution has not subscribed to the signal
     */
    void signalEvent(String code, String instanceId, Map<String, Object> processVariables) throws ProcessExecutionException;
}
