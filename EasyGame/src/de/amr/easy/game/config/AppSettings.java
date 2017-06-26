package de.amr.easy.game.config;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import de.amr.easy.game.ui.FullScreen;

public class AppSettings {

	public String title = "My Application!";
	public int width = 600;
	public int height = 400;
	public float scale = 1f;
	public boolean fullScreenOnStart = false;
	public FullScreen fullScreenMode = null;
	public Color bgColor = Color.BLACK;

	private final Map<String, Object> settings = new HashMap<>();

	public void set(String key, Object value) {
		settings.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		return (T) settings.get(key);
	}

	public String getString(String key) {
		return String.valueOf(get(key));
	}

	public boolean getBool(String key) {
		return settings.containsKey(key) ? (Boolean) get(key) : false;
	}

	public int getInt(String key) {
		return (Integer) get(key);
	}

	public Color getColor(String key) {
		return (Color) get(key);
	}
}
