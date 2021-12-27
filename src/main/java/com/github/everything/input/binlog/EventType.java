package com.github.everything.input.binlog;

public enum EventType {
    insert,
    update,
    delete;

    private EventType() {
    }
}