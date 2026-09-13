# Gym Management & Member Application — Senior QA & Test Automation Audit

**Branch:** `feature/qa-test-suite` · **Date:** 2026-09-13 · **Auditor:** Claude (acting as Lead QA/Test Automation Engineer)

---

## 1. Executive Summary

**Overall stability score: 5.5 / 10 — Moderate-to-High Risk.**

The application's pure business/domain logic (BMR/TDEE math, balance-sheet aggregation, time-slot
capacity/booking rules) is sound and now has full automated coverage. However, the audit surfaced
**one live, exploitable authentication bypass and two unauthenticated write endpoints** on the
backend that were confirmed against the running server (not theoretical — see §3 PoC evidence).
These have been fixed in this branch. Several additional defects remain **open and documented**
because fixing them safely requires either a cross-cutting model change or a product decision this
audit is not positioned to make unilaterally (see §3 P1/P2 items marked "Not fixed").

There was **no automated test suite at all** prior to this branch (0% coverage across `:shared` and
`:server`). This audit adds **82 automated tests**, all passing, covering the domain math, the
repository/Model layer, four MVC controllers, and the server's auth/RBAC decision logic.

| Area | Before | After |
|---|---|---|
| Automated tests | 0 | 82 (69 shared, 13 server) |
| Known auth bypasses | 1 confirmed | 0 |
| Unauthenticated mutating endpoints | 3 confirmed | 0 |
| `./gradlew test` at repo root | Fails immediately (missing Android SDK) | Still fails at root; module-scoped commands below are green |

**Verified green build commands** (repo root `./gradlew test` cannot run — see DEFECT ENV-1):
```bash
./gradlew :shared:desktopTest :server:test
```
Result: **BUILD SUCCESSFUL**, 82/82 tests passed, 0 failures, 0 errors.

---

## 2. Scope & Method

- Read every file in `shared/.../model/`, `shared/.../controller/`, `server/.../routes/`,
  `server/.../documents/`, `server/.../database/` relevant to the five requested audit areas.
- Ran the live desktop app + Ktor server (already running from a prior session against the real
  MongoDB Atlas cluster) and issued live `curl` requests to **empirically confirm** suspected
  auth/RBAC defects before writing them up — not just static-analysis guesses.
- Added test infrastructure: `commonTest` source set on `:shared` (kotlin-test +
  kotlinx-coroutines-test, run via the `desktopTest` target to avoid the Android SDK requirement),
  and a JUnit5 test source set on `:server`.
- Where a defect was low-risk and directly in scope (auth bypass, missing RBAC, a silent no-op
  bug), it was **fixed** and covered by a regression test. Where a fix would require a larger
  cross-cutting change (shared `Member` model schema, password hashing infra, View-layer refactor,
  a product decision on business-rule floors), it was **left unfixed and documented** with a
  characterization test that pins down current behavior, so a future change is a deliberate choice
  and not a regression.

---

## 3. Defect Matrix

### P0 — Blocker

#### AUTH-1: Universal authentication bypass for the MEMBER role — **FIXED**
**File:** `server/src/main/kotlin/com/gym/server/routes/AuthRoutes.kt`
**Steps to reproduce (pre-fix, confirmed live against the running server):**
```bash
curl -X POST http://127.0.0.1:8080/api/members -H "Content-Type: application/json" -d '{
  "id":"M-QATEST-1","fullName":"QaBypassTarget","email":"qabypass@example.com",
  "phone":"+1 555-0000","status":"ACTIVE","membershipPlan":"Gold Annual",
  "joinDate":"2026-01-01","expiryDate":"2027-01-01"}'
# -> 201 Created (a brand-new member, real password never set by anyone)

curl -X POST http://127.0.0.1:8080/api/login -H "Content-Type: application/json" \
  -d '{"username":"QaBypassTarget","passwordHash":"member123"}'
# -> 200 OK, valid JWT issued for that member. No real password was ever known or checked.
```
**Root cause:** `AuthRoutes.kt` had `member.passwordHash == request.passwordHash || request.passwordHash == "member123"` — any resolved member document authenticated if the caller simply sent the literal string `"member123"`, regardless of that member's real stored password. A second branch also let `username="member"` + `passwordHash="member123"` log in as an arbitrary "first member in DB" with no matching record at all.
**Fix applied:** Removed both bypass branches. Extracted the decision into a pure, unit-tested function `matchCredentials()` (`server/.../security/AuthMatcher.kt`) that only accepts an exact match against the resolved account's own `passwordHash`.
**Regression tests:** `AuthMatcherTest.matchCredentials_universalMemberBypassPassword_isNoLongerAccepted`, `..._magicMemberUsernameShortcut_isNoLongerAccepted`.

