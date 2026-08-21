// DynamicStay dashboard — vanilla JS, no build step required.
// IDs referenced here are deliberately stable; the Selenium suite targets
// them directly (see selenium-tests/README or the main project README).

let trendChart = null;
let roomsCache = [];

document.addEventListener("DOMContentLoaded", () => {
  checkApiHealth();
  loadRooms();
  loadOccupancyToday();
  loadOccupancyTrend();
  loadRecentBookings();

  document.getElementById("refresh-rooms-btn").addEventListener("click", loadRooms);
  document.getElementById("quote-form").addEventListener("submit", onQuoteSubmit);
  document.getElementById("booking-form").addEventListener("submit", onBookingSubmit);

  const today = new Date().toISOString().slice(0, 10);
  ["quote-checkin", "book-checkin"].forEach((id) => (document.getElementById(id).min = today));
});

async function checkApiHealth() {
  const pill = document.getElementById("api-status");
  try {
    const ok = await DynamicStayApi.health();
    pill.textContent = ok ? "API connected" : "API unreachable";
    pill.className = "status-pill " + (ok ? "ok" : "err");
  } catch (e) {
    pill.textContent = "API unreachable";
    pill.className = "status-pill err";
  }
}

async function loadRooms() {
  const tbody = document.getElementById("rooms-tbody");
  tbody.innerHTML = `<tr><td colspan="5" class="muted">Loading rooms…</td></tr>`;
  try {
    const rooms = await DynamicStayApi.getRooms();
    roomsCache = rooms;
    renderRoomsTable(rooms);
    populateRoomSelects(rooms);
  } catch (e) {
    tbody.innerHTML = `<tr><td colspan="5" class="muted">Failed to load rooms: ${e.message}</td></tr>`;
  }
}

function renderRoomsTable(rooms) {
  const tbody = document.getElementById("rooms-tbody");
  if (!rooms.length) {
    tbody.innerHTML = `<tr><td colspan="5" class="muted">No active rooms.</td></tr>`;
    return;
  }
  tbody.innerHTML = rooms
    .map(
      (r) => `
      <tr data-room-id="${r.id}">
        <td>${r.roomNumber}</td>
        <td>${r.roomType}</td>
        <td>$${Number(r.baseRate).toFixed(2)}</td>
        <td>${r.maxOccupancy}</td>
        <td><button class="btn btn-secondary select-room-btn" data-room-id="${r.id}">Use for Quote</button></td>
      </tr>`
    )
    .join("");

  document.querySelectorAll(".select-room-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
      document.getElementById("quote-room").value = btn.dataset.roomId;
      document.getElementById("quote-panel").scrollIntoView({ behavior: "smooth" });
    });
  });
}

function populateRoomSelects(rooms) {
  const options = rooms
    .map((r) => `<option value="${r.id}">${r.roomNumber} — ${r.roomType} ($${Number(r.baseRate).toFixed(2)})</option>`)
    .join("");
  document.getElementById("quote-room").innerHTML = options;
  document.getElementById("book-room").innerHTML = options;
}

async function loadOccupancyToday() {
  try {
    const snapshot = await DynamicStayApi.getTodayOccupancy();
    document.getElementById("occ-rate").textContent = `${Math.round(snapshot.occupancyRate * 100)}%`;
    document.getElementById("occ-count").textContent = snapshot.occupiedRooms;
    document.getElementById("occ-total").textContent = snapshot.totalRooms;
  } catch (e) {
    document.getElementById("occ-rate").textContent = "—";
  }
}

async function loadOccupancyTrend() {
  const to = new Date();
  const from = new Date();
  from.setDate(from.getDate() - 20);
  const fmt = (d) => d.toISOString().slice(0, 10);

  try {
    const trend = await DynamicStayApi.getOccupancyTrend(fmt(from), fmt(to));
    renderTrendChart(trend);
  } catch (e) {
    console.error("Failed to load occupancy trend", e);
  }
}

