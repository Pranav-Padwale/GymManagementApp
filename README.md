# 🏋️ Gym Management & Member Application (KMP MVC)

A modern, cross-platform **Gym Management & Member Application** built using **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**, strictly adhering to the **Model-View-Controller (MVC)** architectural pattern.

---

## 🏛️ Architectural Contract (Model-View-Controller)

This application enforces strict separation of concerns across three core architectural roles:

1. **Model**: Represents the data layer, Room KMP SQLite databases, repositories, and core business logic (e.g., Mifflin-St Jeor TDEE formulas, macro calculations, financial balance sheet calculations). The Model is completely independent of the UI and Controllers.
2. **View**: Passive Compose Multiplatform screens (`ActivityDashboardScreen`, `EditGoalScreen`, `MemberManagementScreen`, `FinanceDashboardScreen`, `HamburgerSideDrawer`). Views hold **zero** structural state or calculation logic; they strictly render data passed to them by the Controller and forward user events via callbacks.
3. **Controller**: Dedicated Kotlin classes (`ActivityController`, `GoalController`, `MemberManagementController`, `FinanceController`, `DrawerController`) that act as the glue. Controllers process View callbacks, update the Model layer asynchronously, and publish updated immutable `StateFlow<UiState>` back to the Views without relying on Android `ViewModel` or MVI intent pipelines.

---

## 🚀 Key Features & Modules

### 1. 🎯 Member Fitness & Nutrition Activity Dashboard
- **Donut Ring Calorie Chart**: Custom Canvas multi-color ring rendering target vs eaten vs burned calories (`2557.5 Calories left`).
- **Macro Nutrient Summary**: Macro pill indicators for 🥖 **Carbs** (60.0%), 🥩 **Protein** (25.0%), and 🥑 **Fat** (15.0%).
- **Health Data Permission Banner**: Interactive card for syncing steps and health metrics.
- **Nutrition Breakdown Table**: Detailed table showing total, consumed, and remaining grams per macro.
- **Calorie Consume/Burn Graph**: Custom Canvas line chart with series toggle switches (`Calories`, `Eaten`, `Burn`) and time range tabs (`7 Days`, `15 Days`, `30 Days`, `3 Months`, `6 Months`).
- **Dosh Test Section**: Badges for `KAPHA`, `PITTA`, and `VATA`.
- **Quick Action Grid**: Exercise and Yoga action cards.

### 2. 📅 Interactive Calendar Picker
- Clickable header date trigger (`Today, 25 July, 2026`).
- Month & year navigation controls (`<` Previous / Next `>`).
- 7-column day grid (1 to 31) with day of week headers (`S M T W T F S`).
- Active day selection highlighting, "Today" badge indicator, and quick **Jump to Today** button.
- Dynamic metric fetching upon date selection.

### 3. 🍔 Hamburger Side Navigation Drawer
- **User Profile Header**: Avatar circle with initial `P`, name (`pradnya`), phone (`+91 8010129371`), and Dark/Light mode 🌙 toggle.
- **Primary Route Links**: Instant access to `Fitness Activity`, `Member Status Directory`, and `Income & Expense Sheet`.
- **Secondary App Sections**: `Gym Plan`, `Health Data`, `ActiveX`, `Review/Rating`, `Store`, `Time Slot`, `Upcoming Meetings`, `Invite a Friend`, `Logout`.
- **Footer**: Version tag (`v1.0.0.53`).

### 4. 🎯 Edit Goal & BMR/TDEE Calculator
- **Body Type Selector**: `Female` vs `Male` toggle button.
- **Personal Info Inputs**: Outlined fields for `Age`, `My Height (cm)`, `My Weight (kg)`, and `Goal Weight (kg)` with live ideal weight calculation banner (`From 55 KG to 74 KG`).
- **Body Fat % / BMI Speedometer Gauge**: Custom Canvas semi-circle arc gauge with needle pointer and range legend (`Underweight`, `Normal`, `Overweight`, `Obesity`) updating real-time BMI (`BMI : 23.3`).
- **Weight Goal Selector**: 2x2 grid for `Gain`, `Maintain`, `Lose`, `Lose Keto`.
- **Activity Level Selector**: 2x2 grid for `Sedentary`, `Lightly`, `Moderately`, `Very`.
- **Mifflin-St Jeor Calculation Engine**: Tapping **View Result** computes daily TDEE:
  - `Calories` (2557.5 kcal)
  - `Fat` (42.62 Gram Per Day)
  - `Protein` (159.84 Gram Per Day)
  - `Carbs` (383.62 Gram Per Day)
  - Last Update timestamp (`2026-07-25`)

