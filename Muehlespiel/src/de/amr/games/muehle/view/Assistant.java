package de.amr.games.muehle.view;

import static de.amr.games.muehle.model.board.Board.neighbors;
import static de.amr.games.muehle.model.board.Board.positions;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.entity.SpriteEntity;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.muehle.controller.game.MillGameController;
import de.amr.games.muehle.controller.game.MillGameState;
import de.amr.games.muehle.controller.player.Player;
import de.amr.games.muehle.model.board.Board;
import de.amr.games.muehle.model.board.StoneColor;
import de.amr.games.muehle.msg.Messages;

/**
 * An assistant providing visual and acoustic hints to the assisted player.
 */
public class Assistant extends SpriteEntity {

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

	private final Board board;
	private final MillGameController control;
	private MillGameUI view;
	private HelpLevel helpLevel;

	public Assistant(MillGameController control) {
		this.control = control;
		this.board = control.model.board;
		this.helpLevel = HelpLevel.OFF;
		sprites.set("s_alien", Sprite.of(Assets.image("images/alien.png")).scale(100, 100));
		sprites.select("s_alien");
		tf.setWidth(sprites.current().getWidth());
		tf.setHeight(sprites.current().getHeight());
	}

	public void setView(MillGameUI view) {
		this.view = view;
	}

	public HelpLevel getHelpLevel() {
		return helpLevel;
	}

	public void setHelpLevel(HelpLevel level) {
		helpLevel = level;
		if (helpLevel == HelpLevel.OFF) {
			Application.LOGGER.info(Messages.text("assistant_off"));
		} else {
			tellYoFine();
			Application.LOGGER.info(Messages.text("assistant_on"));
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
		if (helpLevel != HelpLevel.OFF
				&& Stream.of(SoundID.values()).map(SoundID::sound).anyMatch(Sound::isRunning)) {
			super.draw(g);
			if (helpLevel == HelpLevel.HIGH && control.playerInTurn().isInteractive()) {
				MillGameState state = control.getFsm().getState();
				if (state == MillGameState.PLACING || state == MillGameState.PLACING_REMOVING) {
					view.markPositions(g, board.positionsClosingMill(control.playerInTurn().color()), Color.GREEN);
					view.markPositions(g, board.positionsOpeningTwoMills(control.playerInTurn().color()), Color.YELLOW);
					view.markPositions(g, board.positionsClosingMill(control.playerNotInTurn().color()), Color.RED);
				} else if (state == MillGameState.MOVING || state == MillGameState.MOVING_REMOVING) {
					markPossibleMoveStarts(g, control.playerInTurn().color(), Color.GREEN);
					markTrappingPosition(g, control.playerInTurn().color(), control.playerNotInTurn().color(),
							Color.RED);
				}
			}
		}
	}

	private void markPossibleMoveStarts(Graphics2D g, StoneColor stoneColor, Color color) {
		(control.playerInTurn().canJump() ? board.positions(stoneColor)
				: board.positionsWithEmptyNeighbor(stoneColor)).forEach(p -> view.markPosition(g, p, color));
	}

	private void markTrappingPosition(Graphics2D g, StoneColor either, StoneColor other, Color color) {
		if (board.positionsWithEmptyNeighbor(other).count() == 1) {
			int singleFreePosition = board.positionsWithEmptyNeighbor(other).findFirst().getAsInt();
			if (neighbors(singleFreePosition).filter(board::hasStoneAt)
					.anyMatch(p -> board.getStoneAt(p).get() == either)) {
				view.markPosition(g, singleFreePosition, color);
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
			StoneColor color = player.color();

			// can opponent close mill?
			positions = positions().filter(p -> board.isMillClosingPosition(p, color.other()));
			if (positions.findAny().isPresent()) {
				play(SoundID.CAN_OPPONENT_CLOSE_MILL);
				return;
			}

			// can close own mill?
			positions = positions().filter(p -> board.isMillClosingPosition(p, color));
			if (positions.findAny().isPresent()) {
				play(SoundID.CAN_CLOSE_MILL);
				return;
			}

			// can open two mills at once?
			positions = board.positionsOpeningTwoMills(color);
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