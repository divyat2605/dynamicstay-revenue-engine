-- Realistic sample dataset: a small 12-room boutique hotel with a few
-- dozen historical + upcoming bookings, so the dashboard has something to
-- show immediately after `docker compose up`.

INSERT INTO rooms (room_number, room_type, base_rate, max_occupancy, active) VALUES
('101', 'STANDARD',  89.00,  2, TRUE),
('102', 'STANDARD',  89.00,  2, TRUE),
('103', 'STANDARD',  95.00,  2, TRUE),
('104', 'STANDARD',  95.00,  2, TRUE),
('201', 'DELUXE',    139.00, 3, TRUE),
('202', 'DELUXE',    139.00, 3, TRUE),
('203', 'DELUXE',    149.00, 3, TRUE),
('301', 'SUITE',      219.00, 4, TRUE),
('302', 'SUITE',      229.00, 4, TRUE),
('303', 'SUITE',      219.00, 4, TRUE),
('401', 'PENTHOUSE',  449.00, 6, TRUE),
('402', 'PENTHOUSE',  479.00, 6, TRUE);

INSERT INTO guests (full_name, email, phone) VALUES
('Aditi Sharma',     'aditi.sharma@example.com',    '+91-9800000001'),
('Rohan Mehta',      'rohan.mehta@example.com',     '+91-9800000002'),
('Priya Nair',       'priya.nair@example.com',      '+91-9800000003'),
('Karan Verma',      'karan.verma@example.com',     '+91-9800000004'),
('Sara Iqbal',       'sara.iqbal@example.com',      '+91-9800000005'),
('James Wu',         'james.wu@example.com',        '+1-4155550101'),
('Emily Carter',     'emily.carter@example.com',    '+1-4155550102'),
('Liam O''Connor',   'liam.oconnor@example.com',    '+44-7700900123'),
('Hana Kobayashi',   'hana.kobayashi@example.com',  '+81-9012345678'),
('Diego Fernandez',  'diego.fernandez@example.com', '+34-600123456');

-- Historical bookings (mostly CHECKED_OUT / cancelled) — builds an occupancy history
INSERT INTO bookings (guest_id, room_id, check_in, check_out, quoted_rate, final_price, status, pricing_strategy_used, created_at) VALUES
(1, 1, '2026-06-02', '2026-06-05', 111.25, 333.75, 'CHECKED_OUT', 'SEASONAL',        '2026-05-20 10:00:00'),
(2, 5, '2026-06-03', '2026-06-06', 173.75, 521.25, 'CHECKED_OUT', 'SEASONAL',        '2026-05-22 09:15:00'),
(3, 8, '2026-06-10', '2026-06-14', 273.75, 1095.00,'CHECKED_OUT', 'SEASONAL',        '2026-05-30 14:40:00'),
(4, 2, '2026-06-15', '2026-06-17', 111.25, 222.50, 'CHECKED_OUT', 'SEASONAL',        '2026-06-01 08:30:00'),
(5, 11,'2026-06-18', '2026-06-22', 561.25, 2245.00,'CHECKED_OUT', 'SEASONAL',        '2026-06-05 11:00:00'),
(6, 3, '2026-06-20', '2026-06-23', 118.75, 356.25, 'CHECKED_OUT', 'SEASONAL',        '2026-06-10 16:20:00'),
(7, 6, '2026-06-25', '2026-06-28', 173.75, 521.25, 'CHECKED_OUT', 'SEASONAL',        '2026-06-12 12:00:00'),
(8, 9, '2026-07-01', '2026-07-04', 286.25, 858.75, 'CHECKED_OUT', 'SEASONAL',        '2026-06-18 09:45:00'),
(9, 4, '2026-07-03', '2026-07-06', 118.75, 356.25, 'CHECKED_OUT', 'SEASONAL',        '2026-06-20 10:10:00'),
(10,7, '2026-07-05', '2026-07-09', 186.25, 745.00, 'CHECKED_OUT', 'SEASONAL',        '2026-06-22 13:30:00'),
(1, 10,'2026-07-10', '2026-07-14', 273.75, 1095.00,'CHECKED_OUT', 'SEASONAL',        '2026-06-25 15:00:00'),
(2, 1, '2026-07-12', '2026-07-15', 111.25, 333.75, 'CHECKED_OUT', 'SEASONAL',        '2026-06-28 09:00:00'),
(3, 12,'2026-07-15', '2026-07-19', 598.75, 2395.00,'CHECKED_OUT', 'SEASONAL',        '2026-07-01 11:20:00'),
(4, 5, '2026-07-18', '2026-07-20', 173.75, 347.50, 'CHECKED_OUT', 'SEASONAL',        '2026-07-04 14:00:00'),
(5, 2, '2026-07-20', '2026-07-23', 111.25, 333.75, 'CANCELLED',   'SEASONAL',        '2026-07-06 10:30:00'),
(6, 8, '2026-07-22', '2026-07-25', 273.75, 821.25, 'CHECKED_OUT', 'SEASONAL',        '2026-07-08 09:50:00'),
(7, 3, '2026-07-25', '2026-07-28', 118.75, 356.25, 'CHECKED_OUT', 'SEASONAL',        '2026-07-11 13:15:00'),
(8, 6, '2026-07-28', '2026-08-01', 173.75, 695.00, 'CHECKED_OUT', 'SEASONAL',        '2026-07-14 16:00:00'),
(9, 11,'2026-08-01', '2026-08-04', 561.25, 1683.75,'CHECKED_OUT', 'SEASONAL',        '2026-07-18 10:00:00'),
(10,9, '2026-08-03', '2026-08-06', 286.25, 858.75, 'CHECKED_OUT', 'SEASONAL',        '2026-07-20 11:45:00'),

