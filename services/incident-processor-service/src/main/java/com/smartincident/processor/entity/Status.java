package com.smartincident.processor.entity;

/**
 * Status lifecycle for incidents.
 */
public enum Status {
    NEW,
    PROCESSING,
    ASSIGNED,
    ESCALATED,
    RESOLVED,
    CLOSED
}
