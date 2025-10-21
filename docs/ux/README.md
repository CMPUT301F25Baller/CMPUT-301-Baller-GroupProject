# UX Index

This folder contains persona-grouped **screens (SCR-*)** and **storyboards (SBR-*)**.
Every item is named with a stable ID and linked to the corresponding file.
User Story IDs (USpp.xx.yy) are listed under each asset.

> Folder layout
>
> ```
> docs/
> └─ ux/
>    ├─ README.md                        #table of content
>    ├─ AdminPages/
>    ├─ EntrantPages/
>    └─ OrganizerPages/
> ```

---

## Table of Contents
- [Entrant](#entrant)
- [Organizer](#organizer)
- [Admin](#admin)

---

## Entrant

- **[SCR-EntrantHome](EntrantPages/SCR-EntrantHome.png)**  
  _US01.01.03 View events; US01.01.04 Filter events_

- **[SCR-EntrantEventDetails](EntrantPages/SCR-EntrantEventDetails.png)**  
  _US01.06.02 Sign up via event details; US01.05.04 View waiting-list count; US01.05.05 View lottery criteria_

- **[SCR-EntrantNotifications](EntrantPages/SCR-EntrantNotifications.png)**  
  _US01.04.01 Notification (win); US01.04.02 Notification (lose);  
  US01.05.02 Accept invitation; US01.05.03 Decline invitation_

- **[SCR-EntrantNotifications-Empty](EntrantPages/SCR-EntrantNotifications-Empty.png)**  
  _US01.04.01/US01.04.02 Empty state; respects opt-out_

- **[SCR-EntrantProfile](EntrantPages/SCR-EntrantProfile.png)**  
  _US01.02.01 Enter personal info; US01.02.02 Update profile; US01.02.03 View event history_

- **[SBR-EntrantFlow](EntrantPages/SBR-EntrantFlow.png)**  
  _US01.01.01 Join waiting list; US01.01.02 Leave waiting list;  
  US01.06.01 Scan QR to details; US01.06.02 Sign up via details_

---

## Organizer

- **[SCR-OrganizerEventForm](OrganizerPages/SCR-OrganizerEventForm.png)**  
  _US02.01.01 Create/Update event; US02.01.04 Set registration period_

- **[SCR-OrganizerWaitlist](OrganizerPages/SCR-OrganizerWaitlist.png)**  
  _US02.02.01 View entrants; US02.05.02 Conduct draw_

- **[SCR-OrganizerInvited](OrganizerPages/SCR-OrganizerInvited.png)**  
  _US02.05.01 Notify selected; US02.06.02 View cancelled; US02.05.03 Replacement_

- **[SCR-OrganizerEnrolled](OrganizerPages/SCR-OrganizerEnrolled.png)**  
  _US02.06.01 View winners; US02.06.03 View final enrolled; US02.06.04 Cancel no-response_

- **[SCR-OrganizerProfile-About](OrganizerPages/SCR-OrganizerProfile-About.png)**  
  _Navigation shell → entry to event creation (US02.01.01)_

- **[SCR-OrganizerProfile-Event](OrganizerPages/SCR-OrganizerProfile-Event.png)**  
  _Organizer’s events list → entry to EventForm/Lists_

- **[SCR-OrganizerProfile-Following](OrganizerPages/SCR-OrganizerProfile-Following.png)**  
  _Non-Part-1 functionality; used for navigation/consistency_

- **[SBR-OrganizerFlow](OrganizerPages/SBR-OrganizerFlow.png)**  
  _Storyboard spanning US02.01.01, US02.05.01, US02.05.02, US02.05.03, US02.06.01–US02.06.04_

---

## Admin

- **[SCR-AdminDashboard](AdminPages/SCR-AdminDashboard.png)**  
  _US03.04.01 Browse events; US03.05.01 Browse profiles; US03.06.01 Browse uploaded images_

- **[SCR-AdminEvents](AdminPages/SCR-AdminEvents.png)**  
  _US03.04.01 Browse events (list/search)_

- **[SCR-AdminEventDetails](AdminPages/SCR-AdminEventDetails.png)**  
  _US03.01.01 Remove events_

- **[SCR-AdminProfiles](AdminPages/SCR-AdminProfiles.png)**  
  _US03.05.01 Browse profiles_

- **[SCR-AdminProfileDetails](AdminPages/SCR-AdminProfileDetails.png)**  
  _US03.02.01 Remove profiles; US03.07.01 Remove violating organizers_

- **[SCR-AdminNotificationLogs](AdminPages/SCR-AdminNotificationLogs.png)**  
  _US03.08.01 Review notification logs_

- **[SBR-AdminFlow](AdminPages/SBR-AdminFlow.png)**  
  _Storyboard across US03.01.01, US03.02.01, US03.03.01, US03.04.01, US03.05.01, US03.06.01, US03.07.01, US03.08.01_

---

## IDs & Naming Conventions

- **SCR-*** = static screen. **SBR-*** = storyboard/flow.
- **User Story IDs:** `USpp.xx.yy` (Entrant=01, Organizer=02, Admin=03).  

> **Filename normalization used here**  
> - Admin: `SCR-AdminDashboard.png`, `SCR-AdminEvents.png`, `SCR-AdminEventDetails.png`, `SCR-AdminProfiles.png`, `SCR-AdminProfileDetails.png`, `SCR-AdminNotificationLogs.png`, `SBR-AdminFlow.png`  
> - Entrant: `SCR-EntrantHome.png`, `SCR-EntrantEventDetails.png`, `SCR-EntrantNotifications.png`, `SCR-EntrantNotifications-Empty.png`, `SCR-EntrantProfile.png`, `SBR-EntrantFlow.png`  
> - Organizer: `SCR-OrganizerEventForm.png`, `SCR-OrganizerWaitlist.png`, `SCR-OrganizerInvited.png`, `SCR-OrganizerEnrolled.png`, `SCR-OrganizerProfile-About.png`, `SCR-OrganizerProfile-Event.png`, `SCR-OrganizerProfile-Following.png`, `SBR-OrganizerFlow.png`
