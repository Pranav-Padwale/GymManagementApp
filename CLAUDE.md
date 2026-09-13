# Gym Management App — Developer & Claude Code Guide

## 📌 Project Overview
A cross-platform **Gym Management & Member Application** built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**, strictly following the **Model-View-Controller (MVC)** architectural pattern.

- **Frontend**: Compose Multiplatform targeting Desktop (JVM) and Android (with iOS architecture ready).
- **Backend**: Ktor 3.0 (CIO engine), MongoDB Kotlin Coroutine driver, JWT Authentication, and REST APIs.
- **Offline / Local Storage**: Room KMP SQLite database for offline member profiles & data.

---

## 🏛️ Architectural Contract (Strict MVC)
Always preserve separation of concerns:

1. **Model** (`shared/.../model/`, `server/.../documents/`):
   - Pure domain logic (e.g. Mifflin-St Jeor TDEE formulas, BMI, finance metrics).
   - Data entities (Room KMP SQLite, MongoDB BSON documents, Kotlinx Serialization DTOs).
   - Independent of UI and Controllers.
2. **View** (`shared/.../view/`):
   - Passive Compose Multiplatform screens and widgets.
   - **Zero** persistent business state or calculation logic.
   - Strictly renders data provided by controllers and emits user interactions via callbacks.
3. **Controller** (`shared/.../controller/`):
   - Pure Kotlin classes acting as the glue between Views and Models/Repositories.
   - Manages state via `StateFlow<UiState>` and coroutine scopes.
   - **Do NOT** use Android `ViewModel` or complex MVI intent frameworks.

---

## 🛠️ Build, Run & Test Commands

Run from the repository root:

- **Run Desktop App**:
  ```bash
  ./gradlew :desktopApp:run
  ```
- **Run Backend Server (Ktor)**:
  ```bash
  ./gradlew :server:run
  ```
- **Build Android App**:
  ```bash
  ./gradlew :androidApp:assembleDebug
  ```
- **Run All Verification & Unit Tests**:
  ```bash
  ./gradlew check
  ```
- **Build Shared Module**:
  ```bash
  ./gradlew :shared:build
  ```

---

## 📁 Repository Structure

```text
gym_management_app/
├── shared/                             # Pure KMP Shared Module
│   └── src/
│       ├── commonMain/kotlin/com/gym/
│       │   ├── api/                    # HTTP & Ktor Client Repositories
│       │   ├── controller/             # Pure Kotlin MVC Controllers (UiState)
│       │   ├── model/                  # Data classes, Room entities & business logic
│       │   ├── view/                   # Passive Compose Multiplatform UI screens
│       │   └── App.kt                  # Root App Navigation & RBAC auth router
│       ├── androidMain/                # Android-specific implementations
│       ├── jvmMain/                    # Desktop JVM-specific implementations
│       └── iosMain/                    # iOS target declarations
├── server/                             # Ktor 3.0 Backend Module
│   └── src/main/kotlin/com/gym/server/
│       ├── database/                   # MongoDB client & collections
│       ├── documents/                  # BSON Mongo documents
│       ├── routes/                     # AuthRoutes, MemberRoutes, TimeSlotRoutes
│       └── Application.kt              # Ktor server entry point
├── desktopApp/                         # Desktop JVM Runner
│   └── src/main/kotlin/Main.kt
├── androidApp/                         # Android App Runner
├── .agents/
│   └── AGENTS.md                       # Workspace agent rules
└── CLAUDE.md                           # Claude Code guidelines (this file)
```

---

## 🌿 Strict Git & Branching Strategy
Adhere strictly to the workspace guidelines in `.agents/AGENTS.md`:

1. **NEVER commit or push directly to `main`.**
2. **Always create a dedicated feature branch** before making changes:
   ```bash
   git checkout -b feature/<feature-name>
   ```
3. **Push only to the feature branch** on remote:
   ```bash
   git push -u origin feature/<feature-name>
   ```
4. **Merge via Pull Requests**:
   - Open a GitHub Pull Request to merge the feature branch into `main`.
