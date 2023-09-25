package com.example.camundaservice.enums;

public enum LoggerType {
    INFO, WARN, ERROR, DEFAULT;

    public static LoggerType getByName(String name) {
        try {
            return name != null ? LoggerType.valueOf(name.toUpperCase()) : DEFAULT;
        }
        catch (Exception e) {
            return DEFAULT;
        }
    }

}
