package rt4;

import org.openrs2.deob.annotation.OriginalArg;
import org.openrs2.deob.annotation.OriginalClass;
import org.openrs2.deob.annotation.OriginalMember;
import org.openrs2.deob.annotation.Pc;
import plugin.PluginRepository;

import java.awt.*;

@OriginalClass("client!od")
public final class DisplayMode {

	// ==================== Static Fields ====================

	@OriginalMember(owner = "client!ib", name = "i", descriptor = "[Lclient!od;")
	public static DisplayMode[] cachedDisplayModes;

	@OriginalMember(owner = "client!rc", name = "M", descriptor = "Z")
	public static boolean glNativesLoaded = false;

	@OriginalMember(owner = "client!jk", name = "y", descriptor = "Z")
	public static boolean hdModeActive = false;

	@OriginalMember(owner = "client!hi", name = "f", descriptor = "J")
	public static long lastModeChangeTime = 0L;

	// ==================== Instance Fields ====================

	@OriginalMember(owner = "client!od", name = "j", descriptor = "I")
	public int width;

	@OriginalMember(owner = "client!od", name = "k", descriptor = "I")
	public int refreshRate;

	@OriginalMember(owner = "client!od", name = "l", descriptor = "I")
	public int height;

	@OriginalMember(owner = "client!od", name = "m", descriptor = "I")
	public int bitDepth;

	// ==================== Public Methods ====================

	/**
	 * Sets the window display mode (software, GL, HD, or fullscreen).
	 * This is the public entry point that determines if HD mode changed.
	 *
	 * @param forceReload whether to force canvas recreation
	 * @param newMode the desired mode (0=software, 1=GL, 2=HD, 3=fullscreen)
	 * @param width fullscreen width (or -1 for default)
	 * @param height fullscreen height (or -1 for default)
	 */
	@OriginalMember(owner = "client!th", name = "a", descriptor = "(ZIIII)V")
	public static void setWindowMode(@OriginalArg(0) boolean forceReload, @OriginalArg(1) int newMode, @OriginalArg(3) int width, @OriginalArg(4) int height) {
		lastModeChangeTime = 0L;
		@Pc(4) int currentMode = getWindowMode();

		// Fullscreen transitions always require reload
		if (newMode == 3 || currentMode == 3) {
			forceReload = true;
		}

		// Check if we're switching between HD/non-HD modes
		@Pc(44) boolean hdModeChanged = currentMode > 0 != newMode > 0;
		if (forceReload && newMode > 0) {
			hdModeChanged = true;
		}

		setWindowMode(forceReload, newMode, hdModeChanged, currentMode, width, height);
	}

	/**
	 * Gets the current window/display mode.
	 *
	 * @return 0=software, 1=GL, 2=HD, 3=fullscreen
	 */
	@OriginalMember(owner = "client!le", name = "a", descriptor = "(I)I")
	public static int getWindowMode() {
		// Fullscreen takes priority
		if (GameShell.fullScreenFrame != null
				|| GameShell.borderlessFullscreenActive
				|| GameShell.exclusiveFullscreenActive) {
			return 3;
		} else if (GlRenderer.enabled && hdModeActive) {
			return 2; // HD mode
		} else if (GlRenderer.enabled) {
			return 1; // GL mode
		} else {
			return 0; // Software mode
		}
	}

	/**
	 * Gets available display modes, filtering by minimum requirements.
	 * Caches the result for subsequent calls.
	 *
	 * @return array of supported display modes
	 */
	@OriginalMember(owner = "client!ab", name = "c", descriptor = "(B)[Lclient!od;")
	public static DisplayMode[] getDisplayModes() {
		if (cachedDisplayModes == null) {
			DisplayMode[] availableModes = fetchDisplayModes(GameShell.signLink);
			DisplayMode[] filteredModes = new DisplayMode[availableModes.length];
			int filteredCount = 0;

			// Get native screen size to filter out modes larger than the display
			int maxWidth = Integer.MAX_VALUE;
			int maxHeight = Integer.MAX_VALUE;
			try {
				java.awt.DisplayMode nativeMode = GraphicsEnvironment
						.getLocalGraphicsEnvironment()
						.getDefaultScreenDevice()
						.getDisplayMode();
				maxWidth = nativeMode.getWidth();
				maxHeight = nativeMode.getHeight();
			} catch (Exception ignored) {
			}

			// Filter modes: keep only those that meet minimum requirements
			label52:
			for (int i = 0; i < availableModes.length; i++) {
				DisplayMode mode = availableModes[i];

				// Skip modes with insufficient color depth
				if (mode.bitDepth > 0 && mode.bitDepth < 24) {
					continue;
				}

				// Skip modes below minimum resolution
				if (mode.width < 1024 || mode.height < 768) {
					continue;
				}

				// Skip modes larger than the native screen
				if (mode.width > maxWidth || mode.height > maxHeight) {
					continue;
				}

				// Check for duplicate resolutions, keep the one with highest bit depth
				for (int j = 0; j < filteredCount; j++) {
					DisplayMode existing = filteredModes[j];
					if (mode.width == existing.width && existing.height == mode.height) {
						if (mode.bitDepth > existing.bitDepth) {
							filteredModes[j] = mode;
						}
						continue label52;
					}
				}

				filteredModes[filteredCount] = mode;
				filteredCount++;
			}

			// Copy filtered modes to final array
			cachedDisplayModes = new DisplayMode[filteredCount];
			ArrayUtils.copy(filteredModes, 0, cachedDisplayModes, 0, filteredCount);

			// Sort by total pixel count (width * height)
			int[] sortKeys = new int[cachedDisplayModes.length];
			for (int i = 0; i < cachedDisplayModes.length; i++) {
				DisplayMode mode = cachedDisplayModes[i];
				sortKeys[i] = mode.height * mode.width;
			}
			ArrayUtils.sort(sortKeys, cachedDisplayModes);
		}
		return cachedDisplayModes;
	}