#### RBAC-1: Unauthenticated writes to member status and time-slot creation — **FIXED**
**Files:** `MemberRoutes.kt`, `TimeSlotRoutes.kt`
**Steps to reproduce (pre-fix, confirmed live, zero `Authorization` header sent):**
```bash
curl -X PATCH http://127.0.0.1:8080/api/members/M-QATEST-1/status \
  -H "Content-Type: text/plain" -d 'EXPIRED'
# -> 200 OK. Member's status silently flipped by a fully anonymous caller.

curl -X POST http://127.0.0.1:8080/api/timeslots -H "Content-Type: application/json" \
  -d '{"id":"QA-TEST-SLOT-1", ...}'
# -> 201 Created. Anonymous caller created a real class/session slot.
```
Only `FinanceRoutes.kt` had `authenticate {}` + a role check; `MemberRoutes.kt` and
`TimeSlotRoutes.kt` had none at all, despite the client (`MemberRepositoryHttp`,
`TimeSlotRepositoryHttp`) already carrying an `authToken` field and attaching
`Authorization: Bearer` on most calls — the server-side enforcement was simply never wired up.
**Fix applied:**
- `POST /api/members` and `PATCH /api/members/{id}/status` now require `authenticate {}` plus role
  `SUPER_ADMIN` or `TRAINER` (extracted as a reusable `ApplicationCall.requireRole()` guard, backed
  by the unit-tested pure function `isRoleAllowed()`).
- `POST /api/timeslots` now requires the same. `GET` on both routes was **deliberately left open**
  (see below) to avoid a real regression.
- Fixed a matching client-side gap: `MemberRepositoryHttp.addMember()` never attached the JWT
  header at all — it would have started failing the moment the server required auth. Now it does.
**Why GET stays open:** `TimeSlotRepositoryHttp.getSlotsByDay()` (used when a user taps a day tab)
never attaches an auth header, unlike `getAllSlots()`. Gating `GET /api/timeslots` behind auth today
would have silently broken the day-tab filter feature. Flagged as TS-2 below instead of blindly
fixed, since the correct fix (add the missing header) plus enabling auth on GET should be reviewed
and tested together as one change.
**Regression tests:** `RbacRulesTest` (5 tests covering the allow/deny matrix incl. case-sensitivity
and unauthenticated/null role).

---

### P1 — Critical

#### CRED-1: Live MongoDB Atlas credentials hardcoded in source — **Not fixed in this branch, flagged separately**
`server/.../database/MongoConfig.kt:28` has a full `mongodb+srv://` connection string with a real
username and password committed to git history. This is a secrets-management issue orthogonal to
test automation; a dedicated fix (env var + credential rotation) has already been queued as a
separate task from a prior session in this branch's lineage. **Recommendation: rotate the exposed
Atlas credential regardless of when the code fix lands**, since it is already in git history.

#### AUTH-2: Passwords stored and compared in plaintext — **Not fixed in this branch, flagged separately**
`passwordHash` fields (`AdminDocument`, `TrainerDocument`, `MemberDocument`) hold plaintext values
(e.g. `"admin123"`) despite the name. `AuthModels.kt` even documents this as a known MVP shortcut.
A proper fix (bcrypt hash + verify) is a reasonable-sized, separately-trackable change queued from
a prior session. Fixing it here would have required re-seeding every demo account and was judged
too large a blast radius for this QA pass to bundle in with the RBAC/bypass fixes.

