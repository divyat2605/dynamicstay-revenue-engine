-- DynamicStay core relational schema
-- Rooms, guests, bookings, and transactions live in Postgres because they
-- need ACID guarantees and referential integrity (see README "Design Decisions").

CREATE TABLE rooms (
    id              BIGSERIAL PRIMARY KEY,
    room_number     VARCHAR(10)     NOT NULL UNIQUE,
    room_type       VARCHAR(20)     NOT NULL,
    base_rate       NUMERIC(10, 2)  NOT NULL CHECK (base_rate > 0),
    max_occupancy   INT             NOT NULL CHECK (max_occupancy > 0),
    active          BOOLEAN         NOT NULL DEFAULT TRUE
);

CREATE TABLE guests (
    id          BIGSERIAL PRIMARY KEY,
    full_name   VARCHAR(150)    NOT NULL,
    email       VARCHAR(150)    NOT NULL UNIQUE,
    phone       VARCHAR(20),
    created_at  TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE TABLE bookings (
    id                      BIGSERIAL PRIMARY KEY,
    guest_id                BIGINT          NOT NULL REFERENCES guests(id),
    room_id                 BIGINT          NOT NULL REFERENCES rooms(id),
    check_in                DATE            NOT NULL,
    check_out               DATE            NOT NULL,
    quoted_rate             NUMERIC(10, 2)  NOT NULL,
    final_price             NUMERIC(10, 2),
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    pricing_strategy_used   VARCHAR(40),
    created_at              TIMESTAMP       NOT NULL DEFAULT now(),
    CONSTRAINT chk_dates CHECK (check_out > check_in)
);

CREATE INDEX idx_bookings_room_dates ON bookings (room_id, check_in, check_out);
CREATE INDEX idx_bookings_status ON bookings (status);

CREATE TABLE transactions (
    id              BIGSERIAL PRIMARY KEY,
    booking_id      BIGINT          NOT NULL UNIQUE REFERENCES bookings(id),
    amount          NUMERIC(10, 2)  NOT NULL,
    payment_status  VARCHAR(20)     NOT NULL DEFAULT 'COMPLETED',
    processed_at    TIMESTAMP       NOT NULL DEFAULT now()
);
