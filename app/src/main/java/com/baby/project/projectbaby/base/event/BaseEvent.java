package com.baby.project.projectbaby.base.event;

public class BaseEvent {

    public static final int EVENT_CODE_FORCE_OFFLINE = 1;

    private int eventCode;

    private String message;

    public int getEventCode() {
        return eventCode;
    }

    public void setEventCode(int eventCode) {
        this.eventCode = eventCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
