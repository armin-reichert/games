package de.amr.games.muehle.game.impl;

import static de.amr.easy.game.Application.LOG;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.OptionalInt;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.game.api.MillGame;
import de.amr.games.muehle.game.api.MillGameUI;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.player.api.Player;

/**
 * An assistant providing visual and acoustic hints to the assisted player.
 */
public class Assistant extends GameEntity {

	/** Enumeration of used sounds */
	private enum Sounds {
		CAN_CLOSE_MILL("can_close_mill"),
		CAN_OPPONENT_CLOSE_MILL("can_opponent_close_mill"),
		CAN_OPEN_TWO_MILLS("can_open_two_mills"),
		YO_FINE("yo_fine"),
		WIN("win");

		Sounds(String key) {
			this.key = "sfx/" + key + ".mp3";
		}

		final String key;
	}

	private final MillGame game;
	private final Board board;
	private final MillGameUI gameUI;
	private Player assistedPlayer;
	private Player opponentPlayer;
	private boolean enabled;
	private int assistanceLevel; // 1 = normal, 2 = high

	public Assistant(MillGame game, MillGameUI gameUI) {
		this.game = game;
		this.assistedPlayer = game.getWhitePlayer();
		this.opponentPlayer = game.getBlackPlayer();
		this.gameUI = gameUI;
		this.board = game.getBoard();
		this.assistanceLevel = 1;
		setSprites(new Sprite(Assets.image("images/alien.png")).scale(100, 100));
	}

	public int getAssistanceLevel() {
		return assistanceLevel;
	}

	public void setAssistanceLevel(int level) {
		assistanceLevel = level;
	}

	@Override
	public void init() {
		// preload sounds
		Stream.of(Sounds.values()).forEach(snd -> Assets.sound(snd.key));
	}

	public void toggle() {
		setEnabled(!enabled);
		if (enabled) {
			tellYoFine();
		}
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		LOG.info(Messages.text(enabled ? "assistant_on" : "assistant_off"));
	}

	@Override
	public void draw(Graphics2D g) {
		// draw assistant only if enabled and some sound is running
		if (enabled && Stream.of(Sounds.values()).map(snd -> Assets.sound(snd.key)).anyMatch(sound -> sound.isRunning())) {
			super.draw(g);
			if (assistanceLevel == 2 && game.getPlayerInTurn().isInteractive()) {
				if (game.isPlacing()) {
					gameUI.markPositions(g, board.positionsClosingMill(game.getPlayerInTurn().getColor()), Color.GREEN);
					gameUI.markPositions(g, board.positionsOpeningTwoMills(game.getPlayerInTurn().getColor()), Color.YELLOW);
					gameUI.markPositions(g, board.positionsClosingMill(game.getPlayerNotInTurn().getColor()), Color.RED);
				} else if (game.isMoving()) {
					markPossibleMoveStarts(g, game.getPlayerInTurn().getColor(), Color.GREEN);
					markTrappingPosition(g, game.getPlayerInTurn().getColor(), game.getPlayerNotInTurn().getColor(), Color.RED);
				}
			}
		}
	}

	private void markPossibleMoveStarts(Graphics2D g, StoneColor stoneColor, Color color) {
		(game.getPlayerInTurn().canJump() ? board.positions(stoneColor) : board.positionsWithEmptyNeighbor(stoneColor))
				.forEach(p -> gameUI.markPosition(g, p, color));
	}

	private void markTrappingPosition(Graphics2D g, StoneColor either, StoneColor other, Color color) {
		if (board.positionsWithEmptyNeighbor(other).count() == 1) {
			int singleFreePosition = board.positionsWithEmptyNeighbor(other).findFirst().getAsInt();
			if (board.neighbors(singleFreePosition).filter(board::hasStoneAt)
					.anyMatch(p -> board.getStoneAt(p).get() == either)) {
				gameUI.markPosition(g, singleFreePosition, color);
			}
		}
	}

	private void play(Sounds snd) {
		if (enabled) {
			Assets.sound(snd.key).play();
		}
	}

	public void givePlacingHint() {
		if (enabled && game.getPlayerInTurn() == assistedPlayer) {
			StoneColor placingColor = assistedPlayer.getColor();
			StoneColor opponentColor = opponentPlayer.getColor();

			OptionalInt optPosition = board.positions().filter(p -> board.isMillClosingPosition(p, opponentColor)).findAny();
			if (optPosition.isPresent()) {
				play(Sounds.CAN_OPPONENT_CLOSE_MILL);
				return;
			}

			optPosition = board.positions().filter(p -> board.isMillClosingPosition(p, placingColor)).findAny();
			if (optPosition.isPresent()) {
				play(Sounds.CAN_CLOSE_MILL);
				return;
			}

			optPosition = board.positionsOpeningTwoMills(placingColor).findAny();
			if (optPosition.isPresent()) {
				play(Sounds.CAN_OPEN_TWO_MILLS);
				return;
			}
		}
	}

	public void tellMillClosed() {
		if (game.getPlayerInTurn() == assistedPlayer) {
			play(Sounds.YO_FINE);
		}
	}

	public void tellYoFine() {
		play(Sounds.YO_FINE);
	}

	public void tellWin() {
		play(Sounds.WIN);
	}

}