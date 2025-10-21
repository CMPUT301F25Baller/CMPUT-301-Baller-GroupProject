# Event Lottery System — Part 2 (Backlog · UI Mockups/Storyboards · CRC · GitHub Usage)

> CMPUT 301 — Fall 2025 · Team A (Product), Team B (UX), Team C (Technical)

This repository tracks the **Part-2 deliverables** for the Event Lottery System:
- **Product Backlog** with estimates, risk, and halfway-checkpoint
- **UI Mockups & Storyboard Sequences** with requirement coverage
- **Object-Oriented Analysis (CRC cards)**

---

## Repo Structure
```
#docs/
├─ backlog/                    # canonical product backlog (graded copy)
│  ├─ README.md
│  ├─ entrant.md
│  ├─ organizer.md
│  └─ admin.md
├─ crc/                        # object-oriented analysis
│  └─ CRC.md
└─ ux/                         # mockups/storyboards
   ├─ README.md
   ├─ AdminPages/
   ├─ EntrantPages/
   └─ OrginizerPages/
README.md
.gitignore
```

---

## Working Agreements

**Branches**
- `main` is protected (PRs only, 1 review minimum, squash merges).
- Feature branches: `feat/<short-desc>`

**Commit messages**
- Conventional style (e.g., `docs(crc): add LotteryDraw responsibilities`).

**Pull Requests**
- Link related stories (e.g., `US01.01.01`) if affected.

---

## IDs & Naming Conventions

| Artifact                    | ID Pattern         | Example                  |
|-----------------------------|--------------------|--------------------------|
| User Story                  | `US<pp.xx.yy>`     | `US01.01.01`             |
| Screen (UI mockup)          | `SCR-<Name>`       | `SCR-EventDetails`       |
| Storyboard (flow)           | `SBR-<Name>`       | `SBR-EntrantAcceptFlow`  |
| CRC Class                   | `<ClassName>`      | `LotteryDraw`            |

These IDs appear in filenames and figure captions.

---

## Quick Links
- 📚 **Backlog Index** → [`/docs/backlog/README.md`](docs/backlog/README.md)
  - Entrant → [`/docs/backlog/entrant.md`](docs/backlog/entrant.md)  
  - Organizer → [`/docs/backlog/organizer.md`](docs/backlog/organizer.md)  
  - Admin → [`/docs/backlog/admin.md`](docs/backlog/admin.md)
- 🧠 **CRC Cards** → [`/docs/crc/CRC.md`](docs/crc/CRC.md)
- 🎬 **UX (Mockups & Storyboards)** → [`/docs/ux/`](docs/ux/)
- 🗂️ **Project Board** → [_linked in repo Projects_](https://github.com/orgs/CMPUT301F25Baller/projects/1/views/1)

---

