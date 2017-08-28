package de.amr.games.muehle.play;

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
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.player.api.Player;

/**
 * An alien providing visual and acoustic hints to the assisted player.
 */
class AlienAssistant extends GameEntity {

	private enum SFX {
		CAN_CLOSE_MILL("can_close_mill"),
		CAN_OPPONENT_CLOSE_MILL("can_opponent_close_mill"),
		CAN_OPEN_TWO_MILLS("can_open_two_mills"),
		YO_FINE("yo_fine"),
		WIN("win");

		SFX(String key) {
			this.key = "sfx/" + key + ".mp3";
		}

		final String key;
	}

	private final MillGame game;
	private final MillGameUI gameUI;
	private final Board board;
	private Player assistedPlayer;
	private Player opponentPlayer;
	private boolean enabled;
	private int assistanceLevel; // 0 = normal, 1 = high

	AlienAssistant(MillGame game, MillGameUI gameUI) {
		this.game = game;
		this.gameUI = gameUI;
		this.board = game.getBoard();
		this.assistanceLevel = 0;
		setSprites(new Sprite(Assets.image("images/alien.png")).scale(100, 100));
	}

	void setPlayers(Player assistedPlayer, Player opponentPlayer) {
		this.assistedPlayer = assistedPlayer;
		this.opponentPlayer = opponentPlayer;
	}

	int getAssistanceLevel() {
		return assistanceLevel;
	}

	void setAssistanceLevel(int level) {
		assistanceLevel = level % 2;
	}

	@Override
	public void init() {
		Stream.of(SFX.values()).forEach(sfx -> Assets.sound(sfx.key)); // load all sounds
	}

	void toggle() {
		setEnabled(!enabled);
		if (enabled) {
			tellYoFine();
		}
	}

	void setEnabled(boolean enabled) {
		this.enabled = enabled;
		LOG.info(Messages.text(enabled ? "assistant_on" : "assistant_off"));
	}

	@Override
	public void draw(Graphics2D g) {
		if (!enabled || Stream.of(SFX.values()).map(sfx -> Assets.sound(sfx.key)).noneMatch(snd -> snd.isRunning())) {
			return;
		}
		super.draw(g);
		if (assistanceLevel == 1) {
			if (game.isPlacing()) {
				drawPlacingHints(g, game.getPlayerInTurn().getColor());
			} else if (game.isMoving()) {
				drawMovingHints(g, game.getPlayerInTurn().getColor());
			}
		}
	}

	void drawPlacingHints(Graphics2D g, StoneColor placingColor) {
		gameUI.markPositions(g, board.positionsClosingMill(placingColor), Color.GREEN);
		gameUI.markPositions(g, board.positionsOpeningTwoMills(placingColor), Color.YELLOW);
		gameUI.markPositions(g, board.positionsClosingMill(placingColor.other()), Color.RED);
	}

	void drawMovingHints(Graphics2D g, StoneColor movingColor) {
		if (game.isMoveStartPossible()) {
			game.getMove().ifPresent(move -> gameUI.markPosition(g, move.from, Color.ORANGE));
		} else {
			markPossibleMoveStarts(g, movingColor, game.getPlayerInTurn().canJump());
			markTrappingPosition(g, movingColor, movingColor.other(), Color.RED);
		}
	}

	void markPossibleMoveStarts(Graphics2D g, StoneColor stoneColor, boolean canJump) {
		(canJump ? board.positions(stoneColor) : board.positionsWithEmptyNeighbor(stoneColor))
				.forEach(p -> gameUI.markPosition(g, p, Color.GREEN));
	}

	void markTrappingPosition(Graphics2D g, StoneColor either, StoneColor other, Color color) {
		if (board.positionsWithEmptyNeighbor(other).count() == 1) {
			int singleFreePosition = board.positionsWithEmptyNeighbor(other).findFirst().getAsInt();
			if (board.neighbors(singleFreePosition).filter(board::hasStoneAt)
					.anyMatch(p -> board.getStoneAt(p).get() == either)) {
				gameUI.markPosition(g, singleFreePosition, color);
			}
		}
	}

	private void play(SFX sfx) {
		if (enabled) {
			Assets.sound(sfx.key).play();
		}
	}

	void givePlacingHint() {
		if (enabled && game.getPlayerInTurn() == assistedPlayer) {
			StoneColor placingColor = assistedPlayer.getColor();
			StoneColor opponentColor = opponentPlayer.getColor();

			OptionalInt optPosition = board.positions().filter(p -> board.isMillClosingPosition(p, opponentColor)).findAny();
			if (optPosition.isPresent()) {
				play(SFX.CAN_OPPONENT_CLOSE_MILL);
				return;
			}

			optPosition = board.positions().filter(p -> board.isMillClosingPosition(p, placingColor)).findAny();
			if (optPosition.isPresent()) {
				play(SFX.CAN_CLOSE_MILL);
				return;
			}

			optPosition = board.positionsOpeningTwoMills(placingColor).findAny();
			if (optPosition.isPresent()) {
				play(SFX.CAN_OPEN_TWO_MILLS);
				return;
			}
		}
	}

	void tellMillClosed() {
		if (game.getPlayerInTurn() == assistedPlayer) {
			play(SFX.YO_FINE);
		}
	}

	void tellYoFine() {
		play(SFX.YO_FINE);
	}

	void tellWin() {
		play(SFX.WIN);
	}

}