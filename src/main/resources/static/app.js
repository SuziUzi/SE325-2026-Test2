"use strict";

const $ = (id) => document.getElementById(id);

let me = null;
const DAYS = 14;

// board state
let robots = [];
let takenByRobot = {}; // robotId -> { "yyyy-mm-dd": "username" }
let mineByRobot = {};  // robotId -> { "yyyy-mm-dd": { bookingId, purpose } }

async function api(path, options = {}) {
  const res = await fetch(path, { headers: { "Content-Type": "application/json" }, ...options });
  const body = res.status === 204 ? null : await res.json().catch(() => null);
  if (!res.ok) throw new Error((body && body.error) || `HTTP ${res.status}`);
  return body;
}

function toast(message, bad = false) {
  const t = $("toast");
  t.textContent = message;
  t.classList.toggle("bad", bad);
  t.classList.remove("hidden");
  clearTimeout(toast._t);
  toast._t = setTimeout(() => t.classList.add("hidden"), 2600);
}

// ---------------------------------------------------------------- dates

function addDays(iso, n) {
  const d = new Date(iso + "T00:00:00");
  d.setDate(d.getDate() + n);
  return d.toISOString().slice(0, 10);
}
function dayLabels(iso) {
  const d = new Date(iso + "T00:00:00");
  return {
    wd: d.toLocaleDateString(undefined, { weekday: "short" }),
    dm: d.toLocaleDateString(undefined, { day: "numeric", month: "short" }),
    weekend: d.getDay() === 0 || d.getDay() === 6,
  };
}

// ---------------------------------------------------------------- session

async function refreshSession() {
  try { me = await api("/api/me"); } catch { me = null; }
  render();
}

function render() {
  const inApp = !!me;
  $("login-view").classList.toggle("hidden", inApp);
  $("app-view").classList.toggle("hidden", !inApp);
  $("logout").classList.toggle("hidden", !inApp);
  $("fleet-management").classList.toggle("hidden", !inApp || me.role !== "ADMIN");
  $("who").textContent = inApp ? `${me.fullName} · ${me.role}` : "Not logged in";
  if (inApp) loadBoard();
}

$("login-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  $("login-error").textContent = "";
  try {
    me = await api("/api/login", {
      method: "POST",
      body: JSON.stringify({ username: $("username").value, password: $("password").value }),
    });
    $("password").value = "";
    render();
  } catch (err) { $("login-error").textContent = err.message; }
});

$("logout").addEventListener("click", async () => {
  await api("/api/logout", { method: "POST" });
  me = null; render();
});

// ---------------------------------------------------------------- board data

async function loadBoard() {
  robots = await api("/api/robots");

  takenByRobot = {};
  await Promise.all(robots.map(async (r) => {
    const slots = await api(`/api/robots/${r.id}/bookings`).catch(() => []);
    takenByRobot[r.id] = Object.fromEntries(slots.map((s) => [s.date, s.bookedBy]));
  }));

  mineByRobot = {};
  const mine = await api("/api/bookings/mine").catch(() => []);
  for (const b of mine) {
    (mineByRobot[b.robotId] ||= {})[b.date] = { bookingId: b.id, purpose: b.purpose };
  }

  renderRobotList();
  renderBoard();
}

function renderRobotList() {
  const list = $("robot-list");
  list.replaceChildren();
  for (const robot of robots) {
    const item = document.createElement("div");
    item.className = "robot-item";
    const details = document.createElement("div");
    details.className = "robot-details";
    const name = document.createElement("strong");
    name.textContent = robot.name;
    const description = document.createElement("span");
    description.textContent = `${robot.model} · ${robot.location}`;
    const remove = document.createElement("button");
    remove.type = "button";
    remove.className = "danger";
    remove.dataset.robot = robot.id;
    remove.textContent = "Remove";
    details.append(name, description);
    item.append(details, remove);
    list.appendChild(item);
  }
}

