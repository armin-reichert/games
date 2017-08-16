package de.amr.games.muehle.player;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.util.Util.randomElement;
import static java.lang.String.format;

import java.util.OptionalInt;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Move;
import de.amr.games.muehle.board.StoneColor;

/**
 * Peter aka "Strack"'s player.
 * 
 * @author Armin Reichert, Peter Schillo
 */
public class SmartPlayer implements Player {

	private final Board board;
	private final StoneColor color;
	private Move move;

	public SmartPlayer(Board board, StoneColor color) {
		this.board = board;
		this.color = color;
		move = new Move();
	}

	@Override
	public StoneColor getColor() {
		return color;
	}

	private void reason(String msg, OptionalInt optPos) {
		LOG.info(getName() + ": " + format(msg, optPos.getAsInt()));
	}

	@Override
	public OptionalInt supplyPlacePosition() {

		// Fallback: Zufällige freie Position
		final OptionalInt randomFreePos = randomElement(board.positions().filter(board::isEmptyPosition));

		// Wenn noch kein Stein meiner Farbe gesetzt ist, dann zufällig:
		if (board.positions(color).count() == 0) {
			reason("Setze Stein auf Position %d, weil noch kein Stein meiner Farbe gesetzt wurde", randomFreePos);
			return randomFreePos;
		}

		// Es gibt schon mindestens einen Stein meiner Farbe auf dem Brett.

		// Suche freie Position, an der Mühle meiner Farbe geschlossen werden kann:
		OptionalInt millClosingPos = randomElement(board.positionsClosingMill(color));
		if (millClosingPos.isPresent()) {
			reason("Setze Stein auf Position %d, weil eigene Mühle geschlossen wird", millClosingPos);
			return millClosingPos;
		}

		// Finde gegnerische Position, an der Mühle geschlossen werden kann:
		OptionalInt otherMillClosingPos = randomElement(board.positionsClosingMill(color.other()));
		if (otherMillClosingPos.isPresent()) {
			reason("Setze Stein auf Position %d, weil gegnerische Mühle verhindert wird", otherMillClosingPos);
			return otherMillClosingPos;
		}

		// Finde Position, an der 2 eigene Mühlen geöffnet werden können
		OptionalInt twoMillsOpeningPos = randomElement(board.positionsOpeningTwoMills(color));
		if (twoMillsOpeningPos.isPresent()) {
			reason("Setze Stein auf Position %d, weil zwei eigene Mühlen geöffnet werden", twoMillsOpeningPos);
			return twoMillsOpeningPos;
		}

		// Finde Position, an der eine eigene Mühle geöffnet werden kann
		OptionalInt millOpeningPos = randomElement(board.positionsOpeningMill(color));
		if (millOpeningPos.isPresent()) {
			reason("Setze Stein auf Position %d, weil eigene Mühle geöffnet wird", millOpeningPos);
			return millOpeningPos;
		}

		// Finde Position, an der im nächsten Schritt 2 eigene Mühlen geöffnet werden könnten
		OptionalInt twoMillsLater = randomElement(board.positionsOpeningTwoMillsLater(color));
		if (twoMillsLater.isPresent()) {
			reason("Setze Stein auf Position %d, weil später evtl. 2 eigene Mühlen geöffnet werden könnten", twoMillsLater);
			return twoMillsLater;
		}

		// Finde eine freie Position neben einem Stein meiner Farbe
		OptionalInt posWithFreeNeighbor = randomElement(board.positions(color).filter(board::hasEmptyNeighbor));
		if (posWithFreeNeighbor.isPresent()) {
			OptionalInt neighbor = randomElement(board.emptyNeighbors(posWithFreeNeighbor.getAsInt()));
			reason("Setze Stein auf Position %d, weil es eine freie Position neben eigenem Stein ist", neighbor);
			return neighbor;
		}

		// Fallback
		reason("Setze Stein auf Position %d, weil kein Spezialfall zutraf", randomFreePos);
		return randomFreePos;
	}

	@Override
	public OptionalInt supplyRemovalPosition(StoneColor otherColor) {
		return randomElement(board.positions(otherColor));
	}

	@Override
	public void clearMove() {
		move = new Move();
	}

	@Override
	public Move supplyMove(boolean canJump) {
		if (move.from == -1) {
			// Finde eine Position, von der aus eine Mühle geschlossen werden kann
			OptionalInt millClosingFrom = randomElement(board.positions(color).filter(p -> canCloseMillFrom(p)));
			millClosingFrom.ifPresent(p -> move.from = p);
			if (move.from == -1) {
				// Fallback
				OptionalInt moveStartPos = randomElement(board.positions(color).filter(board::hasEmptyNeighbor));
				moveStartPos.ifPresent(p -> move.from = p);
			}
		} else {
			OptionalInt moveEndPos = supplyMoveEndPosition();
			moveEndPos.ifPresent(p -> move.to = p);
		}
		return move;
	}

	private OptionalInt supplyMoveEndPosition() {
		// Suche freie Position, an der Mühle geschlossen werden kann:
		OptionalInt millClosingPos = randomElement(
				board.positions().filter(board::isEmptyPosition).filter(to -> board.isMillClosedByMove(move.from, to, color)));
		if (millClosingPos.isPresent()) {
			reason("Ziehe Stein zu Position %d, weil eigene Mühle geschlossen wird", millClosingPos);
			return millClosingPos;
		}
		return randomElement(board.emptyNeighbors(move.from));
	}

	private boolean canCloseMillFrom(int p) {
		return board.neighbors(p).anyMatch(q -> board.isMillClosingPosition(q, color));
	}
}