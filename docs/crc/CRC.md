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

|User Story|Description|
|----------|:----------|
|01.01.01  | As an entrant, I want to join the waiting list for a specific event|
|01.01.02  | As an entrant, I want to leave the waiting list for a specific event|
|01.01.03  |As an entrant, I want to be able to see a list of events that I can join the waiting list for|
|01.01.04  |As an entrant, I want to filter events based on my interests and availability|
|01.02.01  |As an entrant, I want to provide my personal information such as name, email and optional phone number in the app|
|01.02.02  |As an entrant I want to update information such as name, email and contact information on my profile|
|01.02.03  |As an entrant, I want to have a history of events I have registered for, whether I was selected or not|
|01.02.04  |As an entrant, I want to delete my profile if I no longer wish to use the app|
|01.04.01  |As an entrant I want to receive notification when I am chosen to participate from the waiting list (when I "win" the lottery)|
|01.04.02  |As an entrant I want to receive notification of when I am not chosen on the app (when I "lose" the lottery)|
|01.04.03  |As an entrant I want to opt out of receiving notifications from organizers and admins|
|01.05.01  |As an entrant I want another chance to be chosen from the waiting list if a selected user declines an invitation to sign up|
|01.05.02  |As an entrant I want to be able to accept the invitation to register/sign up when chosen to participate in an event|
|01.05.03  |As an entrant I want to be able to decline an invitation when chosen to participate in an event|
|01.05.04  |As an entrant, I want to know how many total entrants are on the waiting list for an event|
|01.05.05  |As an entrant, I want to be informed about the criteria or guidelines for the lottery selection process|
|01.06.01  |As an entrant I want to view event details within the app by scanning the promotional QR code|
|01.06.02  |As an entrant I want to be able to be sign up for an event by from the event details|
|01.07.01  |As an entrant, I want to be identified by my device, so that I don't have to use a username and password|
|02.02.01  |As an organizer I want to view the list of entrants who joined my event waiting list|
|02.02.02  |As an organizer I want to see on a map where entrants joined my event waiting list from|
|02.03.01  |As an organizer I want to OPTIONALLY limit the number of entrants who can join my waiting list|
|02.05.01  |As an organizer I want to send a notification to chosen entrants to sign up for events|
|02.05.02  |As an organizer I want to set the system to sample a specified number of attendees to register for the event|
|02.05.03  |As an organizer I want to be able to draw a replacement applicant from the pooling system when a previously selected applicant cancels or rejects the invitation|
|02.06.01  |As an organizer I want to view a list of all chosen entrants who are invited to apply|
|02.06.02  |As an organizer I want to see a list of all the cancelled entrants|
|02.06.03  |As an organizer I want to see a final list of entrants who enrolled for the event|
|02.06.04  |As an organizer I want to cancel entrants that did not sign up for the event|
|02.06.05  |As an organizer I want to export a final list of entrants who enrolled for the event in CSV format|
|02.07.01  |As an organizer I want to send notifications to all entrants on the waiting list|
|02.07.02  |As an organizer I want to send notifications to all selected entrants|
|02.07.03  |As an organizer I want to send a notification to all cancelled entrants|







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
