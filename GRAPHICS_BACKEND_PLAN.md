# AmiliousScape Client — Dual Graphics Backend Plan (JOGL + LWJGL)

**Repo:** https://github.com/amilious-ba/AmiliousScape-Client
**Branch for this work:** `graphics/lwjgl-backend` (or equivalent)
**Default/production path:** JOGL (must keep working at every step)
**Experimental path:** LWJGL 3 (Windows first, Linux later)
**License:** AGPL-3.0 — keep open source

This plan is written so another AI (Claude, Cursor, IDE agent, etc.) can execute it without the full prior chat history.

---

## 1. Goals

1. Keep a shippable JOGL HD client at all times.
2. Add LWJGL as a second backend selectable by config / JVM flag.
3. Introduce a draw-call facade so game code does not depend on JOGL types.
4. Grow LWJGL until it can run HD without crashing, then toward visual parity.
5. Do not rewrite networking, UI logic, plugins, commands, or amilious features as part of this work.

### Non-goals (for this conversion)

- Core-profile OpenGL / shaders / Vulkan
- Replacing AWT windowing with GLFW (optional later phase)
- Dropping JOGL in the first merge
- Platform-perfect Linux LWJGL on day one

---

## 2. Current state (as of this plan)

### Already done

| Item | Status |
|------|--------|
| `rt4.gl.GlBackend` interface | Exists / should exist |
| `rt4.gl.JoglBackend` | Owns JOGL context/drawable/window; init/quit/swap |
| `GlRenderer.init/quit/swapBuffers` | Delegates to backend |
| Config idea `graphicsBackend` / `-Damilious.gl=` | Partially wired or ready to wire |
| `rt4.gl.LwjglBackend` Windows stub | JAWT + GDI32 + WGL; context create works |
| LWJGL Gradle deps | Added (lwjgl, opengl, glfw, jawt, Windows natives) |

### Known gap

- After LWJGL init OK, client still runs HD paths that use `GlRenderer.gl` (JOGL `GL2`).
- LWJGL does not set `GlRenderer.gl` → NPE (e.g. in Shadow).
- Stub must not claim full HD ready (`enabled = true` without a working gl facade).

### Critical constraint

**JOGL path must remain default and playable after every commit.**

---

## 3. Architecture target

```
Game code (models, UI, shadows, materials, …)
        │
        ▼
   GlApi  (draw calls: enable, bindTexture, matrix, …)
        │
        ├── JoglGlApi   → JOGL GL2
        └── LwjglGlApi  → LWJGL GL11/GL13/… (compat profile)

Context / swap / window
        │
        ▼
   GlBackend
        ├── JoglBackend   (production)
        └── LwjglBackend  (experimental)
```

- **GlBackend:** lifecycle only (init, quit, swapBuffers, isEnabled).
- **GlApi:** every `GlRenderer.gl.gl*` style call used by the client.
- **Selection:** `graphicsBackend` in config and/or `-Damilious.gl=jogl|lwjgl`.

---

## 4. Config & selection

### Config field (GlobalJsonConfig or equivalent)

```java
public String graphicsBackend = "jogl"; // "jogl" | "lwjgl"
```

### Optional config.json

```json
"graphicsBackend": "jogl"
```

### Selection order in GlRenderer.init

1. System property `-Damilious.gl=...` if set
2. Else `GlobalJsonConfig.instance.graphicsBackend`
3. Else `"jogl"`

### Fallback rule

If LWJGL init fails (nonzero code), log and fall back to JOGL unless explicitly testing "fail hard."

```
Default run  → JOGL HD (unchanged)
-Damilious.gl=lwjgl → try LWJGL; on failure → JOGL (recommended)
```

---

## 5. Phased plan

### Phase 0 — Stabilize branch (if not done)

- Work only on `graphics/lwjgl-backend`.
- Merge master into it regularly.
- Confirm CI / local build green on JOGL.
- Confirm: no `-Damilious.gl` → full HD playable.

**Exit criteria:** JOGL-only behavior identical to pre-branch.

---

### Phase 1 — Backend lifecycle (mostly done)

