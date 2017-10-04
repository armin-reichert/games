package de.amr.games.pacman2.play.controller;

import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.easy.statemachine.State;
import de.amr.games.pacman2.play.entities.Pacman;
import de.amr.games.pacman2.play.model.PacmanGameData;
import de.amr.games.pacman2.play.view.PacmanGameScene;

public class PacmanGameController extends PacmanGameStateMachine implements Controller {

	public PacmanGameData model;
	public PacmanGameScene view;
	public Pacman pacman;

	public PacmanGameController() {
		setLogger(Application.LOG);
	}

	@Override
	public View currentView() {
		return view;
	}

	@Override
	public void init() {
		super.init();
	}

	@Override
	public void update() {
		readUserInput();
		super.update();
	}

	public void setModel(PacmanGameData model) {
		this.model = model;
	}

	public void setView(PacmanGameScene view) {
		this.view = view;
	}

	private void readUserInput() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_C)) {
			addInput(PacmanGameEvent.COIN_INSERTED);
		}
	}

	@Override
	protected void resetGame(State s) {
		model.reset();
		pacman = new Pacman(view.theme);
		pacman.setRow(26);
		pacman.setCol(13);
	}

	@Override
	protected boolean canPacmanDie() {
		return model.lives == 0;
	}

	@Override
	protected boolean isReadyForPlaying() {
		return true;
	}

}