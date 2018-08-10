package de.amr.games.pacman.view;

import java.awt.Color;

import de.amr.easy.game.view.ViewController;

public interface GameViewController extends ViewController {

	void enableAnimation(boolean enable);

	void showInfo(String text, Color color);
	
	void hideInfo();
	
	void setBonusTimer(int ticks);
	
	void setMazeFlashing(boolean flashing);

}
