-- Serialize the business invariant at the database boundary. PostgreSQL's
-- half-open daterange means [check_in, check_out), so back-to-back stays are valid.
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE bookings
    ADD CONSTRAINT bookings_no_overlapping_active_dates
    EXCLUDE USING gist (
        room_id WITH =,
        daterange(check_in, check_out, '[)') WITH &&
    )
    WHERE (status <> 'CANCELLED');