**Files:**

- `rt4/gl/GlBackend.java`
- `rt4/gl/JoglBackend.java`
- `rt4/gl/LwjglBackend.java`
- `rt4/GlRenderer.java` (delegate only)

**Rules:**

- `JoglBackend` owns JOGL GLContext / GLDrawable / JAWTWindow.
- `GlRenderer` keeps `public static GL2 gl` until Phase 2 facade exists.
- `LwjglBackend` may create context and clear once, but must not set `GlRenderer.enabled = true` for full game unless `GlApi` is wired.

**Temporary LWJGL init policy (until facade works):**

```
After successful context + test clear:
  either return error code and fall back to JOGL
  or leave enabled=false so HD paths are not entered with null gl
```

**Exit criteria:**

- JOGL playable.
- `-Damilious.gl=lwjgl` creates context without unexplained native crashes; either falls back cleanly or shows stub clear without NPE spam.

---

### Phase 2 — Inventory GL usage

**Task:** Find every use of JOGL draw APIs in the client.

**Search patterns:**

```
GlRenderer.gl.
gl.gl
GL2.
import com.jogamp.opengl
```

Produce a checklist of methods actually called, grouped roughly as:

- State: enable/disable, depth, blend, cull, fog, lighting
- Clear / viewport / scissor
- Matrix stack / ortho / projection helpers used by client
- Textures: bind, texImage, texParam, texEnv/combine
- Client arrays / draw arrays/elements
- Misc: line width, color, material, light setup

**Do not implement the entire OpenGL API** — only what this repo calls.

**Exit criteria:** Written inventory (file or comment list) used to design `GlApi`.

#### Phase 2 Inventory Results (Completed)

**Total**: ~85+ unique GL methods across 15 files (381 call sites)

**Key files with GL usage:**
- GlRenderer.java (111 calls)
- SpecularMaterialRenderer.java (92 calls)
- GlFont.java (51 calls)
- GlTexture.java (30 calls)
- FogManager.java, LightingManager.java, MaterialManager.java
- GlModel.java, GlTile.java, GlRaster.java, ParticleSystem.java
- WaterMaterialRenderer.java, UnderwaterMaterialRenderer.java

**Categories:**

1. **State Management** (20+ methods)
   - glEnable/glDisable: TEXTURE_2D, FOG, DEPTH_TEST, LIGHTING, LIGHT0/1, COLOR_MATERIAL, CULL_FACE, BLEND, ALPHA_TEST, SCISSOR_TEST
   - glEnableClientState/glDisableClientState: VERTEX_ARRAY, NORMAL_ARRAY, COLOR_ARRAY, TEXTURE_COORD_ARRAY

2. **Clear/Viewport/Scissor** (5 methods)
   - glClear, glClearColor, glClearDepth, glViewport, glScissor

3. **Matrix Operations** (11 methods)
   - glMatrixMode, glLoadIdentity, glLoadMatrixf
   - glTranslatef, glRotatef, glScalef
   - glPushMatrix, glPopMatrix, glOrtho

4. **Texture Operations** (25+ methods)
   - Binding: glGenTextures, glBindTexture, glDeleteTextures
   - Images: glTexImage2D, glTexImage3D, glTexSubImage2D, glTexStorage2D, glGenerateMipmap
   - Parameters: glTexParameteri (MIN_FILTER, MAG_FILTER, WRAP_S/T, GENERATE_MIPMAP)
   - Environment: glTexEnvi, glTexEnvfv (COMBINE modes, RGB/ALPHA ops)
   - Coords: glTexCoord2f, glMultiTexCoord2f
   - Multitexture: glActiveTexture, glClientActiveTexture

5. **Buffer Operations** (11 methods)
   - VBO: glGenBuffers, glDeleteBuffers, glBindBuffer, glBufferData, glBufferSubData
   - Arrays: glVertexPointer, glColorPointer, glNormalPointer, glTexCoordPointer

6. **Drawing** (13 methods)
   - Primitives: glBegin/glEnd, glVertex2f, glDrawElements, glDrawPixels
   - Display Lists: glGenLists, glNewList, glEndList, glCallList, glDeleteLists
   - Raster: glRasterPos2i, glPixelZoom, glCopyPixels