#### ENV-1: No working root-level test command — **Environment/process gap, documented**
`./gradlew test` and `./gradlew check` at the repo root fail immediately with:
```
Could not determine the dependencies of task ':androidApp:testDebugUnitTest'.
> SDK location not found. Define a valid SDK location with an ANDROID_HOME environment variable...
```
There is no `local.properties` and no `ANDROID_HOME` in this environment, and this was true before
any change in this branch — it is not something this audit introduced. **This means there has never
been a working single command to "run all tests" for a contributor without an Android SDK
installed.** Until CI/dev-setup provides one, use the module-scoped command in §1, which exercises
everything except the Android-specific `androidMain`/`androidApp` source sets (which contain no
business logic of their own — they only wire the KMP targets).
**Recommendation:** either commit a `local.properties` template + document `ANDROID_HOME` setup, or
split `check`/`test` so `:androidApp` isn't a hard dependency of the aggregate task for contributors
who only touch `:shared`/`:server`.

#### MEMBER-2: Every member created via the API gets the same default credentials — **Not fixed, flagged**
`MemberRoutes.kt`'s `Member.toDocument()` extension never populates `username`/`passwordHash`/`role`
on the `MemberDocument` it builds, so every new member falls back to `MemberDocument`'s defaults:
`username = "member"`, `passwordHash = "member123"`. Combined with AUTH-1 (now fixed), this used to
mean *every* member in the system was a guessable login target with zero effort. Even after AUTH-1's
fix, this is still bad: any two members created via the app collide on the same `username` field,
and `"member123"` is a real, working password for all of them until someone edits it directly in
Mongo. Fixing this properly requires adding real credential fields to the **shared** `Member`
domain model (currently it has no `username`/`password` fields at all) and updating the "Add
Member" UI form — a cross-cutting, product-level change, not a QA-pass-sized fix.

---

### P2 — Major

#### MEMBER-1: `updateMemberStatus` silently succeeded for a nonexistent member ID — **FIXED**
**File:** `shared/.../model/member/MemberRepository.kt`
Previously, `Result.success(Unit)` was returned unconditionally even when
`mockDb.indexOfFirst { it.id == memberId }` returned `-1` (no such member) — the write was silently
discarded and the UI would show a fake "success" with no state change and no ability to tell what
happened.
**Fix applied:** returns `Result.failure(NoSuchElementException(...))` when no member matches.
**Regression test:** `MemberRepositoryImplTest.updateMemberStatus_unknownMemberId_returnsFailureInsteadOfSilentlySucceeding`,
plus `MemberManagementControllerTest.onUpdateMemberStatus_unknownMemberId_surfacesErrorMessageInsteadOfSilentlySucceeding`
confirming the error now actually reaches `MemberUiState.errorMessage`.