	/**
	 * Fetches raw display modes from the system via privileged SignLink request.
	 *
	 * @param signLink the SignLink instance for privileged operations
	 * @return array of available display modes
	 */
	@OriginalMember(owner = "client!pm", name = "a", descriptor = "(ILsignlink!ll;)[Lclient!od;")
	public static DisplayMode[] fetchDisplayModes(@OriginalArg(1) SignLink signLink) {
		if (!signLink.isFullScreenSupported()) {
			return new DisplayMode[0];
		}

		// Request display modes from system
		@Pc(17) PrivilegedRequest request = signLink.getDisplayModes();
		while (request.status == 0) {
			ThreadUtils.sleep(10L);
		}

		if (request.status == 2) {
			return new DisplayMode[0];
		}

		// Parse raw data: 4 ints per mode (width, height, bitDepth, refreshRate)
		@Pc(39) int[] rawData = (int[]) request.result;
		@Pc(45) DisplayMode[] modes = new DisplayMode[rawData.length >> 2];
		for (@Pc(47) int i = 0; i < modes.length; i++) {
			@Pc(59) DisplayMode mode = new DisplayMode();
			modes[i] = mode;
			mode.width = rawData[i << 2];
			mode.height = rawData[(i << 2) + 1];
			mode.bitDepth = rawData[(i << 2) + 2];
			mode.refreshRate = rawData[(i << 2) + 3];
		}
		return modes;
	}

	/**
	 * Creates a fullscreen frame using the legacy second-frame approach.
	 * This is a fallback when modern fullscreen methods fail.
	 *
	 * @param bitDepth desired bit depth (or 0 for auto)
	 * @param height desired height
	 * @param width desired width
	 * @param signLink SignLink for privileged operations
	 * @return the created Frame, or null if failed
	 */
	@OriginalMember(owner = "client!nf", name = "a", descriptor = "(IIIIILsignlink!ll;)Ljava/awt/Frame;")
	public static Frame createFullScreenFrame(@OriginalArg(2) int bitDepth, @OriginalArg(3) int height, @OriginalArg(4) int width, @OriginalArg(5) SignLink signLink) {
		if (!signLink.isFullScreenSupported()) {
			return null;
		}

		// Find a matching display mode
		@Pc(20) DisplayMode[] modes = fetchDisplayModes(signLink);
		if (modes == null) {
			return null;
		}

		// Search for best matching mode (highest bit depth for given resolution)
		@Pc(27) boolean foundMatch = false;
		for (@Pc(29) int i = 0; i < modes.length; i++) {
			if (width == modes[i].width && height == modes[i].height && (!foundMatch || modes[i].bitDepth > bitDepth)) {
				bitDepth = modes[i].bitDepth;
				foundMatch = true;
			}
		}

		if (!foundMatch) {
			return null;
		}

		// Create fullscreen frame via privileged request
		@Pc(90) PrivilegedRequest request = signLink.enterFullScreen(bitDepth, height, width);
		while (request.status == 0) {
			ThreadUtils.sleep(10L);
		}

		@Pc(103) Frame frame = (Frame) request.result;
		if (frame == null) {
			return null;
		} else if (request.status == 2) {
			// Failed - clean up and return null
			exitFullScreen(frame, signLink);
			return null;
		} else {
			return frame;
		}
	}

	/**
	 * Exits fullscreen mode for the given frame (legacy approach).
	 *
	 * @param frame the fullscreen frame to close
	 * @param signLink SignLink for privileged operations
	 */
	@OriginalMember(owner = "client!c", name = "a", descriptor = "(Ljava/awt/Frame;ZLsignlink!ll;)V")
	public static void exitFullScreen(@OriginalArg(0) Frame frame, @OriginalArg(2) SignLink signLink) {
		while (true) {
			@Pc(16) PrivilegedRequest request = signLink.exitFullScreen(frame);
			while (request.status == 0) {
				ThreadUtils.sleep(10L);
			}
			if (request.status == 1) {
				frame.setVisible(false);
				frame.dispose();
				return;
			}
			ThreadUtils.sleep(100L);
		}
	}

	// ==================== Private Methods ====================

	/**
	 * Main implementation of window mode switching.
	 * Handles fullscreen enter/exit, canvas recreation, and GL initialization.
	 *
	 * @param forceReload whether to recreate the canvas
	 * @param newMode target mode (0-3)
	 * @param hdModeChanged whether switching between HD/non-HD
	 * @param currentMode current mode (0-3)
	 * @param width fullscreen width (-1 for default)
	 * @param height fullscreen height (-1 for default)
	 */
	@OriginalMember(owner = "client!pm", name = "a", descriptor = "(ZIZIZII)V")
	private static void setWindowMode(boolean forceReload, int newMode, boolean hdModeChanged, int currentMode, int width, int height) {
		// Quit GL renderer if switching rendering modes
		if (hdModeChanged) {
			GlRenderer.quit();
		}

		// Handle fullscreen exit
		handleFullscreenExit(newMode, currentMode, width, height);

		// Handle fullscreen entry
		handleFullscreenEntry(newMode, currentMode, width, height);

		// If fullscreen entry failed completely, fall back to windowed mode
		if (newMode == 3 && !isInFullscreenMode()) {
			setWindowMode(true, Preferences.favoriteWorlds, true, currentMode, -1, -1);
			return;
		}

		// Canvas replacement required after fullscreen peer rebuild
		if (GameShell.replaceCanvas) {
			forceReload = true;
		}

		// Determine active container and calculate frame dimensions
		Container activeContainer = getActiveContainer();
		calculateFrameDimensions(activeContainer, currentMode);

		// Set canvas dimensions based on display mode
		setCanvasDimensions(newMode, currentMode);

		// Recreate canvas if needed, or just resize it
		if (forceReload) {
			recreateCanvas(newMode);
		} else {
			resizeCanvas(activeContainer);
		}

		// Handle GL context for mode transitions
		handleGLContextTransition(newMode, currentMode);

		// Handle HD mode initialization
		handleHDModeInitialization(hdModeChanged, newMode, currentMode);

		// Verify GL is enabled when requested
		if (!GlRenderer.enabled && newMode > 0) {
			setWindowMode(true, 0, true, currentMode, -1, -1);
			return;
		}

		// Configure rendering based on mode
		configureRenderingMode(newMode, currentMode);

		// Update scene and interface state
		updateSceneAndInterfaceState(hdModeChanged, newMode, currentMode);

		// Notify other systems of mode change
		notifyModeChange();
	}

