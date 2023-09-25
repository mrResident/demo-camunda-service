package com.example.camundaservice.utils;

import com.example.camundaservice.enums.LoggerType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.xml.SpinXmlElement;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class CamundaUtil {

    private static final String CAST_ERROR_MESSAGE = "Can not casting variable {} to type {}";
    private static final String LOG_MESSAGE = "businessKey: {} | processInstanceId: {} | message: {}";
    private static final Map<Class<?>, Function<String, ?>> STRING_TO_TYPE_BY_CLASS = Map.ofEntries(Map.entry(Boolean.class, Boolean::valueOf),
                                                                                                    Map.entry(Integer.class, Integer::parseInt),
                                                                                                    Map.entry(Long.class, Long::parseLong),
                                                                                                    Map.entry(Double.class, Double::parseDouble),
                                                                                                    Map.entry(Float.class, Float::parseFloat),
                                                                                                    Map.entry(Short.class, Short::parseShort),
                                                                                                    Map.entry(BigDecimal.class, BigDecimal::new));
    private static final String BPM_VARIABLE_AS_JSON = "json";
    private static final String BPM_VARIABLE_AS_XML = "xml";

    public static void logger(final DelegateExecution execution, final String level, final Object message) {
        if (level == null || execution == null) {
            return;
        }
        switch (LoggerType.getByName(level)) {
            case WARN -> log.warn(LOG_MESSAGE, execution.getBusinessKey(), execution.getProcessInstanceId(), message);
            case ERROR -> log.error(LOG_MESSAGE, execution.getBusinessKey(), execution.getProcessInstanceId(), message);
            default -> log.info(LOG_MESSAGE, execution.getBusinessKey(), execution.getProcessInstanceId(), message);
        }
    }

    @Nullable
    public static <T> T getVariable(final DelegateExecution execution, final String varName, final Class<T> type) {
        if (execution == null || execution.getVariable(varName) == null || type == null) return null;
        final Object variable = execution.getVariable(varName);
        if (type.isInstance(variable)) return type.cast(variable);
        try {
            if (BPM_VARIABLE_AS_JSON.equalsIgnoreCase(execution.getVariableTyped(varName).getType().getName())) {
                return Spin.JSON(variable).mapTo(type);
            }
            if (BPM_VARIABLE_AS_XML.equalsIgnoreCase(execution.getVariableTyped(varName).getType().getName())) {
                return Spin.XML(variable).mapTo(type);
            }
            return objectToType(variable, type);
        } catch (Exception exception) {
            log.warn(CAST_ERROR_MESSAGE, varName, type.getName());
        }
        return null;
    }

    public static <T> Optional<T> getOptionalVariable(final DelegateExecution execution, final String varName, final Class<T> type) {
        return Optional.ofNullable(getVariable(execution, varName, type));
    }

    public static void setVariable(final DelegateExecution execution, final String varName, final Object varValue) {
        if (execution == null || varName == null) {
            return;
        }
        if (varValue == null) {
            execution.setVariable(varName, null);
        } else {
            execution.setVariable(varName, Variables.objectValue(varValue).serializationDataFormat(Variables.SerializationDataFormats.JSON).create());
        }
    }

    @Nullable
    public static <T> T stringValueToType(final String stringValue, final Class<T> type) {
        if (StringUtils.isBlank(stringValue) || type == null) {
            return null;
        }
        try {
            Object result = Optional.ofNullable(STRING_TO_TYPE_BY_CLASS.get(type))
                .map(stringFunction -> stringFunction.apply(stringValue))
                .orElse(null);
            if (type.isInstance(result)) {
                return type.cast(result);
            }
        } catch (Exception exception) {
            log.warn("Can not casting string value to type {}", type.getName(), exception);
        }
        return null;
    }

    @Nullable
    public static <T> T numberValueToType(final Number numberValue, final Class<T> type) {
        if (numberValue == null || type == null) {
            return null;
        }
        try {
            String numberVariableAsString = numberValue.toString();
            if (type == String.class) {
                return type.cast(numberVariableAsString);
            }
            Object result = Optional.ofNullable(STRING_TO_TYPE_BY_CLASS.get(type))
                .map(stringFunction -> stringFunction.apply(numberVariableAsString))
                .orElse(null);
            if (type.isInstance(result)) {
                return type.cast(result);
            }
        } catch (Exception exception) {
            log.warn("Can not casting number value to type {}", type.getName(), exception);
        }
        return null;
    }

    @Nullable
    public static <T> T objectToType(Object object, Class<T> type) {
        if (object == null || type == null) {
            return null;
        }
        try {
            if (type.isInstance(object)) {
                return type.cast(object);
            }
            if (object instanceof String strVal) {
                T result = stringValueToType(strVal, type);
                if (result != null) {
                    return result;
                }
            }
            if (object instanceof Number numberValue) {
                return numberValueToType(numberValue, type);
            }
            if (object instanceof SpinJsonNode spinJsonNode) {
                return spinJsonNode.mapTo(type);
            }
            if (object instanceof SpinXmlElement spinXmlElement) {
                return spinXmlElement.mapTo(type);
            }
        } catch (Exception exception) {
            log.warn("Can not casting object {} to type {}", object.getClass().getName(), type.getName());
        }
        return null;
    }

}
