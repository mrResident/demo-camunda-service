package com.example.camundaservice.camunda;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import static com.example.camundaservice.utils.CamundaUtil.getVariable;
import static com.example.camundaservice.utils.CamundaUtil.logger;

@Component
public class LoggerDelegate implements JavaDelegate {

    private static final String BPM_VAR_LEVEL = "level";
    private static final String BPM_VAR_MESSAGE = "message";

    @Override
    public void execute(DelegateExecution execution) {
        logger(execution, getVariable(execution, BPM_VAR_LEVEL, String.class), getVariable(execution, BPM_VAR_MESSAGE, Object.class));
    }

}
