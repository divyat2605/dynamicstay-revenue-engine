package com.dynamicstay.exception;

/** Thrown for semantically invalid requests that pass bean validation
 *  (e.g. checkOut before checkIn). */
public class InvalidBookingRequestException extends RuntimeException {
    public InvalidBookingRequestException(String message) {
        super(message);
    }
}
