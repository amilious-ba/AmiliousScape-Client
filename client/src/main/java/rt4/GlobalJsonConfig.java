package rt4;

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

	String ip_management = "amilious.xyz";
	String ip_address = "amilious.xyz";
	int world = 1;
	int server_port = 43594;
	int wl_port = 43595;
	int js5_port = 43595;
	boolean mouseWheelZoom = GlobalConfig.MOUSEWHEEL_ZOOM;
	public String pluginsFolder = "plugins";
	public boolean startFullscreen = false;
	public boolean borderlessFullscreen = true;
	public boolean enableAmiliousDebugAtStart = false;
	/** VSync: 0 = off, 1 = on (default), -1 = adaptive if supported */
	public int swapInterval = 1;
	public String voiceoverSpeaker = "";      // empty = off
	public String elevenLabsKey = "";
	public String elevenLabsMale = "pNInz6obpgDQGcFmaJgB";
	public String elevenLabsFemale = "21m00Tcm4TlvDq8ikWAM";
	public String openaiKey = "";
	public String openaiModel = "tts-1";           // or tts-1-hd
	public String openaiVoiceMale = "onyx";        // onyx, echo
	public String openaiVoiceFemale = "nova";      // nova, shimmer, alloy

}