function renderBoard() {
  const start = $("board-start").value;
  const dates = Array.from({ length: DAYS }, (_, i) => addDays(start, i));
  const board = $("board");
  board.style.setProperty("--days", DAYS);
  board.innerHTML = "";

  // header row
  board.appendChild(cell("corner", "Robot"));
  for (const date of dates) {
    const { wd, dm, weekend } = dayLabels(date);
    const h = document.createElement("div");
    h.className = "col-head" + (weekend ? " weekend" : "");
    h.innerHTML = `<span class="wd">${wd}</span><span class="dm">${dm}</span>`;
    board.appendChild(h);
  }

  // one row per robot
  for (const r of robots) {
    const label = document.createElement("div");
    label.className = "row-head";
    label.innerHTML = `<span class="rname">${r.name}</span><span class="rmodel">${r.model}</span>`;
    board.appendChild(label);

    for (const date of dates) {
      const mineHere = (mineByRobot[r.id] || {})[date];
      const takenBy = (takenByRobot[r.id] || {})[date];
      const c = document.createElement("button");
      c.type = "button";
      c.dataset.robot = r.id;
      c.dataset.date = date;

      if (mineHere) {
        c.className = "slot mine";
        c.dataset.booking = mineHere.bookingId;
        c.textContent = "You";
      } else if (takenBy) {
        c.className = "slot taken";
        c.textContent = takenBy;
        c.title = `Booked by ${takenBy}`;
      } else {
        c.className = "slot free";
        c.textContent = "+";
      }
      board.appendChild(c);
    }
  }
}

function cell(cls, text) {
  const d = document.createElement("div");
  d.className = cls;
  d.textContent = text;
  return d;
}

$("board").addEventListener("click", (e) => {
  const c = e.target.closest(".slot");
  if (!c) return;
  if (c.classList.contains("free")) openBook(Number(c.dataset.robot), c.dataset.date);
  else if (c.classList.contains("mine")) openCell(Number(c.dataset.robot), c.dataset.date);
  else if (c.classList.contains("taken")) toast("Booked by " + c.textContent);
});

$("board-start").addEventListener("change", renderBoard);
$("week-prev").addEventListener("click", () => { $("board-start").value = addDays($("board-start").value, -7); renderBoard(); });
$("week-next").addEventListener("click", () => { $("board-start").value = addDays($("board-start").value, 7); renderBoard(); });

// ---------------------------------------------------------------- robot management

$("robot-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    await api("/api/robots", {
      method: "POST",
      body: JSON.stringify({
        name: $("robot-name").value,
        model: $("robot-model").value,
        location: $("robot-location").value,
      }),
    });
    $("robot-form").reset();
    await loadBoard();
    toast("Robot added.");
  } catch (err) { toast(err.message, true); }
});

$("robot-list").addEventListener("click", async (e) => {
  const button = e.target.closest("button[data-robot]");
  if (!button) return;
  const robot = robots.find((item) => item.id === Number(button.dataset.robot));
  if (!robot || !window.confirm(`Remove ${robot.name}?`)) return;
  try {
    await api(`/api/robots/${robot.id}`, { method: "DELETE" });
    await loadBoard();
    toast("Robot removed.");
  } catch (err) { toast(err.message, true); }
});

// ---------------------------------------------------------------- book dialog

const bookDialog = $("book-dialog");

function openBook(robotId, date) {
  const robot = robots.find((r) => r.id === robotId);
  if (!robot) return;
  bookDialog.dataset.robot = robotId;
  bookDialog.dataset.date = date;
  $("book-title").textContent = `Book ${robot.name}`;
  $("book-when").textContent = dayLabels(date).wd + ", " + dayLabels(date).dm + " " + date.slice(0, 4);
  $("book-purpose").value = "";
  $("book-error").textContent = "";
  bookDialog.showModal();
}

$("book-cancel").addEventListener("click", () => bookDialog.close());

$("book-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  $("book-error").textContent = "";
  try {
    await api("/api/bookings", {
      method: "POST",
      body: JSON.stringify({
        robotId: Number(bookDialog.dataset.robot),
        date: bookDialog.dataset.date,
        purpose: $("book-purpose").value,
      }),
    });
    bookDialog.close();
    toast("Booked! 🤖");
    loadBoard();
  } catch (err) { $("book-error").textContent = err.message; }
});

// ---------------------------------------------------------------- your-booking dialog

const cellDialog = $("cell-dialog");

function openCell(robotId, date) {
  const robot = robots.find((r) => r.id === robotId);
  const mineHere = (mineByRobot[robotId] || {})[date];
  if (!robot || !mineHere) return;
  $("cell-title").textContent = robot.name;
  $("cell-when").textContent = dayLabels(date).wd + ", " + dayLabels(date).dm + " " + date.slice(0, 4);
  $("cell-purpose").textContent = mineHere.purpose;
  cellDialog.showModal();
}

$("cell-close").addEventListener("click", () => cellDialog.close());

// ---------------------------------------------------------------- boot

refreshSession();
