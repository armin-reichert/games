package de.amr.games.pacman2.play.view;

import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.stream.IntStream;

import de.amr.easy.game.scene.Scene;
import de.amr.easy.game.view.Controller;
import de.amr.games.pacman.theme.ClassicTheme;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman2.play.PacmanGameApp;
import de.amr.games.pacman2.play.controller.PacmanGameController;
import de.amr.games.pacman2.play.model.PacmanGameData;

public class PacmanGameScene extends Scene<PacmanGameApp> {

	public PacManTheme theme;
	public PacmanGameData model;
	public PacmanGameController controller;

	public PacmanGameScene(PacmanGameApp app) {
		super(app);
		theme = new ClassicTheme();
	}

	@Override
	public void init() {
	}

	public void setController(PacmanGameController controller) {
		this.controller = controller;
	}

	public void setModel(PacmanGameData model) {
		this.model = model;
	}

	@Override
	public Controller getController() {
		return controller;
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		drawBoard(g, 3);
		drawPacman(g);
		drawGrid(g);
	}

	private void drawPacman(Graphics2D g) {
		controller.pacman.draw(g);
	}

	private void drawBoard(Graphics2D g, int row) {
		g.translate(0, row * TILE_SIZE);
		theme.getBoardSprite().draw(g);
		g.translate(0, -row * TILE_SIZE);
	}

	private void drawGrid(Graphics2D g) {
		g.setColor(Color.LIGHT_GRAY);
		IntStream.range(1, 36).forEach(row -> {
			g.drawLine(0, row * TILE_SIZE, getWidth(), row * TILE_SIZE);
		});
		IntStream.range(1, 28).forEach(col -> {
			g.drawLine(col * TILE_SIZE, 0, col * TILE_SIZE, getHeight());
		});
	}

}
