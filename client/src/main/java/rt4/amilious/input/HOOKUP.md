# Input system hookup guide

Status: PARTIALLY HOOKED (background only)

- rt4.input is designed to run in the client tick via AmiliousClient.
- Lifecycle calls may be live (init / tick / onLogin).
- Input reading is NOT hooked — do not read Keyboard.pressedKeys / mouse for this system yet.
- Live client behavior still uses rt4.Keyboard + rt4.amilious.ChatController.

This file is the checklist for wiring the new system without losing context mid-work.

---

## Goals

- Mode-based input: WORLD vs CHAT (not a single focus boolean).
- Device to frame to actions (keyboard now; mouse/gamepad later).
- No keyboard walk actions (RS is click-to-walk). Camera is primarily mouse.
- Chat collapsed => stay in WORLD; no accidental Quick Chat from WORLD.
- Old ChatController stays until cutover is proven.

---

## Package layout (target)

    rt4/input/
      HOOKUP.md
      InputMode.java
      InputConfig.java
      ChatboxState.java
      InputManager.java
      device/
        InputDevice.java
        KeyboardDevice.java
        MouseDevice.java
        GamepadDevice.java
      state/
        InputFrame.java
      action/
        Action.java
        Binding.java
        ActionMapper.java

---

## Currently hooked (background only)

These keep the system alive without changing gameplay.

- [x] AmiliousClient.Init() -> InputManager.init()
- [x] AmiliousClient.update() -> InputManager.tick()
  tick() must call beginFrame(false, false) (or equivalent) so no Enter/Esc edges
  are synthesized from real keys yet
- [x] AmiliousClient.onLogin() -> InputManager.onLogin() / reset() + chatbox defaults

### Explicitly not hooked yet

- [ ] Reading Keyboard.pressedKeys into the input system
- [ ] Keyboard gating (block keys from chat in WORLD)
- [ ] Chat submit / empty Enter / Quick Chat
- [ ] Chatbox visibility (component read or tab-click)
- [ ] Replacing ChatController consumers
- [ ] KeyboardDevice.poll / MouseDevice / gamepad
- [ ] ActionMapper driving camera or binds

---

## What must be hooked (in order)

### 1. Per-frame tick

Where: rt4.amilious.AmiliousClient.update()
(called from client.mainLoop after PluginRepository.Update())

What:

    InputManager.tick();

Background behavior (now):

    InputManager.beginFrame(false, false);
    InputManager.endFrame();

Later (still no gating): pass real key state for mode testing only:

    InputManager.beginFrame(
        Keyboard.pressedKeys[Keyboard.KEY_ENTER],
        Keyboard.pressedKeys[/* ESC code */]
    );

Done when: mode can be logged each frame; with false,false it stays WORLD and does nothing.

---

### 2. Keyboard gating (stop keys leaking into chat)

Where: rt4.Keyboard (keyTyped / keyPressed / queues that feed chat + CS2).

What:

- InputManager.shouldSendKeyToChat()
  Only then append chars to chat buffer / chat scripts
- InputManager.shouldConsumeEnter()
  Do not deliver Enter to vanilla empty-line handler
- InputManager.shouldBlockQuickChat()
  Block QC when policy says no
- InputManager.shouldAllowWorldBinds()
  F-keys / custom binds / optional key-camera

Done when: WORLD mode typing does not open chat or QC; CHAT mode still types.

---

### 3. Chat submit / empty Enter (Quick Chat landmine)

Where: hunt in client for empty-line Enter -> Quick Chat
(QuickChatPhrase, activePhrase, interface 751, CS2 / ScriptRunner paths).

What:

- Non-empty send -> InputManager.notifyChatSubmit() (auto WORLD if config says so).
- Empty Enter -> InputManager.notifyEmptyEnter(); only open QC if it returns true.

Done when: Enter in WORLD never opens QC; empty Enter in CHAT follows
InputConfig.allowQuickChatOnEmptyEnter.

---

### 4. Chatbox visibility (collapsed HD chat)

Where:

- Preferred: InterfaceList.getComponent(...).hidden / height for a chat body child on interface 751.
- Fallback: tab-click tracking (ChatboxState.onChatTabClicked).

Tab component ids (packed):

