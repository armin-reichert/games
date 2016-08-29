package de.amr.games.birdy.tools;

import static de.amr.easy.game.Application.Assets;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ExtractSprites {

	public static void main(String[] args) throws IOException {
		for (String name : Assets.imageNames()) {
			BufferedImage sprite = Assets.image(name);
			File imageFile = new File("resources/" + name + ".png");
			ImageIO.write(sprite, "png", imageFile);
			System.out.println("Stored sprite image in " + imageFile.getAbsolutePath());
		}
	}

}