-- Recent / near-term bookings (CONFIRMED) — feeds "current occupancy" and last-minute pricing
(1, 1,  '2026-08-22', '2026-08-24', 62.30,  124.60, 'CONFIRMED', 'LAST_MINUTE',      '2026-08-20 09:00:00'),
(2, 2,  '2026-08-22', '2026-08-25', 62.30,  186.90, 'CONFIRMED', 'LAST_MINUTE',      '2026-08-19 18:20:00'),
(3, 5,  '2026-08-23', '2026-08-26', 132.05, 396.15, 'CONFIRMED', 'LAST_MINUTE',      '2026-08-20 14:00:00'),
(4, 8,  '2026-08-23', '2026-08-27', 208.05, 832.20, 'CONFIRMED', 'LAST_MINUTE',      '2026-08-20 08:10:00'),
(5, 3,  '2026-08-24', '2026-08-27', 90.25,  270.75, 'CONFIRMED', 'OCCUPANCY_BASED',  '2026-08-18 12:00:00'),
(6, 11, '2026-08-25', '2026-08-29', 471.45, 1885.80,'CONFIRMED', 'OCCUPANCY_BASED',  '2026-08-17 15:30:00'),
(7, 6,  '2026-08-26', '2026-08-28', 146.10, 292.20, 'CONFIRMED', 'OCCUPANCY_BASED',  '2026-08-16 10:20:00'),
(8, 9,  '2026-08-27', '2026-08-30', 240.45, 721.35, 'CONFIRMED', 'OCCUPANCY_BASED',  '2026-08-15 09:00:00'),
(9, 4,  '2026-08-28', '2026-09-01', 79.80,  319.20, 'CONFIRMED', 'SEASONAL',         '2026-08-14 11:00:00'),
(10,7,  '2026-08-30', '2026-09-03', 125.30, 501.20, 'CONFIRMED', 'SEASONAL',         '2026-08-13 13:40:00');

-- Matching transactions for every non-cancelled booking above (ids align because
-- this runs immediately after the booking inserts on a fresh database).
INSERT INTO transactions (booking_id, amount, payment_status, processed_at)
SELECT b.id, b.final_price, 'COMPLETED', b.created_at
FROM bookings b
WHERE b.status <> 'CANCELLED';
