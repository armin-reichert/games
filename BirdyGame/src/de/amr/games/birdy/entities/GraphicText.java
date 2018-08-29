package de.amr.games.birdy.entities;

import java.awt.Image;

import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.sprite.Sprite;

public class GraphicText extends GameEntityUsingSprites {

	public GraphicText(Image image) {
		addSprite("s_text", new Sprite(image));
		setCurrentSprite("s_text");
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}
}
