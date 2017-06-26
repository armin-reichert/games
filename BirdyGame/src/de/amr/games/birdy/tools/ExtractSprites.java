package de.amr.games.birdy.tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.amr.games.birdy.BirdyGame;

public class ExtractSprites {

	public static void main(String[] args) throws IOException {
		BirdyGame app = new BirdyGame();
		for (String name : app.assets.imageNames()) {
			BufferedImage sprite = app.assets.image(name);
			File imageFile = new File("resources/" + name + ".png");
			ImageIO.write(sprite, "png", imageFile);
			System.out.println("Stored sprite image in " + imageFile.getAbsolutePath());
		}
	}
}
