# Input system hookup guide

Status: **ACTIVE — modes + chat + amount modal wired; full ActionMapper cutover still open**

Package: `rt4.amilious.input` (not `rt4.input`)

Live behavior still uses `rt4.Keyboard` + interface `onKey` / CS2. Amilious owns **mode**, **Enter/QC policy**, **amount SPECIAL_MODAL**, and **whether keys are accepted** in `Protocol`.

This file is the checklist so work can resume without losing context.

---

## Goals

- Mode-based input (priority stack), not a single chat `focused` boolean.
- Device → frame → actions later (keyboard polling exists; gameplay still mostly vanilla).
- No keyboard walk (click-to-walk). Camera primarily mouse; optional key camera when allowed.
- Chat collapsed → WORLD; no Quick Chat from WORLD Enter.
- Amount / name / string dialogs → **SPECIAL_MODAL** with text input allowed.
- `ChatController` remains a thin adapter over `InputManager` where possible.

---

## Mode priority (highest first)

    MAIN_MENU → MAP → SPECIAL_MODAL → CHAT → WORLD

| Mode | How derived |
|------|-------------|
| MAIN_MENU | `client.gameState != 30` |
| MAP | `MapController.isOpen()` |
| SPECIAL_MODAL | `SpecialModalRegistry.isActive()` (amount chrome show/hide, etc.) |
| CHAT | `chatArmed` + chatbox visible |
| WORLD | fallback |

Tracked: `previousMode`, optional `ModeListener`, log via `InputManager.setLogModeChanges(true)`.

---

## Package layout

    rt4/amilious/input/
      InputMode.java
      InputConfig.java
      ChatboxState.java
      InputManager.java
      SpecialModalRegistry.java
      device/
        InputDevice.java
        KeyboardDevice.java
        GamepadDevice.java
      state/
        InputFrame.java
        InputButtons.java
      action/
        Action.java
        ActionMapper.java
        …

    rt4/amilious/
      ModalController.java      # amount dialog open/close
      ChatController.java       # adapter + visuals
      InputController.java      # AWT hotkeys (digits, Esc, tabs)
      AmiliousClient.java       # init / tick / forwards

---

## Done

### Lifecycle

- [x] `AmiliousClient.Init()` → `InputManager.init()`, flags, `ModalController.init()`
- [x] `AmiliousClient.update()` → `ModalController.tick()` (if any) then `InputManager.tick()`
- [x] `AmiliousClient.onLogin()` → `InputManager.onLogin()` / reset + chatbox defaults
- [x] `KeyboardDevice` can poll when `setPollDevices(true)`
- [x] `setProcessModeKeys(true)` — Enter/Esc arm/disarm chat

### Chat

- [x] Enter in WORLD → CHAT (`chatArmed`) when chatbox visible
- [x] Esc → leave CHAT
- [x] Submit → `notifyChatSubmit()` → WORLD (when configured)
- [x] Empty Enter disarm (`disarmChatIfNoSubmit`)
- [x] QC blocked from WORLD Enter (`shouldConsumeEnter` + `InterfaceList` onKey skip)
- [x] Chatbox tab collapse → WORLD (`ChatboxState` + tab clicks)
- [x] `ChatController.isFocused()` → `InputManager.isChatMode()`

### Amount dialog (SPECIAL_MODAL)

- [x] Chrome ids **49283074 / 49283075** (iface **752**); input **49283077**
- [x] Show/hide via CS2 `setHidden` → `AmiliousClient.onComponentHiddenChanged` → `ModalController`
- [x] Optional backup: mini-menu Withdraw-X / Cook X → arm
- [x] Clear on hide, `RESUME_P_COUNTDIALOG`, `closeWidget`
- [x] **Not** driven by `IF_OPENTOP` / `IF_SETHIDE` alone (752 stays loaded)

### Critical: key acceptance (Protocol)

- [x] **`Protocol` in-game key drain** must not discard keys when only chat focused
- [x] Use `InputManager.shouldAcceptTextInput()`:

    CHAT | SPECIAL_MODAL | MAIN_MENU  → fill keyQueue from Keyboard.nextKey()
    WORLD | MAP                         → only ':' / '/' open chat; else drain/discard

**Bug fixed:** amount could keyTyped but never onKey because WORLD discarded all keys.

### Other policy helpers

    shouldAcceptTextInput()  → CHAT | SPECIAL_MODAL | MAIN_MENU
    shouldConsumeEnter()     → WORLD | MAP (and consumeEnterThisFrame)
    shouldAllowWorldBinds()  → WORLD
    shouldSendKeyToChat()    → CHAT + chatbox visible
    isSpecialModalMode()     → SPECIAL_MODAL

