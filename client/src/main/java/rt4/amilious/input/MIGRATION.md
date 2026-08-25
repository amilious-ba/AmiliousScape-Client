# Input System Migration Status

**Last Updated:** 2026-08-24

## Summary

Migrating from direct `Keyboard.pressedKeys[]` and `Mouse.*` access to Action-based InputManager system.

**Architecture:** Device → InputFrame → ActionMapper → Actions

**Status:** Partial migration (7 files migrated, ~8 files remaining for keyboard, ~7 for mouse)

---

## Critical Fixes Applied

### 1. BotInputDevice Overwriting Issue (FIXED)
**Problem:** `BotInputDevice.poll()` used `System.arraycopy()` which **overwrote** all keyboard/mouse input with zeros.

**Fix:** Changed to OR-merge pattern:
```java
// BEFORE (broken):
System.arraycopy(simulatedButtons, 0, out.buttonDown, 0, InputButtons.BUTTON_COUNT);

// AFTER (fixed):
for (int i = 0; i < InputButtons.BUTTON_COUNT && i < simulatedButtons.length; i++) {
    if (simulatedButtons[i]) {
        out.buttonDown[i] = true;  // OR, don't overwrite
    }
}
```

**Location:** `rt4/amilious/input/device/BotInputDevice.java:36-44`

### 2. Frame Timing for Event-Driven Input (FIXED)
**Problem:** `currentFrame.clear()` was called before polling, so event-driven code (clicks) saw empty frames.

**Fix:** Delay clear until polling:
```java
// Copy previous frame
previousFrame.copyDownFrom(currentFrame);

// Don't clear yet - let events read from last frame!
if (pollDevices) {
    currentFrame.clear();  // Clear JUST before polling
    // poll devices...
}
```

**Location:** `rt4/amilious/input/InputManager.java:106-128`

---

## Migration Progress

### ✅ Completed Files (7)

| File | Lines Migrated | Actions Created | Notes |
|------|----------------|-----------------|-------|
| **Protocol.java** | 4 locations | CAMERA_UP/DOWN/LEFT/RIGHT, MODIFIER_CTRL/SHIFT | Camera movement, teleport cheats |
| **MiniMenu.java** | 4 locations | MENU_ALTERNATIVE_ACTION, cheat helpers | Shift-click, teleport |
| **InterfaceList.java** | 2 locations | MODIFIER_CTRL/ALT/SHIFT, HOTKEY_1-0 | Interface hotkeys, world map teleport |
| **LoginManager.java** | 1 location | MENU_ALTERNATIVE_ACTION | Cursor selection |
| **InputManager.java** | Helper methods | N/A | `isCheatTeleportModifierDown()`, `isMenuAlternativeActionDown()`, `isAnyCameraKeyDown()`, `isRawKeyPressed()` |
| **ActionMapper.java** | Default bindings | All gameplay actions | Keyboard bindings installed |
| **Action.java** | Full documentation | All actions | JavaDoc added per user request |

### 🔄 Remaining Files - Keyboard (~8 files, ~40 sites)

| File | Est. Sites | Priority | Notes |
|------|------------|----------|-------|
| **GameShell.java** | ~5 | Medium | AWT keyboard handling, fullscreen toggles |
| **ScriptRunner.java** | ~10 | Medium | CS2 script keyboard access |
| **ChatHeadReader.java** | ~3 | Low | Voice/accessibility |
| **ClientProt.java** | ~8 | High | Network protocol keyboard checks |
| **MapController.java** | ~5 | Medium | World map keyboard shortcuts |
| **plugin/api/API.java** | ~9 | Low | Plugin API keyboard access |
| **Other files** | TBD | TBD | Find via grep |

**Search command:**
```bash
grep -r "Keyboard\.pressedKeys\[" --include="*.java" client/src/
```

### 🔄 Remaining Files - Mouse (~7 files, ~90 sites)

| File | Est. Sites | Priority | Notes |
|------|------------|----------|-------|
| **Protocol.java** | ~30 | High | Mouse position, clicks, drag |
| **LoginManager.java** | ~10 | High | Login screen mouse |
| **InterfaceList.java** | ~20 | High | Interface mouse interaction |
| **MiniMenu.java** | ~15 | High | Menu mouse hover/click |
| **ScriptRunner.java** | ~5 | Medium | CS2 mouse access |
| **PathFinder.java** | ~5 | Low | Click-to-walk |
| **MouseDevice.java** | N/A | N/A | Already uses InputFrame |

**Search command:**
```bash
grep -rE "Mouse\.(x|y|clickButton|clickX|clickY|clickTime)" --include="*.java" client/src/
```

---

## Actions Created

