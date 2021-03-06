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

package org.activiti.cloud.services.rest.controllers;

import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.payloads.RemoveProcessVariablesPayload;
import org.activiti.api.process.model.payloads.SetProcessVariablesPayload;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.cloud.services.rest.api.ProcessInstanceVariableAdminController;
import org.activiti.engine.ActivitiException;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.model.VariableDefinition;
import org.activiti.spring.process.variable.VariableValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ProcessInstanceVariableAdminControllerImpl implements ProcessInstanceVariableAdminController {
    private final ProcessAdminRuntime processAdminRuntime;
    private final Map<String, ProcessExtensionModel> processExtensionModelMap;
    private final VariableValidationService variableValidationService;

    @Autowired
    public ProcessInstanceVariableAdminControllerImpl(ProcessAdminRuntime processAdminRuntime,
                                                      Map<String, ProcessExtensionModel> processExtensionModelMap,
                                                      VariableValidationService variableValidationService
    ) {
        this.processAdminRuntime = processAdminRuntime;
        this.processExtensionModelMap = processExtensionModelMap;
        this.variableValidationService = variableValidationService;
    }

    @Override
    public ResponseEntity<List<String>> updateVariables(@PathVariable String processInstanceId,
                                                     @RequestBody SetProcessVariablesPayload setProcessVariablesPayload) {
        final Optional<Map<String, VariableDefinition>> variableDefinitionMap = getVariableDefinitionMap(processInstanceId);
        if (variableDefinitionMap.isPresent()) {
            final Map<String, Object> variablePayloadMap = setProcessVariablesPayload.getVariables();
            List<String> variableErrorMessages = validatePayloadVariables(variableDefinitionMap.get(),
                    variablePayloadMap);

            if (!variableErrorMessages.isEmpty()) {
                return new ResponseEntity<>(variableErrorMessages, HttpStatus.BAD_REQUEST);
            }
        }
        setProcessVariablesPayload.setProcessInstanceId(processInstanceId);
        processAdminRuntime.setVariables(setProcessVariablesPayload);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private List<String> validatePayloadVariables(Map<String, VariableDefinition> variableDefinitionMap,
                                                  Map<String, Object> variablePayloadMap) {
        final String errorMessage = "Variable with name {0} does not exists.";
        List<ActivitiException> activitiExceptions = new ArrayList<>();
        Set<String> variableNamesDefinition = variableDefinitionMap.values().stream()
                .map(VariableDefinition::getName)
                .collect(Collectors.toSet());
        Set<String> variableNamesPayload = variablePayloadMap.keySet();

        for (String variableNamePayload : variableNamesPayload) {
            if (variableNamesDefinition.contains(variableNamePayload)) {
                Optional<VariableDefinition> variableDefinition = getVariableDefinitionByName(variableNamePayload,
                        variableDefinitionMap);
                variableDefinition.ifPresent(varDefinition ->
                        activitiExceptions.addAll(variableValidationService.
                                validateWithErrors(variablePayloadMap.get(variableNamePayload), varDefinition)
                        ));
            } else {
                activitiExceptions.add(new ActivitiException(MessageFormat.format(errorMessage, variableNamePayload)));
            }
        }

        return activitiExceptions.stream()
                .map(Throwable::getMessage)
                .collect(Collectors.toList());
    }

    private Optional<VariableDefinition> getVariableDefinitionByName(String variableNamePayload, Map<String,
            VariableDefinition> variableDefinitionMap) {
        return variableDefinitionMap.values().stream()
                .filter(variableDefinition -> variableDefinition.getName().equals(variableNamePayload))
                .findFirst();
    }

    private Optional<Map<String, VariableDefinition>> getVariableDefinitionMap(String processInstanceId) {
        ProcessInstance processInstance = processAdminRuntime.processInstance(processInstanceId);
        String processDefinitionKey = processInstance.getProcessDefinitionKey();
        ProcessDefinition processDefinition = processAdminRuntime.processDefinition(processDefinitionKey);
        ProcessExtensionModel processExtensionModel = processExtensionModelMap.get(processDefinition.getKey());

        return Optional.ofNullable(processExtensionModel)
                .map(ProcessExtensionModel::getExtensions)
                .map(Extension::getProperties);
    }
    
    @Override
    public ResponseEntity<Void> removeVariables(@PathVariable String processInstanceId,
                                                @RequestBody RemoveProcessVariablesPayload removeProcessVariablesPayload) {
        if (removeProcessVariablesPayload!=null) {
            removeProcessVariablesPayload.setProcessInstanceId(processInstanceId);
            
        }
        processAdminRuntime.removeVariables(removeProcessVariablesPayload);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