	/**
	 * Handles exiting from fullscreen mode if needed.
	 */
	private static void handleFullscreenExit(int newMode, int currentMode, int width, int height) {
		boolean leavingFullscreen = isInFullscreenMode()
				&& (newMode != 3 || width != Preferences.fullScreenWidth || height != Preferences.fullScreenHeight);

		if (leavingFullscreen) {
			if (GameShell.borderlessFullscreenActive) {
				exitBorderlessFullscreen();
			} else if (GameShell.exclusiveFullscreenActive) {
				exitExclusiveFullscreen();
			} else if (GameShell.fullScreenFrame != null && GameShell.fullScreenFrame != GameShell.frame) {
				// Legacy second-frame exclusive fullscreen
				if (GameShell.frame != null) {
					GameShell.frame.setVisible(false);
				}
				exitFullScreen(GameShell.fullScreenFrame, GameShell.signLink);
				GameShell.fullScreenFrame = null;
			}
		}
	}

	/**
	 * Handles entering fullscreen mode if needed.
	 */
	private static void handleFullscreenEntry(int newMode, int currentMode, int width, int height) {
		if (newMode == 3 && !isInFullscreenMode()) {
			// Save windowed dimensions before entering fullscreen
			saveWindowedDimensions(currentMode);

			// Choose fullscreen method based on config
			boolean useBorderless = GlobalJsonConfig.instance == null
					|| GlobalJsonConfig.instance.borderlessFullscreen;

			if (useBorderless) {
				enterBorderlessFullscreen(width, height);
			} else {
				enterExclusiveFullscreen(width, height);

				// Fallback to legacy second-frame exclusive if modern method failed
				if (!GameShell.exclusiveFullscreenActive) {
					GameShell.fullScreenFrame = createFullScreenFrame(0, height, width, GameShell.signLink);
					if (GameShell.fullScreenFrame != null) {
						Preferences.fullScreenHeight = height;
						Preferences.fullScreenWidth = width;
						Preferences.write(GameShell.signLink);
					}
				}
			}
		}
	}

	/**
	 * Saves the current windowed frame dimensions before entering fullscreen.
	 */
	private static void saveWindowedDimensions(int currentMode) {
		if (GameShell.frame != null && currentMode != 3 && GameShell.windowedFrameWidth == 0) {
			java.awt.Dimension currentSize = GameShell.frame.getSize();
			java.awt.Insets insets = GameShell.frame.getInsets();
			java.awt.Point location = GameShell.frame.getLocation();

			// Store client area size (excluding window decorations)
			GameShell.windowedFrameWidth = currentSize.width - insets.left - insets.right;
			GameShell.windowedFrameHeight = currentSize.height - insets.top - insets.bottom;
			GameShell.windowedFrameX = location.x;
			GameShell.windowedFrameY = location.y;

			System.out.println("Saved windowed size: " + GameShell.windowedFrameWidth + "x" + GameShell.windowedFrameHeight
					+ " at " + GameShell.windowedFrameX + "," + GameShell.windowedFrameY);
		}
	}

	/**
	 * Gets the active container (fullscreen frame, main frame, or applet).
	 */
	private static Container getActiveContainer() {
		if (GameShell.fullScreenFrame != null) {
			return GameShell.fullScreenFrame;
		} else if (GameShell.frame == null) {
			return GameShell.signLink.applet;
		} else {
			return GameShell.frame;
		}
	}

	/**
	 * Calculates and sets the frame width/height based on container or saved dimensions.
	 */
	private static void calculateFrameDimensions(Container activeContainer, int currentMode) {
		Insets insets = null;

		// Check if we're restoring from saved windowed size
		boolean restoredFromSaved = !isInFullscreenMode()
				&& GameShell.windowedFrameWidth > 0
				&& currentMode == 3;

		if (restoredFromSaved) {
			// Use saved dimensions (already client area, don't subtract insets)
			GameShell.frameWidth = GameShell.windowedFrameWidth;
			GameShell.frameHeight = GameShell.windowedFrameHeight;
			System.out.println("Restoring windowed size: " + GameShell.frameWidth + "x" + GameShell.frameHeight);
		} else {
			// Get current container size
			GameShell.frameWidth = activeContainer.getSize().width;
			GameShell.frameHeight = activeContainer.getSize().height;

			// Subtract window decorations if using main frame
			if (GameShell.frame == activeContainer && !isInFullscreenMode()) {
				insets = GameShell.frame.getInsets();
				GameShell.frameWidth -= insets.right + insets.left;
				GameShell.frameHeight -= insets.bottom + insets.top;
			}
		}
	}

