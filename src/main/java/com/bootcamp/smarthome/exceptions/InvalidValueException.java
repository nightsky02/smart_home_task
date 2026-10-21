package com.bootcamp.smarthome.exceptions;

public class InvalidValueException extends HomeAutomationException {
    public InvalidValueException(String field, Object value, String constraint){
        super(String.format("Invalid value '%s' for the field '%s'", value, field));
    }
}
