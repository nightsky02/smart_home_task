package com.bootcamp.smarthome.exceptions;

public class InvalidCommandException extends HomeAutomationException {
    public InvalidCommandException(String message) {
        super(message);
    }
}