	/**
	 * Sets canvas dimensions and margins based on display mode.
	 * This also handles frame resizing and UI scale compensation.
	 *
	 * @param newMode the new display mode (0-3)
	 * @param currentMode the current/previous display mode (0-3)
	 */
	private static void setCanvasDimensions(int newMode, int currentMode) {
		// Configure frame resizability and size BEFORE setting canvas dimensions
		// (skip if fullscreen or no frame)
		if (GameShell.frame != null && !isInFullscreenMode()) {
			if (newMode >= 2) {
				// HD modes: allow resizing
				GameShell.frame.setResizable(true);

				// Only auto-resize when transitioning TO HD mode (2) from other modes
				// (0->2, 1->2, or 3->2, but not 2->2)
				boolean transitioningToHD = newMode == 2 && currentMode != 2;

				if (transitioningToHD) {
					// Get current frame size (client area)
					Dimension currentSize = GameShell.frame.getSize();
					Insets insets = GameShell.frame.getInsets();
					int currentClientWidth = currentSize.width - insets.left - insets.right;
					int currentClientHeight = currentSize.height - insets.top - insets.bottom;

					// Check if we need to resize (frame is still at fixed 765x503 size)
					// Allow some tolerance for insets variations
					boolean isFixedSize = Math.abs(currentClientWidth - 765) < 50
						&& Math.abs(currentClientHeight - 503) < 50;

					if (isFixedSize) {
						int targetWidth = 1024;
						int targetHeight = 768;

						// Detect UI scale and adjust target size
						double uiScale = getUIScale();
						if (uiScale > 1.0) {
							// Compensate for UI scaling so actual canvas is the target size
							targetWidth = (int) Math.round(targetWidth / uiScale);
							targetHeight = (int) Math.round(targetHeight / uiScale);
							System.out.println("UI scale detected: " + uiScale +
								", adjusting HD window to " + targetWidth + "x" + targetHeight);
						}

						// Capture position/size BEFORE resize
						java.awt.Point oldLoc = GameShell.frame.getLocation();
						java.awt.Dimension oldOuter = GameShell.frame.getSize();

						GameShell.frame.setSize(
							insets.left + targetWidth + insets.right,
							insets.top + targetHeight + insets.bottom
						);

						// Keep center, then clamp on-screen (top pinned if taller than display)
						repositionFrameAfterResize(
							GameShell.frame,
							oldOuter.width, oldOuter.height,
							oldLoc.x, oldLoc.y
						);
					}
				}
			} else {
				// SD/GL modes: fixed size, no resizing
				GameShell.frame.setResizable(false);

				// Capture position/size BEFORE resize
				java.awt.Point oldLoc = GameShell.frame.getLocation();
				java.awt.Dimension oldOuter = GameShell.frame.getSize();

				// Size frame to exactly fit 765x503 canvas plus window decorations
				Insets insets = GameShell.frame.getInsets();
				GameShell.frame.setSize(
					insets.left + 765 + insets.right,
					insets.top + 503 + insets.bottom
				);

				// Keep center, then clamp on-screen
				repositionFrameAfterResize(
					GameShell.frame,
					oldOuter.width, oldOuter.height,
					oldLoc.x, oldLoc.y
				);
			}
		}

		// Now set canvas dimensions based on mode
		if (newMode >= 2) {
			// HD/Fullscreen: use entire frame
			GameShell.canvasWidth = GameShell.frameWidth;
			GameShell.canvasHeight = GameShell.frameHeight;
			GameShell.leftMargin = 0;
			GameShell.topMargin = 0;
		} else {
			// Fixed size for software/GL modes
			GameShell.topMargin = 0;
			GameShell.leftMargin = (GameShell.frameWidth - 765) / 2;
			GameShell.canvasWidth = 765;
			GameShell.canvasHeight = 503;
		}
	}

	/**
	 * Gets the UI scale factor (e.g., from -Dsun.java2d.uiScale).
	 */
	private static double getUIScale() {
		if (GameShell.frame == null) {
			return 1.0;
		}
		try {
			GraphicsConfiguration gc = GameShell.frame.getGraphicsConfiguration();
			if (gc != null) {
				java.awt.geom.AffineTransform tx = gc.getDefaultTransform();
				double scaleX = tx.getScaleX();
				double scaleY = tx.getScaleY();
				// Use the larger scale if they differ
				return Math.max(scaleX, scaleY);
			}
		} catch (Exception ignored) {
		}
		return 1.0;
	}