function renderTrendChart(trend) {
  const ctx = document.getElementById("trend-chart");
  const labels = trend.map((t) => t.date);
  const data = trend.map((t) => Math.round(t.occupancyRate * 100));

  if (trendChart) {
    trendChart.data.labels = labels;
    trendChart.data.datasets[0].data = data;
    trendChart.update();
    return;
  }

  trendChart = new Chart(ctx, {
    type: "line",
    data: {
      labels,
      datasets: [
        {
          label: "Occupancy %",
          data,
          borderColor: "#4f8cff",
          backgroundColor: "rgba(79, 140, 255, 0.15)",
          tension: 0.3,
          fill: true,
        },
      ],
    },
    options: {
      responsive: true,
      scales: {
        y: { beginAtZero: true, max: 100, ticks: { color: "#8a90a2" } },
        x: { ticks: { color: "#8a90a2" } },
      },
      plugins: { legend: { labels: { color: "#e7e9ee" } } },
    },
  });
}

async function loadRecentBookings() {
  const tbody = document.getElementById("bookings-tbody");
  tbody.innerHTML = `<tr><td colspan="6" class="muted">Loading bookings…</td></tr>`;
  try {
    const bookings = await DynamicStayApi.getRecentBookings();
    renderBookingsTable(bookings);
  } catch (e) {
    tbody.innerHTML = `<tr><td colspan="6" class="muted">Failed to load bookings: ${e.message}</td></tr>`;
  }
}

function renderBookingsTable(bookings) {
  const tbody = document.getElementById("bookings-tbody");
  if (!bookings.length) {
    tbody.innerHTML = `<tr><td colspan="6" class="muted">No bookings yet.</td></tr>`;
    return;
  }
  tbody.innerHTML = bookings
    .map(
      (b) => `
      <tr data-booking-id="${b.id}">
        <td>${b.roomNumber}</td>
        <td>${b.guestName}</td>
        <td>${b.checkIn} → ${b.checkOut}</td>
        <td>$${Number(b.finalPrice).toFixed(2)}</td>
        <td><span class="badge">${b.pricingStrategyUsed || "—"}</span></td>
        <td class="status-${b.status}">${b.status}</td>
      </tr>`
    )
    .join("");
}

async function onQuoteSubmit(evt) {
  evt.preventDefault();
  const resultBox = document.getElementById("quote-result");
  resultBox.className = "quote-result";
  resultBox.textContent = "Calculating…";

  const payload = {
    roomId: Number(document.getElementById("quote-room").value),
    checkIn: document.getElementById("quote-checkin").value,
    checkOut: document.getElementById("quote-checkout").value,
  };

  try {
    const quote = await DynamicStayApi.getQuote(payload);
    resultBox.innerHTML = `
      <strong>$${Number(quote.quotedPricePerNight).toFixed(2)}</strong> / night
      &middot; ${quote.nights} night(s) &middot; total
      <strong>$${Number(quote.totalPrice).toFixed(2)}</strong><br/>
      Base rate: $${Number(quote.baseRate).toFixed(2)} &middot;
      Strategy: <span class="badge">${quote.strategyUsed}</span> &middot;
      Occupancy at quote time: ${Math.round(quote.occupancyRateAtQuote * 100)}%
    `;
  } catch (e) {
    resultBox.className = "quote-result error";
    resultBox.textContent = `Could not calculate rate: ${e.message}`;
  }
}

async function onBookingSubmit(evt) {
  evt.preventDefault();
  const resultBox = document.getElementById("booking-result");
  resultBox.className = "quote-result";
  resultBox.textContent = "Submitting…";

  const checkIn = document.getElementById("book-checkin").value;
  const checkOut = document.getElementById("book-checkout").value;

  if (checkOut <= checkIn) {
    resultBox.className = "quote-result error";
    resultBox.textContent = "Check-out date must be after check-in date.";
    return;
  }

  const payload = {
    roomId: Number(document.getElementById("book-room").value),
    guestName: document.getElementById("book-name").value,
    guestEmail: document.getElementById("book-email").value,
    checkIn,
    checkOut,
  };

  try {
    const booking = await DynamicStayApi.createBooking(payload);
    resultBox.innerHTML = `Booking <strong>#${booking.id}</strong> confirmed for room
      ${booking.roomNumber} at <strong>$${Number(booking.finalPrice).toFixed(2)}</strong>
      (${booking.pricingStrategyUsed} strategy).`;
    document.getElementById("booking-form").reset();
    loadRecentBookings();
    loadOccupancyToday();
  } catch (e) {
    resultBox.className = "quote-result error";
    resultBox.textContent = `Booking failed: ${e.message}`;
  }
}
