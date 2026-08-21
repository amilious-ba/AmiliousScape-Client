package rt4.core;

import com.google.gson.Gson;

import java.io.FileReader;

public class GlobalJsonConfig {
	public static GlobalJsonConfig instance = null;

	public static void load(String path) {
		Gson gson = new Gson();

		try {
			instance = gson.fromJson(new FileReader(path), GlobalJsonConfig.class);
		} catch (Exception ex) {
			System.err.println("No config.json file, using defaults");
		}
	}

	// ----

	public String ip_management = "amilious.xyz";//was private may need getter
	String ip_address = "amilious.xyz";
	int world = 1;
	public int server_port = 43594; //was private may need getter
	public int wl_port = 43595;//was private may need getter
	int js5_port = 43595;
	boolean mouseWheelZoom = GlobalConfig.MOUSEWHEEL_ZOOM;
	public String pluginsFolder = "plugins";
	public boolean startFullscreen = false;
	public boolean borderlessFullscreen = true;
	public boolean enableAmiliousDebugAtStart = false;
	public String graphicsBackend = "jogl"; // "jogl" | "lwjgl"
	/** VSync: 0 = off, 1 = on (default), -1 = adaptive if supported */
	public int swapInterval = 1;

}