	/**
	 * After setSize: keep the old center, then clamp so the window stays on the
	 * monitor. If the window is larger than the screen, pin top (and left) on-screen.
	 */
	private static void repositionFrameAfterResize(java.awt.Frame frame,
	                                                int oldOuterW, int oldOuterH,
	                                                int oldX, int oldY) {
		if (frame == null) {
			return;
		}

		java.awt.Dimension newSize = frame.getSize();
		int newW = newSize.width;
		int newH = newSize.height;

		// 1) Keep center point stable
		int centerX = oldX + oldOuterW / 2;
		int centerY = oldY + oldOuterH / 2;
		int newX = centerX - newW / 2;
		int newY = centerY - newH / 2;

		// 2) Clamp to the monitor that contains the old center (fallback: default screen)
		java.awt.Rectangle screen;
		try {
			java.awt.GraphicsConfiguration gc = frame.getGraphicsConfiguration();
			if (gc == null) {
				gc = java.awt.GraphicsEnvironment
						.getLocalGraphicsEnvironment()
						.getDefaultScreenDevice()
						.getDefaultConfiguration();
			}
			// Bounds in absolute coords; usable area prefers max window bounds (taskbar-safe)
			java.awt.GraphicsDevice device = gc.getDevice();
			screen = device.getDefaultConfiguration().getBounds();
			try {
				java.awt.Insets screenInsets = java.awt.Toolkit.getDefaultToolkit()
						.getScreenInsets(gc);
				screen = new java.awt.Rectangle(
						screen.x + screenInsets.left,
						screen.y + screenInsets.top,
						screen.width - screenInsets.left - screenInsets.right,
						screen.height - screenInsets.top - screenInsets.bottom
				);
			} catch (Exception ignored) {
			}
		} catch (Exception e) {
			frame.setLocation(newX, newY);
			return;
		}

		// Horizontal
		if (newW >= screen.width) {
			// Wider than screen → pin left edge on screen
			newX = screen.x;
		} else {
			if (newX < screen.x) {
				newX = screen.x;
			}
			if (newX + newW > screen.x + screen.width) {
				newX = screen.x + screen.width - newW;
			}
		}

		// Vertical — if taller than screen, keep TOP on screen (your rule)
		if (newH >= screen.height) {
			newY = screen.y;
		} else {
			if (newY < screen.y) {
				newY = screen.y;
			}
			if (newY + newH > screen.y + screen.height) {
				newY = screen.y + screen.height - newH;
			}
		}

		frame.setLocation(newX, newY);
	}

	/**
	 * Recreates the canvas and initializes GL if needed.
	 */
	private static void recreateCanvas(int newMode) {
		// Stop input handlers
		detachCanvas();

		// Create new canvas
		client.instance.addCanvas();

		// Restart input handlers
		Keyboard.start(GameShell.canvas);
		Mouse.start(GameShell.canvas);
		if (client.mouseWheel != null) {
			client.mouseWheel.start(GameShell.canvas);
		}

		// Wait for canvas to become displayable
		long deadline = System.currentTimeMillis() + 2000;
		while (GameShell.canvas != null
				&& !GameShell.canvas.isDisplayable()
				&& System.currentTimeMillis() < deadline) {
			try {
				GameShell.frame.validate();
				GameShell.canvas.setVisible(true);
				Thread.sleep(50);
			} catch (Exception ignored) {
			}
		}

		System.out.println("Canvas displayable=" + (GameShell.canvas != null && GameShell.canvas.isDisplayable())
				+ " size=" + GameShell.canvasWidth + "x" + GameShell.canvasHeight);

		// Initialize GL for GL/HD modes
		if (newMode > 0) {
			initializeGLRenderer();
		}
	}

	/**
	 * Resizes the existing canvas without recreating it.
	 */
	private static void resizeCanvas(Container activeContainer) {
		if (GlRenderer.enabled) {
			GlRenderer.setCanvasSize(GameShell.canvasWidth, GameShell.canvasHeight);
		}

		GameShell.canvas.setSize(GameShell.canvasWidth, GameShell.canvasHeight);

		// Position canvas with proper insets
		if (GameShell.frame == activeContainer && !isInFullscreenMode()) {
			Insets insets = GameShell.frame.getInsets();
			GameShell.canvas.setLocation(insets.left + GameShell.leftMargin, insets.top + GameShell.topMargin);
		} else {
			GameShell.canvas.setLocation(GameShell.leftMargin, GameShell.topMargin);
		}
	}

	/**
	 * Handles GL context transitions when switching modes.
	 */
	private static void handleGLContextTransition(int newMode, int currentMode) {
		// Switching from GL to software: destroy context
		if (newMode == 0 && currentMode > 0) {
			GlRenderer.createAndDestroyContext(GameShell.canvas);
		}
	}

	/**
	 * Handles HD mode initialization when switching rendering modes.
	 */
	private static void handleHDModeInitialization(boolean hdModeChanged, int newMode, int currentMode) {
		if (hdModeChanged && newMode > 0) {
			GameShell.canvas.setIgnoreRepaint(true);

			if (!glNativesLoaded) {
				// Show loading screen while initializing
				showLoadingScreen(currentMode);

				// Load GL natives
				loadGLNatives();
			}

			if (glNativesLoaded) {
				GlRenderer.init(GameShell.canvas, Preferences.antiAliasingMode * 2);
			}
		}
	}

	/**
	 * Shows a loading screen during HD initialization.
	 */
	private static void showLoadingScreen(int currentMode) {
		SceneGraph.clear();
		SoftwareRaster.frameBuffer = null;
		SoftwareRaster.frameBuffer = FrameBuffer.create(GameShell.canvasHeight, GameShell.canvasWidth, GameShell.canvas);
		SoftwareRaster.clear();

		// Render appropriate loading message
		if (client.gameState == 5) {
			LoadingBar.render(true, Fonts.b12Full);
		} else {
			Fonts.drawTextOnScreen(false, LocalizedText.LOADING);
		}

		try {
			Graphics graphics = GameShell.canvas.getGraphics();
			SoftwareRaster.frameBuffer.draw(graphics);
		} catch (Exception ignored) {
		}

		GameShell.method2704();

		// Restore appropriate framebuffer for current mode
		if (currentMode == 0) {
			SoftwareRaster.frameBuffer = FrameBuffer.create(503, 765, GameShell.canvas);
		} else {
			SoftwareRaster.frameBuffer = null;
		}
	}

