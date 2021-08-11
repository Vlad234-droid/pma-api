package com.tesco.pma.bpm.camunda;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class ServiceTask implements JavaDelegate {

    public static final String REMOTE = "remote";
    public static final String LOCAL = "local";

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String remote = (String) execution.getVariable(REMOTE);
        String remote1 = (String) execution.getVariable("remote1");
        String local = (String) execution.getVariable(LOCAL);
        print(execution, remote, local, remote1);
        execution.setVariableLocal(LOCAL, "local" + System.currentTimeMillis());
        execution.setVariable(REMOTE, "remote" + System.currentTimeMillis());
        execution.setVariable("remote1", "remote1" + System.currentTimeMillis());
    }

    private void print(DelegateExecution execution, String remote, String local, String remote1) throws InterruptedException {
        System.out.println(execution.getCurrentActivityId() + "------------->" + execution.getProcessInstanceId()  //NOPMD
                + " local - " + local);
        Thread.sleep(200L);
        System.out.println(execution.getCurrentActivityId() + "------------->" + execution.getProcessInstanceId()  //NOPMD
                + " remote - " + remote);
        Thread.sleep(200L);
        System.out.println(execution.getCurrentActivityId() + "------------->" + execution.getProcessInstanceId()  //NOPMD
                + " remote1 - " + remote1);
    }

}
