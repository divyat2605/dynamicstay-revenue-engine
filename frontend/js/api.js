// Thin fetch wrapper around the DynamicStay REST API.
// Change API_BASE if the backend isn't on localhost:8080.
const API_BASE = window.DYNAMICSTAY_API_BASE || "http://localhost:8080/api";

async function apiRequest(path, options = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });

  let body = null;
  try {
    body = await res.json();
  } catch (_) {
    // no JSON body (e.g. some error responses) — that's fine
  }

  if (!res.ok) {
    const messages = body && body.messages ? body.messages.join(", ") : res.statusText;
    const err = new Error(messages || "Request failed");
    err.status = res.status;
    err.body = body;
    throw err;
  }
  return body;
}

const DynamicStayApi = {
  getRooms: () => apiRequest("/rooms"),
  getRoom: (id) => apiRequest(`/rooms/${id}`),

  getTodayOccupancy: () => apiRequest("/occupancy/today"),
  getOccupancyTrend: (from, to) => apiRequest(`/occupancy/trend?from=${from}&to=${to}`),

  getRecentBookings: () => apiRequest("/bookings"),
  createBooking: (payload) =>
    apiRequest("/bookings", { method: "POST", body: JSON.stringify(payload) }),
  cancelBooking: (id) => apiRequest(`/bookings/${id}`, { method: "DELETE" }),

  getQuote: (payload) =>
    apiRequest("/pricing/quote", { method: "POST", body: JSON.stringify(payload) }),

  health: () => fetch(`${API_BASE.replace("/api", "")}/actuator/health`).then((r) => r.ok),
};