	/**
	 * Configures rendering settings based on the selected mode.
	 */
	private static void configureRenderingMode(int newMode, int currentMode) {
		if (newMode > 0 && currentMode == 0) {
			// Switching to GL/HD mode
			GameShell.thread.setPriority(5);
			SoftwareRaster.frameBuffer = null;
			SoftwareModel.method4580();
			((Js5GlTextureProvider) Rasteriser.textureProvider).method3248(200);

			if (Preferences.highDetailLighting) {
				Rasteriser.setBrightness(0.7F);
			}
			LoginManager.method4637();

		} else if (newMode == 0 && currentMode > 0) {
			// Switching to software mode
			GameShell.thread.setPriority(1);
			SoftwareRaster.frameBuffer = FrameBuffer.create(503, 765, GameShell.canvas);
			SoftwareModel.method4583();
			ParticleSystem.quit();
			((Js5GlTextureProvider) Rasteriser.textureProvider).method3248(20);

			// Apply brightness based on preference
			if (Preferences.highDetailLighting) {
				if (Preferences.brightness == 1) {
					Rasteriser.setBrightness(0.9F);
				}
				if (Preferences.brightness == 2) {
					Rasteriser.setBrightness(0.8F);
				}
				if (Preferences.brightness == 3) {
					Rasteriser.setBrightness(0.7F);
				}
				if (Preferences.brightness == 4) {
					Rasteriser.setBrightness(0.6F);
				}
			}
			GlTile.method1939();
			LoginManager.method4637();
		}
	}

	/**
	 * Updates scene graph and interface state after mode change.
	 */
	private static void updateSceneAndInterfaceState(boolean hdModeChanged, int newMode, int currentMode) {
		SceneGraph.aBoolean130 = !SceneGraph.allLevelsAreVisible();

		if (hdModeChanged) {
			client.method2721();
		}

		hdModeActive = newMode >= 2;

		if (InterfaceList.topLevelInterface != -1) {
			InterfaceList.method3712(true);
		}

		// Re-run interface open scripts when entering/leaving fullscreen
		// (peer rebuild causes component state to reset)
		if (newMode == 3 || currentMode == 3) {
			if (InterfaceList.topLevelInterface != -1) {
				InterfaceList.method1626(InterfaceList.topLevelInterface);
			}

			if (InterfaceList.openInterfaces != null) {
				for (rt4.ComponentPointer p = (rt4.ComponentPointer) InterfaceList.openInterfaces.head();
					 p != null;
					 p = (rt4.ComponentPointer) InterfaceList.openInterfaces.next()) {
					InterfaceList.method1626(p.interfaceId);
				}
			}

			for (int i = 0; i < 100; i++) {
				InterfaceList.aBooleanArray100[i] = true;
			}
			GameShell.fullRedraw = true;
		}
	}

	/**
	 * Notifies other systems that the window mode has changed.
	 */
	private static void notifyModeChange() {
		// Custom patch for tutorial interface positioning
		rt4.amilious.TutorialPatch.onWindowModeChanged();

		// Send window details to server
		if (Protocol.socket != null && (client.gameState == 30 || client.gameState == 25)) {
			ClientProt.sendWindowDetails();
		}

		// Mark interfaces for update
		for (int i = 0; i < 100; i++) {
			InterfaceList.aBooleanArray100[i] = true;
		}

		GameShell.fullRedraw = true;
		GameShell.replaceCanvas = false;

		// Reload plugins for new window state
		PluginRepository.reloadPlugins();
	}

	// ==================== Helper Methods ====================

	/**
	 * Checks if currently in any fullscreen mode.
	 */
	private static boolean isInFullscreenMode() {
		return GameShell.fullScreenFrame != null
				|| GameShell.borderlessFullscreenActive
				|| GameShell.exclusiveFullscreenActive;
	}

	/**
	 * Detaches the canvas from input handlers and its parent container.
	 * This must be called before disposing the frame to prevent JOGL issues.
	 */
	private static void detachCanvas() {
		if (GameShell.canvas != null) {
			try {
				Keyboard.stop(GameShell.canvas);
				Mouse.stop(GameShell.canvas);
				if (client.mouseWheel != null) {
					client.mouseWheel.stop(GameShell.canvas);
				}
				if (GameShell.canvas.getParent() != null) {
					GameShell.canvas.getParent().remove(GameShell.canvas);
				}
			} catch (Exception ignored) {
			}
		}
	}

	/**
	 * Rebuilds the frame's peer by disposing and recreating it.
	 * Required on Windows to properly toggle window decorations.
	 */
	private static void rebuildFramePeer() {
		GameShell.frame.setVisible(false);
		GameShell.frame.dispose();
	}

	/**
	 * Gets the saved or default windowed dimensions.
	 */
	private static Dimension getWindowedDimensions() {
		int w = GameShell.windowedFrameWidth > 0 ? GameShell.windowedFrameWidth : 1024;
		int h = GameShell.windowedFrameHeight > 0 ? GameShell.windowedFrameHeight : 768;
		return new Dimension(w, h);
	}

	/**
	 * Restores the frame to decorated windowed mode with the specified dimensions.
	 * Rebuilds the frame peer, restores decorations, and adjusts size for insets.
	 *
	 * @param w client area width
	 * @param h client area height
	 */
	private static void restoreDecoratedWindow(int w, int h) {
		// Rebuild frame peer to restore decorations
		rebuildFramePeer();

		GameShell.frame.setUndecorated(false);

		// Set rough size first (insets become valid after setVisible)
		GameShell.frame.setSize(w + 16, h + 39);
		if (GameShell.windowedFrameX > 0 || GameShell.windowedFrameY > 0) {
			GameShell.frame.setLocation(GameShell.windowedFrameX, GameShell.windowedFrameY);
		} else {
			GameShell.frame.setLocationRelativeTo(null);
		}

		GameShell.frame.setVisible(true);
		GameShell.frame.validate();

		// Adjust for actual insets
		Insets insets = GameShell.frame.getInsets();
		GameShell.frame.setSize(
				insets.left + w + insets.right,
				insets.top + h + insets.bottom
		);
	}