7. **Lighting/Material/Fog** (9 methods)
   - glLightfv, glLightf, glLightModelfv, glColorMaterial
   - glFogi, glFogf, glFogfv, glHint

8. **Color/Blend/Depth** (8 methods)
   - glColor3ub, glColor4ub, glColor4f
   - glBlendFunc, glAlphaFunc
   - glDepthFunc, glDepthMask

9. **Misc Rendering State** (7 methods)
   - glShadeModel, glCullFace, glPolygonMode, glLineWidth
   - glPushAttrib, glPopAttrib
   - glDrawBuffer, glReadBuffer

10. **Query/Extension** (5 methods)
    - glGetIntegerv, glGetFloatv, glGetString, isExtensionAvailable
    - setSwapInterval (backend-specific)

11. **Particles** (2 methods)
    - glPointParameterfv, glPointParameterf

**Critical extensions required:**
- GL_ARB_multitexture (required)
- GL_ARB_texture_env_combine (required)
- GL_ARB_vertex_buffer_object (optional, VBO path)
- GL_EXT_texture3D (optional, water effects)

**Rendering style:** Hybrid immediate-mode + display lists + VBOs (when available), fixed-function pipeline, OpenGL 1.2+ with ARB extensions.

---

### Phase 3 — GlApi + JOGL implementation (zero behavior change)

**New types:**

```
rt4.gl.GlApi          // interface
rt4.gl.JoglGlApi      // implements GlApi, holds GL2
```

**Strategy:**

- Define `GlApi` methods matching the inventory (names can mirror GL: `enable(int)`, `bindTexture(int,int)`, …).
- `JoglGlApi` delegates 1:1 to `GL2`.
- After JOGL backend init, set something like:
  `GlRenderer.api = new JoglGlApi(gl2);`
- Keep `GlRenderer.gl` temporarily as the same `GL2` or
  Replace call sites gradually to `GlRenderer.api` / static `Gl.*` helper.

**Preferred low-churn option:**

- Introduce static helper `rt4.gl.Gl` that forwards to `GlRenderer.api`.
- Migrate call sites file-by-file from `GlRenderer.gl.glFoo` → `Gl.foo`.
- Until migration done, JOGL path can still use `GL2` directly.

**Hard rule:** After this phase, with `backend=jogl`, game must look and play the same.

**Exit criteria:** JOGL runs through `JoglGlApi` for all migrated sites; no gameplay regression.

---

### Phase 4 — LwjglGlApi (compat profile)

**Requirements:**

- Context must be OpenGL compatibility (fixed-function: matrix stack, client arrays, texenv, etc.).
- Windows: keep JAWT + WGL path (already started).
- Implement `LwjglGlApi` methods using `GL11` / `GL13` / needed classes.

**Buffer differences:**

- JOGL often accepts Java arrays; LWJGL prefers direct `ByteBuffer`/`FloatBuffer`/`IntBuffer`.
- Add small helpers in `LwjglGlApi` or `GlBuffers` to wrap arrays when needed.

**Init sequence for LWJGL when ready for HD attempt:**

1. Lock JAWT surface
2. Get HWND/HDC
3. Choose/Set pixel format
4. wglCreateContext / wglMakeCurrent
5. GL.createCapabilities()
6. Construct `LwjglGlApi`, assign to `GlRenderer.api`
7. Run same post-init setup JOGL runs (checkContext equivalent, material/lighting init) via `GlApi`, not JOGL types
8. Unlock surface (and define lock policy for each frame later)

**Do not call JOGL-only `afterContextCreated` that assumes `GL2`.**

**Exit criteria:** With `-Damilious.gl=lwjgl`, client reaches login or world without NPE on missing gl. Visual bugs OK.

---

### Phase 5 — Per-frame correctness (Windows)

- JAWT lock around make-current / draw / swap each frame (or validated safe pattern).
- Resize / canvas replace / fullscreen / borderless focus behavior.
- MSAA if JOGL path supports samples — match or document difference.
- Compare screenshots: lobby, world, bank, shadows, UI.