### InputController

- [x] Digit binds skipped when chat focused **or** special modal
- [x] Esc: map → modal close → logout UI as designed

### Display / window (related branch work)

- [x] Borderless / exclusive fullscreen fixes (separate from input modes)
- [x] gl.setSwapInterval / config exposure (performance)

---

## Not done / partial

- [ ] Full **ActionMapper** driving camera / binds (vanilla Keyboard + InputController still primary)
- [ ] Gate all chat script input solely via shouldSendKeyToChat inside every path (Protocol accept + onKey Enter is main)
- [ ] Name / string dialogs same as amount (hooks exist; ids TBD)
- [ ] MouseDevice / real GamepadDevice
- [ ] Remove ChatController entirely
- [ ] Plugin API for modes / key intercept

---

## Important call sites (search these)

| Concern | Location |
|---------|----------|
| Key accept / discard | Protocol (~keyQueue fill, ChatController.isFocused / shouldAcceptTextInput) |
| Enter skip for QC | InterfaceList.method946 onKey loop + shouldConsumeEnter() |
| setHidden amount | ScriptRunner Cs2Opcodes.setHidden → onComponentHiddenChanged |
| IF_OPENTOP (logging only) | Protocol.method1148 → onInterfaceOpen |
| Menu X backup | MiniMenu.doAction → onMiniMenuAction |
| Amount submit | ScriptRunner sendIntegerInput / RESUME_P_COUNTDIALOG |
| Mode tick | AmiliousClient.update → InputManager.tick |
| Hotkeys | InputController KeyAdapter |
| Chat visuals | ChatController.getChatColor / alpha / focus indicator |

---

## Amount dialog reference

| Packed id | Iface:child | Role |
|-----------|-------------|------|
| 49283074 | 752:2 | Chrome — hidden=false when open |
| 49283075 | 752:3 | Chrome — same |
| 49283077 | 752:5 | Text input (*) — receives onKey |
| 49283079 | 752:… | Inverse hide vs chrome (optional) |
| 48889860/61 | 746:… | Parent layers |

Open signal: **setHidden false** on 74/75 (preferred), not OPENTOP.
Close: **setHidden true**, submit, or close widget.

---

## Config (InputConfig)

- enabled
- enterOpensChat / escapeClosesChat
- autoWorldAfterSend
- allowQuickChatOnEmptyEnter
- forceWorldWhenChatHidden

Optional later: mirror into GlobalJsonConfig.

---

## Debug (remove when stable)

| Log | File |
|-----|------|
| [Keyboard.keyTyped] | Keyboard |
| [onKey] | InterfaceList.method946 |
| [keys] | client.mainUpdate |
| [IF_OPENTOP] | Protocol.method1148 |
| [modal] amount option… | ModalController |
| [input] mode … | InputManager — setLogModeChanges(false) to silence |

---

## Debug checklist

- [x] Login → MAIN_MENU then WORLD
- [x] Enter → CHAT; type + Enter → send → WORLD
- [x] WORLD Enter does not open QC
- [x] Esc closes CHAT
- [x] Collapse chat → no CHAT arm
- [x] Withdraw-X → SPECIAL_MODAL; digits → field 49283077; Enter submits → WORLD
- [x] Protocol accepts keys in SPECIAL_MODAL
- [ ] Name/string dialogs
- [ ] ActionMapper owns at least one bind end-to-end
- [ ] ChatController removable

---

## Suggested remaining order

1. Delete debug prints; keep Protocol shouldAcceptTextInput.
2. Rely on setHidden for amount; drop mini-menu arm if stable.
3. Wire name/string dialogs like amount.
4. Point remaining ChatController / camera checks at shouldAllowWorldBinds().
5. Grow ActionMapper + KeyboardDevice; then cut hotkeys over gradually.
6. Deprecate ChatController when nothing reads focused for routing.

---

## Search terms when resuming

    shouldAcceptTextInput
    shouldConsumeEnter
    SpecialModalRegistry
    onComponentHiddenChanged
    49283074
    49283077
    ChatController.isFocused
    keyQueueSize
    InputManager.tick
    ModalController
    Cs2Opcodes.setHidden

---

## One-sentence summary

Modes and amount/chat text routing are live (especially Protocol key accept + setHidden amount); next is cleanup, more modals, and ActionMapper cutover without breaking SPECIAL_MODAL or WORLD key discard policy.
```
