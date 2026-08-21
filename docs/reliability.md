# Reliability and Operations

## Concurrent booking guarantee

`BookingService` performs an overlap query for a fast, friendly rejection, but that query is not the concurrency guarantee. Migration `V3__prevent_overlapping_bookings.sql` adds a PostgreSQL exclusion constraint:

- `room_id` must be equal
- `daterange(check_in, check_out, '[)')` must not overlap
- cancelled bookings are excluded from the constraint

The `[)` range makes adjacent stays valid: a booking ending on June 10 does not conflict with one beginning on June 10. PostgreSQL evaluates the constraint inside the transaction, so two application instances cannot both commit overlapping active bookings. The insert is flushed before the payment row is written; a losing request receives HTTP 409 and its transaction rolls back.

**Two simultaneous Book clicks:** one transaction commits the booking and transaction record; the other blocks at the database constraint, fails with `BOOKING_CONFLICT`, and creates no booking or payment record.

## PostgreSQL to Mongo consistency

Booking and payment data are committed atomically in PostgreSQL. The occupancy event is published with `ApplicationEventPublisher` and handled by `@TransactionalEventListener`, so it runs only after a successful PostgreSQL commit. Mongo persistence is asynchronous and retried three times with exponential backoff. After the final failure, `mongo.event.failure.count` is incremented and the failure is logged for operational follow-up. This is intentionally eventual consistency: Mongo analytics cannot invalidate a confirmed booking.

## Observability

- Every request receives or propagates `X-Correlation-Id`; the value is returned in the response and included in the logging MDC.
- Actuator endpoints: `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness`, `/actuator/metrics`, and `/actuator/prometheus`.
- Key meters: `pricing.quote.duration`, `booking.creation.duration`, `booking.creation.count`, `booking.failure.count`, `booking.cancellation.count`, and `mongo.event.failure.count`.

## Integration tests

`BookingIntegrationTest` starts disposable PostgreSQL 16 and MongoDB 7 containers with Testcontainers. It verifies concurrent booking arbitration, transactional cancellation/persistence, and can run independently of developer-local databases. Docker must be running for these tests.
