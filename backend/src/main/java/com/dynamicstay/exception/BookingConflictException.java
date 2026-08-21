package com.dynamicstay.exception;

/** Thrown when a requested room/date range overlaps an existing booking. */
public class BookingConflictException extends RuntimeException {
    public BookingConflictException(String message) {
        super(message);
    }
}