	/**
	 * Initializes the GL renderer and loads natives if needed.
	 */
	private static void initializeGLRenderer() {
		try {
			GameShell.canvas.setIgnoreRepaint(true);

			// Load GL natives if not already loaded
			if (!glNativesLoaded) {
				loadGLNatives();
			}

			// Initialize GL renderer
			if (glNativesLoaded) {
				int result = GlRenderer.init(GameShell.canvas, Preferences.antiAliasingMode * 2);
				System.out.println("GlRenderer.init result=" + result + " enabled=" + GlRenderer.enabled);
			} else {
				System.out.println("Skipped GlRenderer.init – natives not loaded");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads OpenGL native libraries via privileged request.
	 */
	private static void loadGLNatives() {
		PrivilegedRequest request = GameShell.signLink.loadGlNatives(client.instance.getClass());
		while (request.status == 0) {
			ThreadUtils.sleep(100L);
		}
		if (request.status == 1) {
			glNativesLoaded = true;
		}
		System.out.println("loadGlNatives status=" + request.status);
	}

	/**
	 * Enters single-window borderless fullscreen using the existing GameShell.frame.
	 * This method uses the frame's graphics configuration to get screen bounds.
	 */
	private static void enterBorderlessFullscreen(int width, int height) {
		if (GameShell.frame == null) {
			return;
		}

		try {
			// Get screen bounds for borderless fullscreen
			GraphicsConfiguration gc = GameShell.frame.getGraphicsConfiguration();
			if (gc == null) {
				gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
						.getDefaultScreenDevice()
						.getDefaultConfiguration();
			}
			Rectangle bounds = gc.getBounds();

			// Always use scaled AWT bounds (not native DisplayMode dimensions)
			int w = bounds.width;
			int h = bounds.height;

			// Detach canvas before disposing frame (prevents JOGL issues)
			detachCanvas();

			// Rebuild frame peer to remove decorations
			rebuildFramePeer();

			// Configure as undecorated fullscreen
			GameShell.frame.setUndecorated(true);
			GameShell.frame.setBounds(bounds.x, bounds.y, w, h);
			GameShell.frame.setVisible(true);
			GameShell.frame.setAlwaysOnTop(true);  // Force above taskbar
			GameShell.frame.validate();
			GameShell.frame.toFront();
			GameShell.frame.requestFocus();

			// Update fullscreen state
			GameShell.borderlessFullscreenActive = true;
			GameShell.fullScreenFrame = GameShell.frame; // Marker for getWindowMode()
			GameShell.replaceCanvas = true;

			// Save preferences
			Preferences.fullScreenWidth = w;
			Preferences.fullScreenHeight = h;
			Preferences.write(GameShell.signLink);

			System.out.println("Entered borderless fullscreen: " + w + "x" + h);
		} catch (Exception e) {
			e.printStackTrace();
			GameShell.borderlessFullscreenActive = false;
		}
	}

	/**
	 * Restores the normal decorated window from borderless fullscreen.
	 */
	private static void exitBorderlessFullscreen() {
		if (GameShell.frame == null) {
			GameShell.borderlessFullscreenActive = false;
			return;
		}

		try {
			Dimension size = getWindowedDimensions();
			int w = size.width;
			int h = size.height;

			// Detach canvas before disposing
			detachCanvas();

			// Restore decorated window
			restoreDecoratedWindow(w, h);
			GameShell.frame.toFront();

			// Clear fullscreen state
			GameShell.borderlessFullscreenActive = false;
			GameShell.fullScreenFrame = null;
			GameShell.replaceCanvas = true;

			System.out.println("Exited borderless fullscreen, restored " + w + "x" + h);
		} catch (Exception e) {
			e.printStackTrace();
			GameShell.borderlessFullscreenActive = false;
		}
	}

	/**
	 * Enters exclusive fullscreen mode using Java's native fullscreen API.
	 * This changes the actual display mode of the monitor.
	 */
	private static void enterExclusiveFullscreen(int width, int height) {
		if (GameShell.frame == null) {
			return;
		}

		try {
			// Detach canvas first
			detachCanvas();

			GraphicsConfiguration gc = GameShell.frame.getGraphicsConfiguration();
			GraphicsDevice device = gc.getDevice();

			if (!device.isFullScreenSupported()) {
				System.out.println("[FS] Exclusive not supported on this device");
				return;
			}

			// Determine target display mode (native physical size)
			java.awt.DisplayMode current = device.getDisplayMode();
			int wantW = current.getWidth();
			int wantH = current.getHeight();

			// Try to match requested dimensions to a real display mode
			if (width >= 800 && height >= 600) {
				// Account for HiDPI scaling
				double sx = 1.0, sy = 1.0;
				try {
					java.awt.geom.AffineTransform tx = gc.getDefaultTransform();
					if (tx.getScaleX() > 0) sx = tx.getScaleX();
					if (tx.getScaleY() > 0) sy = tx.getScaleY();
				} catch (Exception ignored) {
				}

				int nativeFromArgsW = (int) Math.round(width * sx);
				int nativeFromArgsH = (int) Math.round(height * sy);

				// Check if args look like logical (scaled) size
				boolean argsLookLogical = (width < current.getWidth() - 50);
				int candidateW = argsLookLogical ? nativeFromArgsW : width;
				int candidateH = argsLookLogical ? nativeFromArgsH : height;

				// Find matching display mode
				for (java.awt.DisplayMode dm : device.getDisplayModes()) {
					if (dm.getWidth() == candidateW && dm.getHeight() == candidateH) {
						wantW = candidateW;
						wantH = candidateH;
						break;
					}
				}
			}

			// Find best display mode (highest bit depth and refresh rate)
			java.awt.DisplayMode best = null;
			for (java.awt.DisplayMode dm : device.getDisplayModes()) {
				if (dm.getWidth() == wantW && dm.getHeight() == wantH) {
					if (best == null
							|| dm.getBitDepth() > best.getBitDepth()
							|| (dm.getBitDepth() == best.getBitDepth()
							&& dm.getRefreshRate() > best.getRefreshRate())) {
						best = dm;
					}
				}
			}
			if (best == null) {
				best = current;
			}

			// Prepare frame for fullscreen
			rebuildFramePeer();

			try {
				GameShell.frame.setUndecorated(true);
			} catch (Exception e) {
				System.out.println("[FS] setUndecorated failed: " + e.getMessage());
			}

			// Enter fullscreen mode (MUST happen before setDisplayMode)
			device.setFullScreenWindow(GameShell.frame);

			// Set display mode only if supported (prevents iconification exceptions)
			if (device.isDisplayChangeSupported()) {
				try {
					if (best != null && !best.equals(device.getDisplayMode())) {
						device.setDisplayMode(best);
						System.out.println("[FS] Exclusive display mode: "
								+ best.getWidth() + "x" + best.getHeight()
								+ " @" + best.getRefreshRate() + "hz");
					} else {
						System.out.println("[FS] Exclusive using current mode: "
								+ device.getDisplayMode().getWidth() + "x"
								+ device.getDisplayMode().getHeight()
								+ " @" + device.getDisplayMode().getRefreshRate() + "hz");
					}
				} catch (Exception e) {
					// Gracefully fall back to current mode
					System.out.println("[FS] Could not set display mode, using current: " + e.getMessage());
				}
			} else {
				System.out.println("[FS] Display mode changes not supported, using current mode: "
						+ device.getDisplayMode().getWidth() + "x"
						+ device.getDisplayMode().getHeight()
						+ " @" + device.getDisplayMode().getRefreshRate() + "hz");
			}

			GameShell.frame.validate();

			// Update state
			GameShell.exclusiveFullscreenActive = true;
			GameShell.borderlessFullscreenActive = false;
			GameShell.fullScreenFrame = GameShell.frame;
			GameShell.replaceCanvas = true;

			// Save dimensions (use frame bounds as AWT may report scaled values)
			Rectangle bounds = GameShell.frame.getBounds();
			int storeW = bounds.width > 0 ? bounds.width : wantW;
			int storeH = bounds.height > 0 ? bounds.height : wantH;
			Preferences.fullScreenWidth = storeW;
			Preferences.fullScreenHeight = storeH;
			Preferences.write(GameShell.signLink);

			System.out.println("[FS] Entered exclusive fullscreen: " + storeW + "x" + storeH
					+ " (native target " + wantW + "x" + wantH + ")");
		} catch (Exception e) {
			e.printStackTrace();
			GameShell.exclusiveFullscreenActive = false;
			GameShell.fullScreenFrame = null;

			// Try to exit fullscreen on error
			try {
				GraphicsDevice device = GameShell.frame.getGraphicsConfiguration().getDevice();
				device.setFullScreenWindow(null);
			} catch (Exception ignored) {
			}
		}
	}

	/**
	 * Exits exclusive fullscreen mode and restores windowed state.
	 */
	private static void exitExclusiveFullscreen() {
		if (GameShell.frame == null) {
			GameShell.exclusiveFullscreenActive = false;
			GameShell.fullScreenFrame = null;
			return;
		}

		try {
			// Detach canvas
			detachCanvas();

			// Exit fullscreen mode first
			try {
				GraphicsDevice device = GameShell.frame.getGraphicsConfiguration().getDevice();
				device.setFullScreenWindow(null);
			} catch (Exception e) {
				System.out.println("[FS] setFullScreenWindow(null) failed: " + e);
			}

			Dimension size = getWindowedDimensions();
			int w = size.width;
			int h = size.height;

			// Restore decorated window
			restoreDecoratedWindow(w, h);

			// Restore position again (just to be sure after inset adjustment)
			if (GameShell.windowedFrameX > 0 || GameShell.windowedFrameY > 0) {
				GameShell.frame.setLocation(GameShell.windowedFrameX, GameShell.windowedFrameY);
			}

			// Force window to front (toFront alone often fails on Windows)
			forceToFront();

			// Clear fullscreen state
			GameShell.exclusiveFullscreenActive = false;
			GameShell.fullScreenFrame = null;
			GameShell.replaceCanvas = true;

			System.out.println("[FS] Exited exclusive fullscreen, restored " + w + "x" + h
					+ " at " + GameShell.windowedFrameX + "," + GameShell.windowedFrameY
					+ " undecorated=" + GameShell.frame.isUndecorated());
		} catch (Exception e) {
			e.printStackTrace();
			GameShell.exclusiveFullscreenActive = false;
			GameShell.fullScreenFrame = null;
		}
	}

	private static void forceToFront() {
		if (GameShell.frame == null) return;
		try {
			GameShell.frame.setAlwaysOnTop(true);
			GameShell.frame.toFront();
			GameShell.frame.requestFocus();
			if (GameShell.canvas != null)
				GameShell.canvas.requestFocus();
			GameShell.frame.setAlwaysOnTop(false);
		} catch (Exception ignored) {}
	}

}
