package de.amr.easy.game.assets;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import de.amr.easy.game.Application;

public class Assets {

	private final Map<String, Font> fonts = new HashMap<>();
	private final Map<String, Image> images = new HashMap<>();
	private final Map<String, Sound> sounds = new HashMap<>();
	private final Map<String, String> texts = new HashMap<>();

	private InputStream asInputStream(String path) {
		InputStream istream = Assets.class.getClassLoader().getResourceAsStream(path);
		if (istream == null) {
			Application.Log.severe("Could not get input stream for path: " + path);
			throw new IllegalArgumentException();
		}
		return istream;
	}

	/**
	 * Reads a text file from the given assets path.
	 * 
	 * @param path
	 *          relative path inside "assets" folder
	 * @return the text file content as a single string
	 */
	public String readTextFile(String path) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(asInputStream(path)));
			StringBuilder sb = new StringBuilder();
			for (String line; (line = reader.readLine()) != null;) {
				sb.append(line).append('\n');
			}
			return sb.toString();
		} catch (IOException e) {
			Application.Log.severe("Could not read text resource from path: " + path);
			throw new IllegalArgumentException();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Font readFont(String path) {
		InputStream fontStream = null;
		try {
			fontStream = asInputStream(path);
			return Font.createFont(Font.TRUETYPE_FONT, fontStream);
		} catch (Exception e) {
			Application.Log.severe("Could not read font resource from path: " + path);
			throw new IllegalArgumentException();
		}
	}

	public BufferedImage readImage(String path) {
		InputStream is = null;
		try {
			is = asInputStream(path);
			BufferedImage image = ImageIO.read(is);
			if (image == null) {
				Application.Log.severe("Unsupported image resource at path: " + path);
				throw new IllegalArgumentException();
			}
			return image;
		} catch (IOException e) {
			Application.Log.severe("Could not read image resource from path: " + path);
			throw new IllegalArgumentException();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException x) {
					x.printStackTrace();
				}
			}
		}
	}

	public Iterable<String> imageNames() {
		return images.keySet();
	}

	public Iterable<String> soundNames() {
		return sounds.keySet();
	}
	
	public Stream<Sound> sounds() {
		return sounds.values().stream();
	}

	public void storeImage(String path, Image image) {
		if (images.put(path, image) != null) {
			Application.Log.warning("Image with name: " + path + " has been replaced.");
		}
	}

	public static BufferedImage scaledImage(Image image, int width, int height) {
		Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		BufferedImage copy = new BufferedImage(scaled.getWidth(null), scaled.getHeight(null),
				BufferedImage.TYPE_INT_ARGB);
		Graphics g = copy.getGraphics();
		g.drawImage(scaled, 0, 0, null);
		g.dispose();
		return copy;
	}
	
	public Font storeFont(String key, String fontName, float size, int style) {
		if (!fonts.containsKey(key)) {
			Font font = readFont(fontName).deriveFont(style, size);
			fonts.put(key, font);
		}
		return fonts.get(key);
	}
	
	public Font font(String key) {
		if (fonts.containsKey(key)) {
			return fonts.get(key);
		}
		throw new IllegalStateException("Font not found");
	}

	@SuppressWarnings("unchecked")
	public <T extends Image> T image(String key) {
		if (!images.containsKey(key)) {
			images.put(key, readImage(key));
		}
		return (T) images.get(key);
	}

	public Sound sound(String key) {
		if (!sounds.containsKey(key)) {
			InputStream is = asInputStream(key);
			sounds.put(key, new AudioClip(is));
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sounds.get(key);
	}

	public String text(String key) {
		if (!texts.containsKey(key)) {
			String text = readTextFile(key);
			texts.put(key, text);
		}
		return texts.get(key);
	}

	public String overview() {
		StringBuilder s = new StringBuilder();
		String[] fontNames = fonts.keySet().toArray(new String[fonts.size()]);
		String[] imageNames = images.keySet().toArray(new String[images.size()]);
		String[] soundNames = sounds.keySet().toArray(new String[sounds.size()]);
		Arrays.sort(fontNames);
		Arrays.sort(imageNames);
		Arrays.sort(soundNames);
		s.append("\n-- Fonts:\n");
		for (String name : fontNames) {
			s.append(name).append(": ").append(font(name)).append("\n");
		}
		s.append("\n-- Images:\n");
		for (String name : imageNames) {
			Image image = image(name);
			s.append(name).append(": ").append(image.getWidth(null) + "x" + image.getHeight(null))
					.append("\n");
		}
		s.append("\n-- Sounds:\n");
		for (String name : soundNames) {
			Sound sound = sound(name);
			s.append(name).append(": ").append(sound.getClass().getSimpleName()).append("\n");
		}
		s.append("\n-- Texts:\n");
		texts.entrySet().stream().forEach(entry -> {
			s.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
		});
		return s.toString();
	}
}
