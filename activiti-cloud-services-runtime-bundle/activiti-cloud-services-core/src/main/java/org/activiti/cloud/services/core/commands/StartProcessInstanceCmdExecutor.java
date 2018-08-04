package org.activiti.cloud.services.core.commands;

import org.activiti.runtime.api.ProcessRuntime;
import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.payloads.StartProcessPayload;
import org.activiti.runtime.api.model.results.ProcessInstanceResult;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class StartProcessInstanceCmdExecutor implements CommandExecutor<StartProcessPayload> {

    private ProcessRuntime processRuntime;
    private MessageChannel commandResults;

    public StartProcessInstanceCmdExecutor(ProcessRuntime processRuntime,
                                           MessageChannel commandResults) {
        this.processRuntime = processRuntime;
        this.commandResults = commandResults;
    }

    @Override
    public String getHandledType() {
        return StartProcessPayload.class.getName();
    }

    @Override
    public void execute(StartProcessPayload startProcessPayload) {
        ProcessInstance processInstance = processRuntime.start(startProcessPayload);
        if (processInstance != null) {
            ProcessInstanceResult result = new ProcessInstanceResult(startProcessPayload,
                                                        processInstance);
            commandResults.send(MessageBuilder.withPayload(result).build());
        } else {
            throw new IllegalStateException("Failed to start processInstance");
        }
    }
}
