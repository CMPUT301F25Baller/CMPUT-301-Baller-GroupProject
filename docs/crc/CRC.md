# CRC Cards — Event Lottery System

> Behavior-first object-oriented analysis to support Part-2 deliverables.  
> Each card lists **Responsibilities** (verbs/behaviors) and **Collaborators** (classes it works with).  

---

## User
**Responsibilities**
- manage profile: update name/contact, set notification preferences
- access registration history

**Collaborators**
- Entrant, Organizer, Admin, NotificationManager, FirebaseManager

---

## Entrant
**Responsibilities**
- discover/view event details (open/close/full)
- request join to event’s lottery pool (via LotterySystem)
- accept/decline an invitation within deadline (via LotterySystem)
- manage notification opt-outs

**Collaborators**
- User, LotterySystem, NotificationManager, QRScanner, FirebaseManager

---

## Organizer
**Responsibilities**
- create/update events (title, windows, capacity, geolocation requirement, event poster)
- open/close registration window
- browse waiting list
- initiate lottery draw and replacement draws
- broadcast announcements to entrants
- cancel non-responders; finalize enrolled list

**Collaborators**
- User, Event, LotterySystem, NotificationManager, FirebaseManager

---

## Admin
**Responsibilities**
- remove/browse events, posters, organizers, entrant profiles per policy
- review notification logs / draw logs
- suspend organizer/user

**Collaborators**
- User, FirebaseManager, NotificationManager, LotterySystem

---

## Event
**Responsibilities**
- provide eventId, title, description, event poster
- provide counts/summaries for UI (entrants in pool, selected, finalized)
- provides a map to view where entrants have signed up from

**Collaborators**
- LotterySystem, FirebaseManager, NotificationManager

---

## LotterySystem
**Responsibilities**
- validate entrant eligibility, record join, deduplication per device/user/event
- perform draw
- sends invites to selected entrants
- start acceptance timer, mark accepted/declined, trigger replacement backfill
- enforce capacity limits, waitingListCap, registration window
- summarize pool & results for Organizer/Admin dashboards

**Collaborators**
- Event, Entrant, Organizer, Admin, NotificationManager, FirebaseManager

---

## NotificationManager
**Responsibilities**
- compose messages (winners, waitlist, broadcast, cancellations)
- enforce preferences/opt-outs (entrant, organizer/admin categories)

**Collaborators**
- User, Entrant, Organizer, Admin, LotterySystem, FirebaseManager

---

## QRScanner
**Responsibilities**
- scan and decode QR code
- validate link (not expired/invalid)
- route to Event details

**Collaborators**
- Event, FirebaseManager, LotterySystem

---

## FirebaseManager
**Responsibilities**
- CRUD for Users/Roles, Events, Pools/Entries, Draws
- transactional updates (e.g., join with deduplication; accept capacity constraints)
- poster upload/delete via Storage
- server-side callable Functions (e.g., `startDraw`, `enforceTimeouts`)

**Collaborators**
- User, Entrant, Organizer, Admin, Event, LotterySystem, NotificationManager

---

# Minimal Interaction Sketches

**Join flow (Entrant)**
1) QRScanner → eventId  
2) FirebaseManager → fetch Event; Event.isOpen?  
3) LotterySystem.join(eventId, entrant, geo?) → FirebaseManager.writeEntry
4) NotificationManager (optional confirmation)

**Draw flow (Organizer)**
1) Organizer triggers draw on Event  
2) LotterySystem.conductDraw(eventId, N) → FirebaseManager.updateEntry
3) NotificationManager.notifyWinners()  
4) LotterySystem.startAcceptanceTimers()

**Accept/Decline flow (Entrant)**
1) Entrant responds  
2) LotterySystem.recordDecision(entryId, accepted?) → FirebaseManager.updateEntry  
3) If declined/timeout → LotterySystem.backfillReplacements()
