package de.amr.games.muehle.game.impl;

import static de.amr.easy.game.Application.LOG;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.OptionalInt;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.game.api.MillGame;
import de.amr.games.muehle.game.api.MillGameUI;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.player.api.Player;

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

	private final MillGame game;
	private final MillGameUI gameUI;

	private HelpLevel helpLevel; // 0 = off, 1 = normal, 2 = high

	public Assistant(MillGame game, MillGameUI gameUI) {
		this.game = game;
		this.gameUI = gameUI;
		this.helpLevel = HelpLevel.OFF;
		setSprites(new Sprite(Assets.image("images/alien.png")).scale(100, 100));
	}

	public HelpLevel getAssistanceLevel() {
		return helpLevel;
	}

	public void setHelpLevel(HelpLevel level) {
		helpLevel = level;
		if (helpLevel != HelpLevel.OFF) {
			LOG.info(Messages.text("assistant_on"));
			tellYoFine();
		} else {
			LOG.info(Messages.text("assistant_off"));
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
		// draw assistant only if enabled and some sound is running
		if (helpLevel != HelpLevel.OFF
				&& Stream.of(SoundID.values()).map(SoundID::sound).anyMatch(Sound::isRunning)) {
			super.draw(g);
			if (helpLevel == HelpLevel.HIGH && game.getPlayerInTurn().isInteractive()) {
				if (game.isPlacing()) {
					gameUI.markPositions(g, game.getBoard().positionsClosingMill(game.getPlayerInTurn().getColor()), Color.GREEN);
					gameUI.markPositions(g, game.getBoard().positionsOpeningTwoMills(game.getPlayerInTurn().getColor()),
							Color.YELLOW);
					gameUI.markPositions(g, game.getBoard().positionsClosingMill(game.getPlayerNotInTurn().getColor()),
							Color.RED);
				} else if (game.isMoving() && game.getPlayerInTurn().isInteractive()) {
					markPossibleMoveStarts(g, game.getPlayerInTurn().getColor(), Color.GREEN);
					markTrappingPosition(g, game.getPlayerInTurn().getColor(), game.getPlayerNotInTurn().getColor(), Color.RED);
				}
			}
		}
	}

	private void markPossibleMoveStarts(Graphics2D g, StoneColor stoneColor, Color color) {
		(game.getPlayerInTurn().canJump() ? game.getBoard().positions(stoneColor)
				: game.getBoard().positionsWithEmptyNeighbor(stoneColor)).forEach(p -> gameUI.markPosition(g, p, color));
	}

	private void markTrappingPosition(Graphics2D g, StoneColor either, StoneColor other, Color color) {
		if (game.getBoard().positionsWithEmptyNeighbor(other).count() == 1) {
			int singleFreePosition = game.getBoard().positionsWithEmptyNeighbor(other).findFirst().getAsInt();
			if (game.getBoard().neighbors(singleFreePosition).filter(game.getBoard()::hasStoneAt)
					.anyMatch(p -> game.getBoard().getStoneAt(p).get() == either)) {
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
			StoneColor placingColor = player.getColor();

			OptionalInt optPosition = game.getBoard().positions()
					.filter(p -> game.getBoard().isMillClosingPosition(p, placingColor.other())).findAny();
			if (optPosition.isPresent()) {
				play(SoundID.CAN_OPPONENT_CLOSE_MILL);
				return;
			}

			optPosition = game.getBoard().positions().filter(p -> game.getBoard().isMillClosingPosition(p, placingColor))
					.findAny();
			if (optPosition.isPresent()) {
				play(SoundID.CAN_CLOSE_MILL);
				return;
			}

			optPosition = game.getBoard().positionsOpeningTwoMills(placingColor).findAny();
			if (optPosition.isPresent()) {
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