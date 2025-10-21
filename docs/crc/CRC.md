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

**Stories:**
[US01.01.01](../backlog/entrant.md#US010101), [US01.01.02](../backlog/entrant.md#US010102), [US01.02.03](../backlog/entrant.md#US010203), [US01.02.04](../backlog/entrant.md#US010204), [US01.04.01](../backlog/entrant.md#US010401), [US01.04.02](../backlog/entrant.md#US010402), [US01.04.03](../backlog/entrant.md#US010403)


---

## Entrant
**Responsibilities**
- discover/view event details (open/close/full)
- request join to event’s lottery pool (via LotterySystem)
- accept/decline an invitation within deadline (via LotterySystem)
- manage notification opt-outs

**Collaborators**
- User, LotterySystem, NotificationManager, QRScanner, FirebaseManager

**Stories:**
[US01.01.01](../backlog/entrant.md#US010101), [US01.01.02](../backlog/entrant.md#US010102), [US01.01.03](../backlog/entrant.md#US010103), [US01.01.04](../backlog/entrant.md#US010104),
[US01.02.01](../backlog/entrant.md#US010201), [US01.02.02](../backlog/entrant.md#US010202), [US01.02.03](../backlog/entrant.md#US010203), [US01.02.04](../backlog/entrant.md#US010204),
[US01.04.01](../backlog/entrant.md#US010401), [US01.04.02](../backlog/entrant.md#US010402), [US01.04.03](../backlog/entrant.md#US010403),
[US01.05.01](../backlog/entrant.md#US010501), [US01.05.02](../backlog/entrant.md#US010502), [US01.05.03](../backlog/entrant.md#US010503), [US01.05.04](../backlog/entrant.md#US010504), [US01.05.05](../backlog/entrant.md#US010505),
[US01.06.01](../backlog/entrant.md#US010601), [US01.06.02](../backlog/entrant.md#US010602)


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

**Stories:**
[US02.01.01](../backlog/organizer.md#US020101), [US02.01.04](../backlog/organizer.md#US020104),
[US02.02.01](../backlog/organizer.md#US020201), [US02.02.02](../backlog/organizer.md#US020202), [US02.02.03](../backlog/organizer.md#US020203),
[US02.03.01](../backlog/organizer.md#US020301),
[US02.04.01](../backlog/organizer.md#US020401), [US02.04.02](../backlog/organizer.md#US020402),
[US02.05.01](../backlog/organizer.md#US020501), [US02.05.02](../backlog/organizer.md#US020502), [US02.05.03](../backlog/organizer.md#US020503),
[US02.06.01](../backlog/organizer.md#US020601), [US02.06.02](../backlog/organizer.md#US020602), [US02.06.03](../backlog/organizer.md#US020603), [US02.06.04](../backlog/organizer.md#US020604), [US02.06.05](../backlog/organizer.md#US020605),
[US02.07.01](../backlog/organizer.md#US020701), [US02.07.02](../backlog/organizer.md#US020702), [US02.07.03](../backlog/organizer.md#US020703)


---

## Admin
**Responsibilities**
- remove/browse events, posters, organizers, entrant profiles per policy
- review notification logs / draw logs
- suspend organizer/user

**Collaborators**
- User, FirebaseManager, NotificationManager, LotterySystem
  
**Stories:**
[US03.02.01](../backlog/admin.md#US030201), [US03.03.01](../backlog/admin.md#US030301), [US03.04.01](../backlog/admin.md#US030401),
[US03.01.01](../backlog/admin.md#US030101), [US03.05.01](../backlog/admin.md#US030501), [US03.06.01](../backlog/admin.md#US030601),
[US03.07.01](../backlog/admin.md#US030701), [US03.08.01](../backlog/admin.md#US030801)


---

## Event
**Responsibilities**
- provide eventId, title, description, event poster
- provide counts/summaries for UI (entrants in pool, selected, finalized)
- provides a map to view where entrants have signed up from

**Collaborators**
- LotterySystem, FirebaseManager, NotificationManager

**Stories:**
[US01.01.03](../backlog/entrant.md#US010103), [US01.01.04](../backlog/entrant.md#US010104), [US01.05.04](../backlog/entrant.md#US010504),
[US01.05.05](../backlog/entrant.md#US010505), [US01.06.01](../backlog/entrant.md#US010601), [US01.06.02](../backlog/entrant.md#US010602),
[US01.01.01](../backlog/entrant.md#US010101)


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

**Stories:**
[US01.01.01](../backlog/entrant.md#US010101), [US01.01.02](../backlog/entrant.md#US010102),
[US01.05.01](../backlog/entrant.md#US010501), [US01.05.02](../backlog/entrant.md#US010502), [US01.05.03](../backlog/entrant.md#US010503),
[US01.05.04](../backlog/entrant.md#US010504), [US01.06.02](../backlog/entrant.md#US010602)


---

## NotificationManager
**Responsibilities**
- compose messages (winners, waitlist, broadcast, cancellations)
- enforce preferences/opt-outs (entrant, organizer/admin categories)

**Collaborators**
- User, Entrant, Organizer, Admin, LotterySystem, FirebaseManager

**Stories:**
[US01.04.01](../backlog/entrant.md#US010401), [US01.04.02](../backlog/entrant.md#US010402), [US01.04.03](../backlog/entrant.md#US010403),
[US01.05.01](../backlog/entrant.md#US010501), [US01.05.02](../backlog/entrant.md#US010502), [US01.05.03](../backlog/entrant.md#US010503)


---

## QRScanner
**Responsibilities**
- scan and decode QR code
- validate link (not expired/invalid)
- route to Event details

**Collaborators**
- Event, FirebaseManager, LotterySystem

**Stories:**
[US01.06.01](../backlog/entrant.md#US010601)


---

## FirebaseManager
**Responsibilities**
- CRUD for Users/Roles, Events, Pools/Entries, Draws
- transactional updates (e.g., join with deduplication; accept capacity constraints)
- poster upload/delete via Storage
- server-side callable Functions (e.g., `startDraw`, `enforceTimeouts`)

**Collaborators**
- User, Entrant, Organizer, Admin, Event, LotterySystem, NotificationManager

**Stories:**
[US01.01.01](../backlog/entrant.md#US010101), [US01.01.02](../backlog/entrant.md#US010102), [US01.01.03](../backlog/entrant.md#US010103), [US01.01.04](../backlog/entrant.md#US010104),
[US01.02.01](../backlog/entrant.md#US010201), [US01.02.02](../backlog/entrant.md#US010202), [US01.02.03](../backlog/entrant.md#US010203), [US01.02.04](../backlog/entrant.md#US010204),
[US01.04.01](../backlog/entrant.md#US010401), [US01.04.02](../backlog/entrant.md#US010402), [US01.04.03](../backlog/entrant.md#US010403),
[US01.05.01](../backlog/entrant.md#US010501), [US01.05.02](../backlog/entrant.md#US010502), [US01.05.03](../backlog/entrant.md#US010503),
[US01.05.04](../backlog/entrant.md#US010504), [US01.05.05](../backlog/entrant.md#US010505),
[US01.06.01](../backlog/entrant.md#US010601), [US01.06.02](../backlog/entrant.md#US010602)


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