### 5. 👥 Member Status Management Directory
- Filter members by status: `ACTIVE`, `ENQUIRE`, `INACTIVE`, `EXPIRED`.
- Room KMP database persistence for member profiles.
- Dynamic status change dropdowns.

### 6. 📊 Income & Expense Balance Sheet Dashboard
- Net monthly balance summary card (Total Income vs Total Expenses).
- Category breakdown list with visual distribution bars (Personal Training Dues, Store Purchases, Administrative Expenses, Maintenance, Payroll).
- Interactive **Log New Expense** text input form modal.

---

## 📁 Project Structure

```
gym_management_app/
├── shared/                             # Pure KMP Shared Module
│   └── src/
│       └── commonMain/
│           └── kotlin/com/gym/
│               ├── model/              # Model Layer (Data & Logic)
│               │   ├── activity/       # Activity & Calorie Models
│               │   ├── drawer/         # User Profile & Drawer Models
│               │   ├── finance/        # Financial & Transaction Models
│               │   ├── goal/           # BmrCalculator & Goal Models
│               │   └── member/         # Member & Room KMP Entities
│               ├── controller/         # Controller Layer (Pure Kotlin Glue)
│               │   ├── activity/       # ActivityController & UiState
│               │   ├── drawer/         # DrawerController & UiState
│               │   ├── finance/        # FinanceController & UiState
│               │   ├── goal/           # GoalController & UiState
│               │   └── member/         # MemberManagementController & UiState
│               ├── view/               # View Layer (Passive Compose Screens)
│               │   ├── activity/       # Activity Dashboard & Canvases
│               │   ├── drawer/         # Hamburger Side Drawer
│               │   ├── finance/        # Balance Sheet Dashboard
│               │   ├── goal/           # Edit Goal Screen & BMI Gauge
│               │   ├── member/         # Member Management Screen
│               │   └── theme/          # GymTheme Color Palette
│               └── App.kt              # App Navigation Root
├── desktopApp/                         # Desktop Runner Module
│   └── src/main/kotlin/Main.kt         # JVM Main Entry Point
├── build.gradle.kts                    # Root Build Configuration
├── settings.gradle.kts                 # Project Settings
└── .gitignore                          # Source Control Ignore File
```

---

## 💻 Prerequisites & Environment Setup

- **Java Development Kit (JDK)**: JDK 17 or JDK 21
- **IDE**: Android Studio (Jellyfish/Koala+), IntelliJ IDEA (2024.1+), or Fleet
- **Gradle**: 8.x / 9.x (Wrapper included)

---

## 🛠️ How to Build and Run

### Running via IntelliJ IDEA / Android Studio (GUI)
1. Open **Android Studio** or **IntelliJ IDEA**.
2. Click **File ➔ Open** and select the project directory:
   `C:\Users\prana\.gemini\antigravity\scratch\gym_management_app`
3. Wait for Gradle sync to complete.
4. Navigate to `desktopApp/src/main/kotlin/Main.kt` and click the **Green Play Button ▶️** next to `fun main()`.

### Running via Terminal / Command Prompt
1. Open PowerShell or Command Prompt.
2. Navigate to the project folder:
   ```powershell
   cd C:\Users\prana\.gemini\antigravity\scratch\gym_management_app
   ```
3. Execute the desktop runner task:
   ```powershell
   gradle :desktopApp:run
   ```

---

## 📄 License
This project is developed for Gym Management & Member Application architecture demonstration.
