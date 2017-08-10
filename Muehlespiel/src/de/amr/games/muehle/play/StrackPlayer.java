package de.amr.games.muehle.play;

import static de.amr.easy.game.Application.LOG;
import static java.lang.String.format;

import java.util.OptionalInt;

import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.ui.Board;

/**
 * Peter aka "Strack"'s player.
 * 
 * @author Armin Reichert, Peter Schillo
 */
public class StrackPlayer extends AbstractPlayer {

	public StrackPlayer(MillApp app, Board board, StoneColor color) {
		super(app, board, color);
	}

	private void reason(String msg, OptionalInt optPos) {
		LOG.info(getName() + ": " + format(msg, optPos.getAsInt()));
	}

	@Override
	public OptionalInt supplyPlacePosition() {

		// Fallback: Zufällige freie Position
		final OptionalInt randomFreePos = randomElement(model.positions().filter(model::isEmptyPosition));

		// Wenn noch kein Stein meiner Farbe gesetzt ist, dann zufällig:
		if (model.positions(color).count() == 0) {
			reason("Setze Stein auf Position %d, weil noch kein Stein meiner Farbe gesetzt wurde", randomFreePos);
			return randomFreePos;
		}

		// Es gibt schon mindestens einen Stein meiner Farbe auf dem Brett.

		// Suche freie Position, an der Mühle meiner Farbe geschlossen werden kann:
		OptionalInt millClosingPos = randomElement(model.positionsClosingMill(color));
		if (millClosingPos.isPresent()) {
			reason("Setze Stein auf Position %d, weil eigene Mühle geschlossen wird", millClosingPos);
			return millClosingPos;
		}

		// Finde gegnerische Position, an der Mühle geschlossen werden kann:
		OptionalInt otherMillClosingPos = randomElement(model.positionsClosingMill(otherColor));
		if (otherMillClosingPos.isPresent()) {
			reason("Setze Stein auf Position %d, weil gegnerische Mühle verhindert wird", otherMillClosingPos);
			return otherMillClosingPos;
		}

		// Finde Position, an der 2 eigene Mühlen geöffnet werden können
		OptionalInt twoMillsOpeningPos = randomElement(model.positionsOpeningTwoMills(color));
		if (twoMillsOpeningPos.isPresent()) {
			reason("Setze Stein auf Position %d, weil zwei eigene Mühlen geöffnet werden", twoMillsOpeningPos);
			return twoMillsOpeningPos;
		}

		// Finde Position, an der eine eigene Mühle geöffnet werden kann
		OptionalInt millOpeningPos = randomElement(model.positionsOpeningMill(color));
		if (millOpeningPos.isPresent()) {
			reason("Setze Stein auf Position %d, weil eigene Mühle geöffnet wird", millOpeningPos);
			return millOpeningPos;
		}

		// Finde Position, an der im nächsten Schritt 2 eigene Mühlen geöffnet werden könnten
		OptionalInt twoMillsLater = randomElement(model.twoMillsLaterPositions(color));
		if (twoMillsLater.isPresent()) {
			reason("Setze Stein auf Position %d, weil später evtl. 2 eigene Mühlen geöffnet werden könnten", twoMillsLater);
			return twoMillsLater;
		}

		// Finde eine freie Position neben einem Stein meiner Farbe
		OptionalInt posWithFreeNeighbor = randomElement(model.positions(color).filter(model::hasEmptyNeighbor));
		if (posWithFreeNeighbor.isPresent()) {
			OptionalInt neighbor = randomElement(model.emptyNeighbors(posWithFreeNeighbor.getAsInt()));
			reason("Setze Stein auf Position %d, weil es eine freie Position neben eigenem Stein ist", neighbor);
			return neighbor;
		}

		// Fallback
		reason("Setze Stein auf Position %d, weil kein Spezialfall zutraf", randomFreePos);
		return randomFreePos;
	}

	@Override
	public OptionalInt supplyRemovalPosition(StoneColor otherColor) {
		return randomElement(model.positions(otherColor));
	}

	@Override
	public OptionalInt supplyMoveStartPosition() {
		return randomElement(model.positions(color).filter(model::hasEmptyNeighbor));
	}

	@Override
	public OptionalInt supplyMoveEndPosition(int from) {
		// Suche freie Position, an der Mühle meiner Farbe geschlossen werden kann:
		OptionalInt millClosingPos = randomElement(
				model.positionsClosingMill(color).filter(to -> model.canCloseMill(from, to, color)));
		if (millClosingPos.isPresent()) {
			reason("Ziehe Stein zu Position %d, weil eigene Mühle geschlossen wird", millClosingPos);
			return millClosingPos;
		}
		return randomElement(model.emptyNeighbors(from));
	}
}