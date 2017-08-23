package de.amr.games.muehle.play;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.play.GamePhase.MOVING;
import static de.amr.games.muehle.play.GamePhase.PLACING;

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
import de.amr.games.muehle.ui.BoardUI;

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

	private final PlayScene scene;
	private final BoardUI boardUI;
	private final Board board;
	private Player assistedPlayer;
	private Player opponentPlayer;
	private boolean enabled;
	private int assistanceLevel; // 0 = normal, 1 = high

	AlienAssistant(PlayScene scene) {
		this.scene = scene;
		this.boardUI = scene.getBoardUI();
		this.board = this.boardUI.board();
		this.assistanceLevel = 0;
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
		setSprites(new Sprite(Assets.image("images/alien.png")).scale(100, 100));
		Stream.of(SFX.values()).forEach(sfx -> Assets.sound(sfx.key)); // load all sounds
	}

	void moveHome() {
		hCenter(scene.getWidth());
		tf.setY(scene.getHeight() / 2 - getHeight());
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
			if (scene.getControl().is(PLACING)) {
				drawPlacingHints(g, scene.getPlayerInTurn().getColor());
			} else if (scene.getControl().is(MOVING)) {
				drawMovingHints(g, scene.getPlayerInTurn().getColor());
			}
		}
	}

	void drawPlacingHints(Graphics2D g, StoneColor placingColor) {
		boardUI.markPositionsClosingMill(g, placingColor, Color.GREEN);
		boardUI.markPositionsOpeningTwoMills(g, placingColor, Color.YELLOW);
		boardUI.markPositionsClosingMill(g, placingColor.other(), Color.RED);
	}

	void drawMovingHints(Graphics2D g, StoneColor movingColor) {
		if (scene.getMoveControl().isMoveStartPossible()) {
			scene.getMoveControl().getMove().ifPresent(move -> boardUI.markPosition(g, move.from, Color.ORANGE));
		} else {
			boardUI.markPossibleMoveStarts(g, movingColor, scene.getPlayerInTurn().canJump());
			boardUI.markPositionTrappingOpponent(g, movingColor, movingColor.other(), Color.RED);
		}
	}

	private void play(SFX sfx) {
		if (enabled) {
			Assets.sound(sfx.key).play();
		}
	}

	void givePlacingHint() {
		if (enabled && scene.getPlayerInTurn() == assistedPlayer) {
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
		if (scene.getPlayerInTurn() == assistedPlayer) {
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