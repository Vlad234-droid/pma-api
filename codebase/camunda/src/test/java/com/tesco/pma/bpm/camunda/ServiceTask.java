package com.tesco.pma.bpm.camunda;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

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
        await().atMost(200, TimeUnit.MILLISECONDS).until(getProcessInstanceId(execution, remote));
        await().atMost(200, TimeUnit.MILLISECONDS).until(getProcessInstanceId(execution, remote1));
    }

    private Callable<Boolean> getProcessInstanceId(DelegateExecution execution, String remote) {
        return () -> {
            System.out.println(execution.getCurrentActivityId() + "------------->" + execution.getProcessInstanceId()  //NOPMD
                    + " remote - " + remote);
            return true;
        };
    }

}
