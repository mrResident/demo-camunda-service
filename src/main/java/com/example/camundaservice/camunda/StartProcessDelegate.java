package com.example.camundaservice.camunda;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.context.ProcessEngineContext;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.VariableMap;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.camundaservice.utils.CamundaUtil.getOptionalVariable;

@Slf4j
@Component
public class StartProcessDelegate implements JavaDelegate {

    private static final String VAR_PROCESS_NAME = "processName";
    private static final String VAR_IS_ONLY_LOCAL_VARS = "isOnlyLocalVars";
    private static final List<String> EXCLUDE_VAR_NAME_LIST = List.of(VAR_PROCESS_NAME, VAR_IS_ONLY_LOCAL_VARS);

    @Override
    public void execute(DelegateExecution execution) {
        final Boolean isUseLocalVars = getOptionalVariable(execution, VAR_IS_ONLY_LOCAL_VARS, Boolean.class)
            .orElse(Boolean.FALSE);
        final String processName = getOptionalVariable(execution, VAR_PROCESS_NAME, String.class)
            .orElseThrow(() -> new BpmnError("startProcess", "Process name is is null"));
        try {
            ProcessEngineContext.requiresNew();
            execution.getProcessEngineServices()
                .getRuntimeService()
                .startProcessInstanceByKey(processName, processName + "-" + execution.getBusinessKey(), getCamundaVariables(execution, isUseLocalVars));
        } catch (Exception exception) {
            throw new BpmnError(
                "startProcess",
                "Error while start process " + processName + ": " + exception.getMessage(),
                exception
            );
        } finally {
            ProcessEngineContext.clear();
        }
    }

    private Map<String, Object> getCamundaVariables(final DelegateExecution execution, final boolean isUseLocalVars) {
        final Map<String, Object> variables = new HashMap<>();
        final VariableMap variableMap;
        if (Boolean.TRUE.equals(isUseLocalVars)) {
            variableMap = execution.getVariablesLocalTyped();
        } else {
            variableMap = execution.getVariablesTyped();
        }
        variableMap.forEach((key, value) -> {
            if (EXCLUDE_VAR_NAME_LIST.stream().noneMatch(varName -> varName.equals(key))) variables.put(key, value);
        });
        return variables;
    }

}
