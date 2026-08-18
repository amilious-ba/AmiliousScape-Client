package rt4;

import org.openrs2.deob.annotation.OriginalArg;
import org.openrs2.deob.annotation.OriginalClass;
import org.openrs2.deob.annotation.OriginalMember;
import org.openrs2.deob.annotation.Pc;

import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

@OriginalClass("signlink!e")
public final class FullScreenManager {

	/** Prefer borderless (no display-mode switch). Good default on Ally. */
	public static boolean borderlessFullscreen = true;

	/** Set true when exclusive failed and we fell back to borderless. */
	public static boolean usedBorderlessFallback = false;

	@OriginalMember(owner = "signlink!e", name = "b", descriptor = "Ljava/awt/DisplayMode;")
	private DisplayMode previousDisplayMode;

	@OriginalMember(owner = "signlink!e", name = "a", descriptor = "Ljava/awt/GraphicsDevice;")
	private GraphicsDevice device;

	@OriginalMember(owner = "signlink!e", name = "<init>", descriptor = "()V")
	public FullScreenManager() throws Exception {
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		this.device = env.getDefaultScreenDevice();
		if (!this.device.isFullScreenSupported()) {
			GraphicsDevice[] devices = env.getScreenDevices();
			for (int i = 0; i < devices.length; i++) {
				GraphicsDevice d = devices[i];
				if (d != null && d.isFullScreenSupported()) {
					this.device = d;
					return;
				}
			}
			throw new Exception();
		}
	}

	@OriginalMember(owner = "signlink!e", name = "a", descriptor = "(Ljava/awt/Frame;B)V")
	private void setFullScreenWindow(@OriginalArg(0) Frame frame) {
		try {
			this.device.setFullScreenWindow(frame);
		} catch (Throwable ex) {
			System.err.println("Failed to set fullscreen window: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	@OriginalMember(owner = "signlink!e", name = "a", descriptor = "(IIIILjava/awt/Frame;I)V")
	public final void enter(
			@OriginalArg(1) int refreshRate,
			@OriginalArg(2) int bitDepth,
			@OriginalArg(3) int height,
			@OriginalArg(4) Frame frame,
			@OriginalArg(5) int width) {

		usedBorderlessFallback = false;

		this.previousDisplayMode = this.device.getDisplayMode();
		if (this.previousDisplayMode == null) {
			throw new NullPointerException();
		}

		frame.setUndecorated(true);
		frame.enableInputMethods(false);

		// Config / ::borderless — skip exclusive entirely
		if (borderlessFullscreen) {
			System.out.println("[FS] borderlessFullscreen=true → borderless "
					+ width + "x" + height + " (using monitor bounds)");
			enterBorderless(frame);
			return;
		}

		// Exclusive attempt
		this.setFullScreenWindow(frame);

		DisplayMode current = this.device.getDisplayMode();
		DisplayMode target = findMode(width, height, bitDepth, current);

		if (target == null) {
			System.err.println("[FS] No real mode for " + width + "x" + height
					+ " — falling back to borderless");
			fallbackBorderless(frame);
			return;
		}

		if (sameMode(current, target)) {
			System.out.println("[FS] Exclusive: already at "
					+ current.getWidth() + "x" + current.getHeight());
			return;
		}

		try {
			this.device.setDisplayMode(target);
			System.out.println("[FS] Exclusive OK: "
					+ target.getWidth() + "x" + target.getHeight()
					+ " @" + target.getRefreshRate() + "Hz depth=" + target.getBitDepth());
		} catch (Exception e) {
			System.err.println("[FS] setDisplayMode failed: " + e
					+ " — falling back to borderless");
			fallbackBorderless(frame);
		}
	}

	private void fallbackBorderless(Frame frame) {
		usedBorderlessFallback = true;
		try {
			this.device.setFullScreenWindow(null);
		} catch (Exception ignored) {
		}
		enterBorderless(frame);
		System.out.println("[FS] Borderless fallback active (monitor bounds)");
	}

	private void enterBorderless(Frame frame) {
		Rectangle bounds = this.device.getDefaultConfiguration().getBounds();
		frame.setBounds(bounds);
		frame.setAlwaysOnTop(true);
		frame.setVisible(true);
		frame.toFront();
	}

	private static boolean sameMode(DisplayMode a, DisplayMode b) {
		return a.getWidth() == b.getWidth()
				&& a.getHeight() == b.getHeight()
				&& a.getBitDepth() == b.getBitDepth()
				&& a.getRefreshRate() == b.getRefreshRate();
	}

	private DisplayMode findMode(int width, int height, int bitDepth, DisplayMode current) {
		DisplayMode target = null;
		DisplayMode[] modes = this.device.getDisplayModes();
		for (int i = 0; i < modes.length; i++) {
			DisplayMode m = modes[i];
			if (m.getWidth() != width || m.getHeight() != height) {
				continue;
			}
			if (bitDepth > 0 && m.getBitDepth() > 0 && m.getBitDepth() != bitDepth) {
				continue;
			}
			if (target == null) {
				target = m;
			} else {
				int curR = current.getRefreshRate();
				if (Math.abs(m.getRefreshRate() - curR) < Math.abs(target.getRefreshRate() - curR)) {
					target = m;
				}
			}
		}
		return target;
	}

	@OriginalMember(owner = "signlink!e", name = "a", descriptor = "(Z)[I")
	public final int[] getDisplayModes() {
		DisplayMode[] displayModes = this.device.getDisplayModes();
		int[] result = new int[displayModes.length << 2];
		for (int i = 0; i < displayModes.length; i++) {
			result[i << 2] = displayModes[i].getWidth();
			result[(i << 2) + 1] = displayModes[i].getHeight();
			result[(i << 2) + 2] = displayModes[i].getBitDepth();
			result[(i << 2) + 3] = displayModes[i].getRefreshRate();
		}
		return result;
	}

	@OriginalMember(owner = "signlink!e", name = "a", descriptor = "(I)V")
	public final void exit() {
		try {
			this.device.setFullScreenWindow(null);
		} catch (Exception ignored) {
		}
		if (this.previousDisplayMode != null) {
			try {
				this.device.setDisplayMode(this.previousDisplayMode);
			} catch (Exception ignored) {
			}
			this.previousDisplayMode = null;
		}
		usedBorderlessFallback = false;
	}

	public final int[] getNativeDisplayMode() {
		DisplayMode currentMode = this.device.getDisplayMode();
		return new int[] {
				currentMode.getWidth(),
				currentMode.getHeight(),
				currentMode.getBitDepth(),
				currentMode.getRefreshRate()
		};
	}
}