**Exit criteria:** Playable Windows LWJGL session with known, listed visual gaps only.

---

### Phase 6 — Linux LWJGL (optional follow-on)

Current Windows stub uses GDI32 / WGL — not portable.

**Options:**

| Option | Pros | Cons |
|--------|------|------|
| A. JAWT + GLX Linux path | Keeps AWT canvas | Second platform backend |
| B. GLFW window (bigger change) | One path Win/Linux/macOS | Input/window rewrite |

**Recommendation:** Finish Windows parity before Linux LWJGL. JOGL remains Linux production path.

Gradle: add `natives-linux` (and arm if needed) when starting Linux work.

---

### Phase 7 — Default switch / cleanup (only when ready)

- Default stays `jogl` until LWJGL checklist passes.
- Optional: ship dual-backend in release with config switch.
- Remove dead JOGL-only code only after confidence is high.
- Document in README: backends, flags, known gaps.

---

## 6. File map

| Path | Role |
|------|------|
| `rt4/gl/GlBackend.java` | Context lifecycle interface |
| `rt4/gl/JoglBackend.java` | JOGL init/quit/swap |
| `rt4/gl/LwjglBackend.java` | LWJGL init/quit/swap |
| `rt4/gl/GlApi.java` | Draw API interface |
| `rt4/gl/JoglGlApi.java` | JOGL implementation of GlApi |
| `rt4/gl/LwjglGlApi.java` | LWJGL implementation of GlApi |
| `rt4/gl/Gl.java` (optional) | Static forwarder to active GlApi |
| `rt4/GlRenderer.java` | Holds backend + api; high-level helpers |
| `GlobalJsonConfig` | `graphicsBackend` |
| `client/build.gradle` | JOGL + LWJGL deps/natives |

**Do not put draw logic in amilious cheats/UI packages.**

---

## 7. Gradle notes

Keep both JOGL (existing lib: jars) and LWJGL Maven deps.

```gradle
def lwjglVersion = "3.3.4" // or current stable

implementation platform("org.lwjgl:lwjgl-bom:${lwjglVersion}")
implementation "org.lwjgl:lwjgl"
implementation "org.lwjgl:lwjgl-opengl"
implementation "org.lwjgl:lwjgl-jawt"
// glfw optional until window migration

// IDE + runtime must see natives (use implementation, not only runtimeOnly)
implementation "org.lwjgl:lwjgl::natives-windows"
implementation "org.lwjgl:lwjgl-opengl::natives-windows"
```

- Ensure `mavenCentral()` is available for LWJGL.
- Fat jar must include LWJGL classes + natives jars if LWJGL is shipped.
- JOGL natives remain as today.

### Import gotchas (Windows LWJGL)

| Need | Correct package |
|------|----------------|
| wglCreateContext etc. | `org.lwjgl.opengl.WGL` |
| ChoosePixelFormat / SwapBuffers / PFD_* | `org.lwjgl.system.windows.GDI32` |
| JAWT | `org.lwjgl.system.jawt.*` + lwjgl-jawt module |
| Native null for handles | `MemoryUtil.NULL` (not Java null) |

**Avoid JogAmp classes (`jogamp.*`) in LWJGL backend.**

---

## 8. LWJGL Windows init rules (learned)

1. Hold JAWT lock through: get HDC → ChoosePixelFormat → SetPixelFormat → create context → make current → first clear/swap.
2. Unlocking before ChoosePixelFormat causes failure (bad HDC).
3. After context current: `GL.createCapabilities()`.
4. Per-frame lock policy is a later task; required once continuous rendering uses LWJGL.
5. Missing natives → `UnsatisfiedLinkError: lwjgl.dll` → put natives on classpath.

---

## 9. What does / does not translate to an interface

### Translates 1:1 (compat profile)

enable/disable, clear, viewport, matrix stack, textures, client arrays, draw arrays/elements, fog/lights/blend/depth fixed-function state.

### Needs adapter logic

