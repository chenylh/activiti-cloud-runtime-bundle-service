/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.cloud.starter.rb.behavior;

import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.engine.impl.bpmn.behavior.IntermediateThrowSignalEventActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;
import org.springframework.context.ApplicationContext;

import static org.activiti.services.subscriptions.behavior.BroadcastSignalEventActivityBehavior.DEFAULT_THROW_SIGNAL_EVENT_BEAN_NAME;

public class CloudActivityBehaviorFactory extends DefaultActivityBehaviorFactory {

    private ApplicationContext applicationContext;

    public CloudActivityBehaviorFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public IntermediateThrowSignalEventActivityBehavior createIntermediateThrowSignalEventActivityBehavior(ThrowEvent throwEvent,
                                                                                                           SignalEventDefinition signalEventDefinition,
                                                                                                           Signal signal) {
        return (IntermediateThrowSignalEventActivityBehavior) applicationContext.getBean(DEFAULT_THROW_SIGNAL_EVENT_BEAN_NAME, applicationContext, signalEventDefinition, signal);
    }
}
