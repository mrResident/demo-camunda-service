package com.example.camundaservice.camunda;

import lombok.Getter;
import lombok.Setter;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.Expression;

import static com.example.camundaservice.utils.CamundaUtil.logger;

@Getter
@Setter
public class LoggerExecutionListener implements ExecutionListener {

    private Expression message;
    private Expression level;

    @Override
    public void notify(DelegateExecution execution) {
        if (level == null) return;
        final String logLevel = (String) level.getValue(execution);
        String msg = message != null ? (String) message.getValue(execution) : null;
        logger(execution, logLevel, msg);
    }

}