- All: 49217538
- Game: 49217541
- Public: 49217544
- Private: 49217548
- Clan: 49217552
- Trade: 49217556
- Assist: 49217560

What: keep ChatboxState.setVisible(true/false) accurate every tick or on click.

Done when: collapsed chat forces WORLD; Enter does not open CHAT while collapsed.

---

### 5. Click-to-focus chat (optional)

Where: InterfaceList click handling (where Amilious logs component ids) /
AmiliousClient.onInterfaceButton.

What: click on chat input / chat root -> InputManager.enterChatMode() if visible.

Done when: click arms chat without needing Enter.

---

### 6. Replace ChatController call sites

Where: search for ChatController.

Known:

- GameShell.mainInputLoop — key camera when chat not focused -> use
  shouldAllowWorldBinds() or mode check (camera is mouse-primary; key camera optional).
- Chat draw — getChatColor / getChatAlpha / drawChatFocusIndicator -> drive from
  isChatMode() instead of isFocused().

Done when: no gameplay path depends on ChatController.focused for routing
(can leave class as deprecated wrapper).

---

### 7. Devices (after modes work)

- KeyboardDevice — Keyboard state each poll — fill InputFrame buttons
- MouseDevice — Mouse click/drag — camera drag, UI click data
  (walk stays existing pipeline initially)
- GamepadDevice — stub, then JInput/etc. later — same actions, different bindings

Done when: gameplay reads ActionMapper.isDown(Action....) for at least one path
(e.g. OPEN_CHAT).

---

## What should NOT be hooked yet

- Do not remove rt4.Keyboard AWT listeners.
- Do not rewrite click-to-walk / scene picking.
- Do not delete ChatController until parallel testing is OK.
- Do not add WASD move actions (there is no keyboard walk).
- Do not gate chat input until step 2 is intentional.

---

## Action set (keep lean)

Chat: OPEN_CHAT, CLOSE_CHAT, SUBMIT_CHAT, QUICK_CHAT (optional)
System: ESCAPE
Hotkeys: add only as needed
Camera: optional key binds only — primary camera is mouse
Not included: MOVE_* (click-to-walk)

---

## Config (InputConfig)

- enabled — false = vanilla-like behavior
- enterOpensChat — WORLD + Enter -> CHAT
- escapeClosesChat — Esc -> WORLD
- autoWorldAfterSend — after submit -> WORLD
- allowQuickChatOnEmptyEnter — empty Enter in CHAT may open QC
- forceWorldWhenChatHidden — collapsed => WORLD

Later: mirror into GlobalJsonConfig if desired.

---

## Suggested cutover strategy

1. Ship rt4.input compiling; background tick via AmiliousClient only (current target).
2. Optionally log InputManager.getMode() — still no Keyboard read.
3. Pass real Enter/Esc into beginFrame for mode testing only (still no chat gate).
4. Gate Keyboard -> chat with shouldSendKeyToChat / Enter consume.
5. Fix QC on empty Enter.
6. Wire ChatboxState visibility.
7. Point ChatController wrappers at InputManager (adapter) so old call sites keep working.
8. Add ActionMapper + one bind path.
9. Remove adapter when confident.

---

## Debug checklist

- [ ] Login: mode WORLD, chat visible All tab (once visibility tracked)
- [ ] Background tick: no behavior change vs before input package
- [ ] Enter: WORLD -> CHAT, no QC (after read + gate)
- [ ] Type + Enter: message sends, mode WORLD if auto
- [ ] Empty Enter in CHAT: QC only if allowed
- [ ] Esc: CHAT -> WORLD
- [ ] Collapse chat: mode WORLD, Enter does nothing to chat
- [ ] Expand chat: can open CHAT again
- [ ] WORLD: hotkeys work; keys do not type into chat

---

## Search terms when resuming

    ChatController
    KEY_ENTER
    QuickChatPhrase
    activePhrase
    InterfaceList.getComponent
    49217538
    pressedKeys
    shouldConsumeEnter
    mainInputLoop
    AmiliousClient.update
    InputManager.tick

---

## One-sentence summary

Hook order: background tick (now) -> optional key state into beginFrame ->
keyboard gate -> chat submit/QC -> chat visibility -> replace ChatController reads ->
devices/actions.