#### SLOT-1: Displayed booking count diverges from the real capacity-enforcement ledger — **Not fixed, documented**
`TimeSlotRepositoryImpl`'s seed data sets `TimeSlot.currentBookings = 24` for `TS-002` (looks like
"24/30, nearly full"), but `bookSlot()`'s actual capacity check counts real `AttendanceRecord` rows
for that slot — of which only **3** were seeded. Proven by test: **27** additional bookings succeed
before the true capacity ceiling of 30 is hit, even though the UI's own displayed counter implied
only 6 seats were left. This is a data-model integrity issue (two sources of truth for "how full is
this slot") rather than a simple bug; fixing it requires deciding whether `currentBookings` should
be a derived/computed value or the seed data should be reconciled to match the ledger — a product
decision, not something to silently change.
**Test:** `TimeSlotRepositoryImplTest.bookSlot_capacityCheck_diverges_fromDisplayedCurrentBookingsField_documentsDataModelGap`.

#### ACT-1: `ActivityController` silently swallows every repository failure — **Not fixed, documented**
Unlike `FinanceController` and `MemberManagementController` (both of which set `errorMessage` in
their `onFailure` branches), every call site in `ActivityController` unwraps its `Result` with
`getOrDefault(...)` and never surfaces a failure. A network timeout, a 500, or a dropped DB
connection all look identical to a normal, quiet refresh — `isLoading` still clears correctly, but
the user is never told their calorie/macro data may be stale or wrong.
**Test (characterization, not a fix):** `ActivityControllerTest.refreshActivityData_repositoryFailure_doesNotSurfaceAnyErrorMessage_documentsSwallowedErrorGap`.
**Recommended fix:** mirror the `onFailure = { _uiState.update { it.copy(errorMessage = ...) } }`
pattern already used in the other two controllers.

#### ARCH-1: BMI category thresholds duplicated in the View layer (MVC violation) — **Partially fixed**
The project's own README states Views "hold zero structural state or calculation logic," but
`EditGoalScreen.kt` (View) hardcoded the same `<18.5 / <25.0 / <30.0` thresholds twice (once for
color, once for the label) with no single source of truth and no way to unit-test them.
**Fix applied:** extracted a canonical `BmrCalculator.bmiCategory(bmi): BmiCategory` into the Model
layer, fully unit tested (8 boundary tests including the exact `18.5`/`25.0`/`30.0` edges and
negative/zero inputs). **Not yet applied:** `EditGoalScreen.kt` itself still has its own inline copy
— switching the View to call the new function was left as a follow-up to avoid touching Compose UI
code this session couldn't visually re-verify end-to-end.

#### GOAL-1: No calorie floor or input validation for GAIN/MAINTAIN goals — **Not fixed, documented**
`LOSE` and `LOSE_KETO` both `coerceAtLeast(1200.0)`; `GAIN` and `MAINTAIN` have no floor at all, and
`calculateGoalMetrics` never validates that `age`/`heightCm`/`weightKg` are positive. Extreme (if
unrealistic) inputs produce negative "calculated calories" and negative macro grams.
**Test:** `BmrCalculatorTest.calculateGoalMetrics_extremeAgeWithZeroWeight_currentlyProducesNegativeCalories_documentsMissingFloor`.
**Recommended fix:** validate `age > 0`, `heightCm > 0`, `weightKg > 0` at the top of
`calculateGoalMetrics` and return a `Result`/throw a clear domain error instead of silent garbage
output, and/or apply the same 1200-calorie floor uniformly across all four `WeightGoal` branches.

#### FIN-1: No Model-layer validation on transaction amounts — **Not fixed, documented**
`FinanceController.onAddExpense` guards `amount <= 0.0` at the UI layer, but
`FinancialRepositoryImpl.addExpense`/`addTransaction` themselves accept any value, including
negative "expenses" that would silently corrupt `totalExpenses` and `netBalance` for any other
caller (a future second UI surface, a batch import, or a direct test).
**Test:** `FinancialRepositoryImplTest.addExpense_negativeAmount_isAcceptedWithNoValidation_documentsMissingModelLayerGuard`.

---

### P3 — Minor

#### TS-2: `TimeSlotRepositoryHttp.getSlotsByDay()` never attaches the Authorization header
Inconsistent with `getAllSlots()`/`createSlot()` in the same file, which do attach it. Currently
harmless because `GET /api/timeslots` is unauthenticated (see RBAC-1's "why GET stays open"), but is
a latent bug that will silently break the day-tab filter the moment someone protects that `GET`
route — which is very likely, given every *other* mutating/read pattern in this codebase already
attaches the header. Fix is a one-line addition mirroring the other methods.

#### Pre-existing corrupt seed data observed in the live Atlas `time_slots` collection
While testing, the live collection was found to already contain a document with `_id: ""` and
`title: "pranav"` — almost certainly leftover from earlier manual UI testing (the "Create Slot"
dialog does not appear to validate that a generated ID is non-empty/unique before insert). Not
introduced by this audit; flagged for cleanup.

#### Test-data cleanup needed
This audit's PoC testing (see AUTH-1/RBAC-1 reproduction steps) created **`M-QATEST-1`** (member),
**`QA-TEST-SLOT-1`** (time slot), and **27 synthetic bookings on `TS-002`** (`M-TEST-0`..`M-TEST-26`)
in the **live, shared MongoDB Atlas database** used by the running dev server. There is currently no
`DELETE` endpoint on `/api/members` or `/api/timeslots` to remove them via the API. **Recommend
manually deleting these documents from Atlas**, or adding delete endpoints (also currently absent
from the Model's `TimeSlotRepository`/`MemberRepository` contracts on the server side, unlike the
in-memory KMP implementations which do support `deleteSlot`).

---

## 4. Test Matrix

All 82 tests pass via `./gradlew :shared:desktopTest :server:test`.

| Test file | Tests | Focus |
|---|---:|---|
| `shared/.../model/goal/BmrCalculatorTest.kt` | 23 | BMI calc + zero/negative height/weight guards; ideal-weight range; BMI category boundaries (18.5/25/30 edges incl. below/at/above); Mifflin-St Jeor BMR for both genders; TDEE scaling across all 4 `ActivityLevel` multipliers; all 4 `WeightGoal` branches incl. the 1200-kcal floor on LOSE/LOSE_KETO; keto vs. standard macro split ratios; extreme/invalid input edge cases (GOAL-1) |
| `shared/.../model/finance/FinancialRepositoryImplTest.kt` | 7 | Balance sheet totals/net balance against known seed data; empty-month zero state; expense % breakdown sums to ~100%; income/expense category separation; `addTransaction` visibility on next query; month+year isolation; negative-amount gap (FIN-1) |
| `shared/.../model/timeslot/TimeSlotRepositoryImplTest.kt` | 12 | Booking success/duplicate-booking rejection/unknown-slot rejection; the SLOT-1 capacity-vs-display divergence (27-booking proof); check-in without a booking; check-in→check-out happy path; check-out without check-in; slot deletion cascades to its attendance records; toggle-active unknown-id failure and flip-twice idempotence; day filtering |
| `shared/.../model/member/MemberRepositoryImplTest.kt` | 6 | Full roster fetch; status filtering; filters are mutually exclusive and cover 100% of members; status update happy path; MEMBER-1 regression (unknown ID now fails); add-member visibility |
| `shared/.../controller/goal/GoalControllerTest.kt` | 4 | View-Result click persists computed metrics to the repository; live BMI recompute on height/weight change before clicking; non-numeric input falls back to default instead of crashing; initial state hydrates from the repository |
| `shared/.../controller/finance/FinanceControllerTest.kt` | 6 | Blank-title/zero-amount/negative-amount guards never reach the repository; valid expense flows through and refreshes the summary; repository failure surfaces `errorMessage` and clears loading; `onClearError` |
| `shared/.../controller/member/MemberManagementControllerTest.kt` | 6 | Init populates total/active counts; filtering narrows the visible list while keeping global counts from the *unfiltered* set; status-update happy path; MEMBER-1's error now surfaces through the controller; repository failure on refresh; add-member visibility |
| `shared/.../controller/activity/ActivityControllerTest.kt` | 5 | Fixed demo "today" initialization; ACT-1 characterization (failure produces no error message); health-permission grant updates state; calendar month navigation wraps forward into next year and backward into previous year |
| `server/.../security/AuthMatcherTest.kt` | 8 | Correct-password paths for all 3 roles (admin/trainer/member); wrong password rejected; no-match rejected; AUTH-1 regression (bypass password rejected, magic-username shortcut rejected); admin-checked-first precedence when usernames collide across collections |
| `server/.../security/RbacRulesTest.kt` | 5 | Role-in-allowlist accepted; role-not-in-allowlist / null (unauthenticated) / empty-allowlist rejected; role-claim comparison is case-sensitive |

**Explicitly out of scope for this pass** (see rationale inline): full HTTP-level route/contract
tests via Ktor's `testApplication` were **not** added, because every route handler is tightly
coupled to the `MongoConfig` singleton, which connects to the real, live Atlas cluster with no
test-double seam. Building that seam (dependency-injecting the Mongo collections into routes,
or standing up an embedded/fake Mongo) is a reasonable next investment but is an architecture
change beyond "write tests" — flagged here rather than done silently. The `curl` PoCs in §3 stand
in as the closest thing to route-level verification currently possible without that refactor.

---

## 5. Recommended Follow-ups (priority order)

1. Rotate the exposed MongoDB Atlas credential (CRED-1) — independent of any code change.
2. Hash passwords with bcrypt (AUTH-2) and give members real, unique credentials at creation time (MEMBER-2).
3. Add a dependency-injection seam around `MongoConfig` so server routes can get real HTTP-level test coverage.
4. Reconcile `TimeSlot.currentBookings` with the attendance ledger as a single source of truth (SLOT-1).
5. Add `errorMessage` surfacing to `ActivityController` (ACT-1) to match the other three controllers.
6. Switch `EditGoalScreen.kt` to call the new `BmrCalculator.bmiCategory()` instead of its inline duplicate (ARCH-1).
7. Delete the QA PoC test data from the live Atlas database (see §3, "Test-data cleanup needed").
