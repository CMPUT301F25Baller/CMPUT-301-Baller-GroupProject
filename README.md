# Event Lottery System — Part 2 (Backlog · UI Mockups/Storyboards · CRC · GitHub Usage)

> CMPUT 301 — Fall 2025 · Team A (Product), Team B (UX), Team C (Technical)

This repository tracks the **Part-2 deliverables** for the Event Lottery System:
- **Product Backlog** with estimates, risk, and halfway-checkpoint
- **UI Mockups & Storyboard Sequences** with requirement coverage
- **Object-Oriented Analysis (CRC cards)**

---

## Repo Structure
/docs/
backlog/ # Team A: user stories, risk, halfway-checkpoint
ux/ # Team B: mockups, storyboards
crc/ # Team C: CRC cards, technical decisions
.gitignore
README.md

---

## Working Agreements

**Branches**
- `main` is protected (PRs only, 1 review minimum, squash merges).
- Feature branches: `feat/<short-desc>`; docs branches: `docs/<area>`.

**Commit messages**
- Conventional style (e.g., `docs(crc): add LotteryDraw responsibilities`).

**Pull Requests**
- Must link related stories (e.g., `US-07`) if affected.

---

## IDs & Naming Conventions

| Artifact                    | ID Pattern         | Example                  |
|-----------------------------|--------------------|--------------------------|
| User Story                  | `US-<NN>`          | `US-07`                  |
| Screen (UI mockup)          | `SCR-<Name>`       | `SCR-EventDetails`       |
| Storyboard (flow)           | `SBR-<Name>`       | `SBR-EntrantAcceptFlow`  |
| CRC Class                   | `<ClassName>`      | `LotteryDraw`            |

These IDs appear in filenames, and figure captions.

---