- Java arrays → direct NIO buffers for LWJGL
- JOGL overload variety → only implement used signatures
- Extension capability queries

### Does not belong on GlApi

Context/window/swap → `GlBackend` only.

### Breaks if core profile is used

Fixed-function matrix stack, client arrays, begin/end style, classic lights/fog — require compatibility profile.

---

## 10. Testing checklist

### Every phase

- ☐ Cold start, default config → JOGL HD works
- ☐ Login → world → bank/dialog → resize
- ☐ Borderless fullscreen focus behavior (existing amilious fix)

### LWJGL-specific

- ☐ `-Damilious.gl=lwjgl` starts without native link error
- ☐ No NPE on `GlRenderer.gl` (facade assigned or HD not claimed)
- ☐ Fallback to JOGL if init fails (if implemented)
- ☐ Side-by-side screenshots when parity work begins

### Regression

- ☐ Plugins load
- ☐ Client commands / binds
- ☐ Cache isolation / amilious features untouched

---

## 11. Commit strategy

Small commits, always JOGL-green:

1. Extract JoglBackend behind GlBackend
2. Add graphicsBackend config + LWJGL deps
3. Add LwjglBackend Windows context stub
4. Inventory GL call sites
5. Add GlApi + JoglGlApi; migrate call sites batch N
6. Add LwjglGlApi; wire after context
7. Per-frame lock + resize/fullscreen fixes
8. Docs / README backend section

**Do not mix feature work (binds, UI) into graphics commits on this branch.**

---

## 12. Agent instructions (for Claude / IDE AI)

When implementing:

1. Read existing `GlRenderer`, `JoglBackend`, `LwjglBackend`, `DisplayMode`, shadow/material managers before editing.
2. **Never break default JOGL path to progress LWJGL.**
3. Prefer facade + dual backend over deleting JOGL.
4. Prefer compatibility GL on LWJGL.
5. Touch only graphics init and GL call sites unless a compile error forces a related fix.
6. After each meaningful change: build, run JOGL default, report what was verified.
7. If unsure about a GL call mapping, implement JOGL side first, then LWJGL, and note gaps.
8. Do not introduce GLFW as required for the first playable LWJGL HD.
9. Do not set `GlRenderer.enabled = true` on LWJGL until `GlApi` is assigned and post-init setup runs without JOGL types.
10. Keep amilious package features independent of backend choice.

---

## 13. Suggested immediate next steps for the agent

1. Confirm branch and that JOGL still runs.
2. Ensure `GlRenderer.init` selects backend + falls back to JOGL on LWJGL failure.
3. Soften LWJGL stub so it cannot NPE the client (return failure after probe or wire minimal `GlApi`).
4. Grep and list all `GlRenderer.gl` / `gl.gl` usages.
5. Create `GlApi` + `JoglGlApi` for the top 20 call patterns; migrate those sites.
6. Only then expand `LwjglGlApi` and attempt HD under `-Damilious.gl=lwjgl`.

---

## 14. Success definition

### Minimum success (merge-worthy dual backend):

- Config/flag selects JOGL or LWJGL.
- JOGL is default and fully playable.
- LWJGL creates a context on Windows and either runs HD without crash or fails soft to JOGL.
- Architecture supports growing LWJGL without rewriting game systems.

### Full success (later):

- LWJGL HD playable on Windows with acceptable parity.
- Documented Linux plan (JOGL production until GLX/GLFW done).
- Optional future: GLFW migration as a separate project phase.

---

## 15. One-paragraph summary for the agent

Convert AmiliousScape's renderer to a dual backend: keep JOGL as production via `JoglBackend`, add experimental `LwjglBackend`, and route draw calls through a new `GlApi` implemented by `JoglGlApi` and `LwjglGlApi`. Select backend with `graphicsBackend` / `-Damilious.gl`, default `jogl`, fallback to JOGL on LWJGL failure. Use OpenGL compatibility profile on LWJGL. Do not break JOGL at any step; inventory real gl call sites; migrate incrementally; Windows JAWT/WGL first; Linux LWJGL later. Context lifecycle stays in `GlBackend`; drawing stays in `GlApi`.