| Action | Default Binding | Mode Filter | Purpose |
|--------|----------------|-------------|---------|
| `OPEN_CHAT` | Enter | WORLD | Open chat input |
| `SUBMIT_CHAT` | Enter | CHAT | Submit chat message |
| `CLOSE_CHAT` | Escape | CHAT | Close chat |
| `ESCAPE` | Escape | Any | General escape |
| `CAMERA_UP` | ↑ or W | WORLD | Move camera up |
| `CAMERA_DOWN` | ↓ or S | WORLD | Move camera down |
| `CAMERA_LEFT` | ← or A | WORLD | Move camera left |
| `CAMERA_RIGHT` | → or D | WORLD | Move camera right |
| `MODIFIER_CTRL` | Ctrl | Any | Ctrl modifier |
| `MODIFIER_ALT` | Alt | Any | Alt modifier |
| `MODIFIER_SHIFT` | Shift | Any | Shift modifier |
| `MENU_ALTERNATIVE_ACTION` | Shift | WORLD | Shift-click alternative |
| `HOTKEY_1` through `HOTKEY_0` | 1-0 | WORLD | Interface hotkeys |

---

## Helper Methods (InputManager)

```java
// Check if Ctrl+Shift held (for staff teleport)
public static boolean isCheatTeleportModifierDown()

// Check if Shift held (for alternative menu actions)
public static boolean isMenuAlternativeActionDown()

// Check if any camera key is down
public static boolean isAnyCameraKeyDown()

// Check raw keyboard button by code (for data-driven hotkeys)
public static boolean isRawKeyPressed(int keyCode)

// Action state queries
public static boolean isActionDown(Action a)
public static boolean isActionPressed(Action a)  // Edge: just pressed
public static boolean isActionReleased(Action a)  // Edge: just released
```

---

## Migration Pattern

### Before (Direct Access):
```java
if (Keyboard.pressedKeys[Keyboard.KEY_CTRL] && Keyboard.pressedKeys[Keyboard.KEY_SHIFT]) {
    // Do something
}
```

### After (Action-based):
```java
if (InputManager.isCheatTeleportModifierDown()) {
    // Do something
}

// Or for simple checks:
if (InputManager.isActionDown(Action.MODIFIER_CTRL)) {
    // Do something
}
```

### For Data-Driven Keys (e.g., interface hotkeys):
```java
// BEFORE:
if (Keyboard.pressedKeys[component.aByteArray8[i]]) {
    // ...
}

// AFTER:
if (InputManager.isRawKeyPressed(component.aByteArray8[i])) {
    // ...
}
```

---

## Known Issues

### ✅ FIXED: BotInputDevice overwrites keyboard input
- **Status:** Fixed in `BotInputDevice.java:36-54`
- **Impact:** Keyboard input now works correctly

### ✅ FIXED: Frame timing causes empty frames during click events
- **Status:** Fixed in `InputManager.tick()` lines 106-128
- **Impact:** Actions work correctly during click handlers

---

## Testing Checklist

- [x] Camera movement (arrow keys, WASD)
- [x] Ctrl+Shift teleport (world click)
- [x] Ctrl+Shift teleport (world map click)
- [x] Ctrl+Shift+wheel (plane change)
- [x] Shift-click alternative menu actions
- [ ] Interface hotkeys (1-0, F-keys)
- [ ] Mouse position/clicks migration
- [ ] Gamepad input (already working)
- [ ] Bot simulation (needs testing after fix)

---

## Next Steps

1. **Test current fixes** - Verify Ctrl+Shift teleport and shift-click work
2. **Migrate remaining keyboard files** - Start with ClientProt.java (high priority)
3. **Migrate mouse input** - Start with Protocol.java mouse position/clicks
4. **Create mouse Actions** - MOUSE_BUTTON_1/2/3, MOUSE_WHEEL_UP/DOWN
5. **Remove debug logs** - Clean up after testing
6. **Update documentation** - Final polish

---

## Architecture Notes

### Frame System
- **InputFrame** has 3 arrays: `buttonDown[]`, `buttonPressed[]`, `buttonReleased[]`
- Edge detection via `computeEdges(previousFrame)` compares current vs previous
- Multiple devices poll into same frame (OR-merge, not overwrite!)

### Device Poll Order
1. KeyboardDevice
2. MouseDevice
3. GamepadDevice
4. BotInputDevice ← Must OR-merge, not overwrite!

### Action Binding
- Bindings are OR'd together (multiple keys can trigger same action)
- Mode filtering prevents actions in wrong modes (e.g., CAMERA_UP only in WORLD)
- Edge types: `isDown()`, `isPressed()`, `isReleased()`

---

## File Locations

**Core Files:**
- `rt4/amilious/input/InputManager.java` - Main input manager
- `rt4/amilious/input/action/Action.java` - Action enum
- `rt4/amilious/input/action/ActionMapper.java` - Bindings
- `rt4/amilious/input/state/InputFrame.java` - Frame state
- `rt4/amilious/input/state/InputButtons.java` - Button constants
- `rt4/amilious/input/device/BotInputDevice.java` - Bot simulation

**Migrated Files:**
- `rt4/Protocol.java` - Camera + teleport
- `rt4/MiniMenu.java` - Menu actions + teleport
- `rt4/InterfaceList.java` - Hotkeys + modifiers
- `rt4/LoginManager.java` - Cursor selection
