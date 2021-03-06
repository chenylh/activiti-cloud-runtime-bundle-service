package org.activiti.cloud.services.core.commands;

import org.activiti.api.model.shared.Payload;

public interface CommandExecutor<T extends Payload> {

    String getHandledType();

    void execute(T cmd);
}
