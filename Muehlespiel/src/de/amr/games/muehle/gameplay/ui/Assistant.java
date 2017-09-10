package de.amr.games.muehle.gameplay.ui;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.board.Board.neighbors;
import static de.amr.games.muehle.board.Board.positions;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.gameplay.MillGameController;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.player.Player;

/**
 * An assistant providing visual and acoustic hints to the assisted player.
 */
public class Assistant extends GameEntity {

	public enum HelpLevel {
		OFF, NORMAL, HIGH
	}

	/** Enumeration type for sound IDs */
	private enum SoundID {
		CAN_CLOSE_MILL("can_close_mill"),
		CAN_OPPONENT_CLOSE_MILL("can_opponent_close_mill"),
		CAN_OPEN_TWO_MILLS("can_open_two_mills"),
		YO_FINE("yo_fine"),
		WIN("win");

		private SoundID(String baseName) {
			assetsPath = "sfx/" + baseName + ".mp3";
		}

		public Sound sound() {
			return Assets.sound(assetsPath);
		}

		private final String assetsPath;
	}

	private final MillGameController gameController;
	private final MillGameUI gameUI;

	private HelpLevel helpLevel;

	public Assistant(MillGameController gameController, MillGameUI gameUI) {
		this.gameController = gameController;
		this.gameUI = gameUI;
		this.helpLevel = HelpLevel.OFF;
		setSprites(new Sprite(Assets.image("images/alien.png")).scale(100, 100));
	}

	public HelpLevel getHelpLevel() {
		return helpLevel;
	}

	public void setHelpLevel(HelpLevel level) {
		helpLevel = level;
		if (helpLevel == HelpLevel.OFF) {
			LOG.info(Messages.text("assistant_off"));
		} else {
			tellYoFine();
			LOG.info(Messages.text("assistant_on"));
		}
	}

	@Override
	public void init() {
		// preload sounds
		Stream.of(SoundID.values()).forEach(SoundID::sound);
	}

	public void toggle() {
		setHelpLevel(helpLevel == HelpLevel.OFF ? HelpLevel.NORMAL : HelpLevel.OFF);
	}

	@Override
	public void draw(Graphics2D g) {
		// draw assistant only if any sound is running
		if (helpLevel != HelpLevel.OFF && Stream.of(SoundID.values()).map(SoundID::sound).anyMatch(Sound::isRunning)) {
			super.draw(g);
			if (helpLevel == HelpLevel.HIGH && gameController.getPlayerInTurn().isInteractive()) {
				if (gameController.isPlacing()) {
					gameUI.markPositions(g, gameController.getBoard().positionsClosingMill(gameController.getPlayerInTurn().getColor()), Color.GREEN);
					gameUI.markPositions(g, gameController.getBoard().positionsOpeningTwoMills(gameController.getPlayerInTurn().getColor()),
							Color.YELLOW);
					gameUI.markPositions(g, gameController.getBoard().positionsClosingMill(gameController.getPlayerNotInTurn().getColor()),
							Color.RED);
				} else if (gameController.isMoving()) {
					markPossibleMoveStarts(g, gameController.getPlayerInTurn().getColor(), Color.GREEN);
					markTrappingPosition(g, gameController.getPlayerInTurn().getColor(), gameController.getPlayerNotInTurn().getColor(), Color.RED);
				}
			}
		}
	}

	private void markPossibleMoveStarts(Graphics2D g, StoneColor stoneColor, Color color) {
		(gameController.getPlayerInTurn().canJump() ? gameController.getBoard().positions(stoneColor)
				: gameController.getBoard().positionsWithEmptyNeighbor(stoneColor)).forEach(p -> gameUI.markPosition(g, p, color));
	}

	private void markTrappingPosition(Graphics2D g, StoneColor either, StoneColor other, Color color) {
		if (gameController.getBoard().positionsWithEmptyNeighbor(other).count() == 1) {
			int singleFreePosition = gameController.getBoard().positionsWithEmptyNeighbor(other).findFirst().getAsInt();
			if (neighbors(singleFreePosition).filter(gameController.getBoard()::hasStoneAt)
					.anyMatch(p -> gameController.getBoard().getStoneAt(p).get() == either)) {
				gameUI.markPosition(g, singleFreePosition, color);
			}
		}
	}

	private void play(SoundID soundID) {
		if (helpLevel != HelpLevel.OFF) {
			soundID.sound().play();
		}
	}

	public void givePlacingHint(Player player) {
		if (helpLevel != HelpLevel.OFF) {
			IntStream positions;
			StoneColor color = player.getColor();

			// can opponent close mill?
			positions = positions().filter(p -> gameController.getBoard().isMillClosingPosition(p, color.other()));
			if (positions.findAny().isPresent()) {
				play(SoundID.CAN_OPPONENT_CLOSE_MILL);
				return;
			}

			// can close own mill?
			positions = positions().filter(p -> gameController.getBoard().isMillClosingPosition(p, color));
			if (positions.findAny().isPresent()) {
				play(SoundID.CAN_CLOSE_MILL);
				return;
			}

			// can open two mills at once?
			positions = gameController.getBoard().positionsOpeningTwoMills(color);
			if (positions.findAny().isPresent()) {
				play(SoundID.CAN_OPEN_TWO_MILLS);
				return;
			}
		}
	}

	public void tellMillClosed() {
		play(SoundID.YO_FINE);
	}

	public void tellYoFine() {
		play(SoundID.YO_FINE);
	}

	public void tellWin(Player player) {
		play(SoundID.WIN);
	}

}