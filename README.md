# Question 1 — RoboLab Booking

**Time guide:** ~0.5 hours · **You may use any AI coding assistant.**

---

## The scenario

You've joined the team that maintains **RoboLab**, a small web system students use to
book time on the robotics lab's equipment. It's a Spring Boot application backed by a
SQLite database.

The system works, but testing and support have turned up three problems, and the lab wants two
new features. There are **five tasks** below. Do as many as you can, without breaking anything else.

## Getting started

This is a standard Maven project (Java source in `src/main`, tests in `src/test`). You'll need
**JDK 21 (or newer)** and **Maven**.

**Run the app:**

```bash
mvn spring-boot:run
```

Then open **http://localhost:8080** and log in with one of:

| Username | Password      | Role    |
|----------|---------------|---------|
| `alice`  | `alice123`    | student |
| `bob`    | `bob123`      | student |
| `admin`  | `R0b0t!Adm1n` | admin   |

**Run the tests:**

```bash
mvn test
```

There are two groups of tests, and you can run them any time to see where you stand:

- **`BookingFeatureTest`** (13 tests) checks the app's existing behaviour (login, listing robots,
  the booking rules, search, sort, the admin report). These **pass on the starter** and must
  **stay green** — a red one means you changed behaviour that was meant to be preserved.
- **`Task1SecurityTest` … `Task5CancellationTest`** are acceptance tests, one class per task. They
  **start red** on the starter and turn **green** as you complete each task. Don't edit them.

So on a fresh checkout `mvn test` is **not** all-green — that's expected. You're done when every
test passes.

**The database** is a file, `robolab.db`, created on first run. You can inspect it with any
SQLite tool, e.g. `sqlite3 robolab.db "SELECT * FROM bookings;"`. Delete the file to reset to
the seed data.

---

## Task 1 — a security problem

> **SEC-114 (Critical).** A tester found that, by typing certain characters into the login
> form, they could sign in as another user — including the admin — without knowing the
> password. They were also able to get the system to return bookings belonging to other
> users. It looks like one underlying flaw, and it may be reachable from more than one screen.

**Fix it** so neither of these is possible any more, and odd input fails safely (a normal error
or an empty result — never a bypass, a leak, or a server crash). The same flaw may appear on more
than one screen; cover all of them, and don't change any existing behaviour.

## Task 2 — a double-booking problem

> **BUG-207 (High).** A robot is supposed to be bookable by only one person per day, and the
> booking screen does check for that. Even so, support has twice found two different students
> booked onto the **same robot on the same day**. It happens only occasionally, and we can't
> reproduce it by clicking through the app by hand.

**Fix it** so a robot can never be booked by more than one person on the same day — including
when two booking requests arrive at the same moment. A booking that genuinely clashes should
still be refused with the same "already booked" response as today.

## Task 3 — an untrusted-content problem

> **SEC-121 (High).** The text a user types into a booking's **purpose** is shown back on the
> page exactly as typed. A tester entered some HTML markup as the purpose and found the browser
> *rendered* it rather than showing it as text — meaning a crafted purpose can run script in the
> page.

To see it for yourself: log in, book a robot with the text below as the **purpose**, then click
that booking on the board to open it — a pop-up box appears, because the markup ran instead of
being shown as text.

```
<img src=x onerror=alert(1)>
```

**Fix it** so what a user typed is always displayed as text, never interpreted as markup or
script. The displayed text must still read exactly as the user typed it.

## Task 4 — a new feature: manage robots

> **FEAT-032.** Lab managers (admins) need to grow and shrink the robot fleet from the app —
> there is currently no way to add a new robot or remove an old one.

Implement this. The **front end is yours to design** however you like — but it must talk to these
two backend endpoints, which you also need to build:

- **`POST /api/robots`** — add a robot. Admins only.
  - Request body (JSON): `{ "name": "...", "model": "...", "location": "..." }`
  - On success: **201 Created**, returning the new robot as JSON (`{ id, name, model, location }`).
  - **400** if any field is missing or blank.
- **`DELETE /api/robots/{id}`** — remove a robot. Admins only.
  - On success: **204 No Content**.
  - **404** if there is no robot with that id.
  - **409** if the robot still has bookings (don't orphan them).
- Both endpoints: **401** if not logged in, **403** if logged in but not an admin.

`GET /api/robots` already exists and lists all robots — your new robots must show up there (and
on the booking board) once added.

## Task 5 — a new feature: cancel a booking

> **FEAT-041.** There is no way to cancel a booking once it's made. Students should be able to
> cancel their own bookings, and lab managers (admins) should be able to cancel anyone's.

Implement this. The **front end is yours to design** however you like — but it must talk to this
backend endpoint, which you also need to build:

- **`DELETE /api/bookings/{id}`** — cancel a booking. Any logged-in user.
  - A **student** may cancel **only their own** booking; an **admin** may cancel **anyone's**.
  - On success: **204 No Content**, and the booking is gone (its day frees up again).
  - **404** if there is no booking with that id, or it belongs to someone else and you are not
    an admin.
  - **401** if not logged in.

## Checking your work

Run `mvn test`. You're done when **everything is green**: `BookingFeatureTest` stays green, and
each task's test (`Task1SecurityTest` … `Task5CancellationTest`) has gone from red to green.

Task 3 is browser code, so its test can only check the source heuristically — also confirm by
hand that a booking with purpose `<img src=x onerror=alert(1)>` no longer pops up a box.

## Scope — read before you start

- **In scope:** the five tasks described above.
- **Out of scope (leave it alone):** this legacy system stores passwords in plain text. That
  is a real problem, but it is **not** part of this question. Don't spend time on it.
- Keep your changes focused. A good submission is a small, surgical diff that any reviewer can
  see is correct.

## What to hand in

Submit **only your `git diff`** — the patch of what you changed against the starter:

```bash
git diff > solution.patch
```

Hand in that `solution.patch` file and nothing else (no full project, no `target/`, no
`robolab.db`).
