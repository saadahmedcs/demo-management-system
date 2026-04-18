# Changelog

All notable changes for this project are summarized here so you can copy them into release notes or assignment submissions.

## 2026-04-18 — Feature and documentation update

### Courses and enrollment
- Students join courses using a **teacher-provided join code** (aligned with sharing codes via Google Classroom / similar).

### Assignments and slots
- **Multiple assignments per course** on the TA side, with a **separate tab per assignment** (not only a single combined list).
- Slot tables show **day separators** between different dates (same idea as in messaging) so schedules are easier to scan.

### Group demos
- Slot generation supports **individual vs group** mode and **group member count** (TA).
- On booking, **students enter roll numbers for all group members** when the slot is group-based.
- TA view shows **primary student roll**, **section**, **group size**, and **group member roll numbers** where applicable.

### Evaluation
- **Evaluate** control **per slot** on the TA side so marks can be recorded even **after** the scheduled demo time.

### Messaging
- Messaging is **direct (DM)** between student and TA (with peer selection / filtering), **not** a single public course wall.

### Accounts
- **Section** is captured at **account creation** and surfaced where student identity matters for TAs.

### Backend / data model
- Extended slot and message APIs/DTOs and persistence for the fields above (e.g. roll numbers, group rolls, DM recipient).

### Documentation
- **README** updated with accurate stack notes (**PostgreSQL** env vars), **H2 quick-start** for local runs without PostgreSQL, and run commands for Windows and Unix.
