package de.amr.games.muehle.play;

import static de.amr.easy.game.Application.LOG;

import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.IntStream;

import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.Direction;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.ui.Board;

public class StrackPlayer extends AbstractPlayer {

	public StrackPlayer(MillApp app, Board board, StoneColor color) {
		super(app, board, color);
	}

	private void reason(String msg, OptionalInt optPos) {
		LOG.info(String.format(msg, optPos.getAsInt()));
	}

	@Override
	public OptionalInt supplyPlacePosition() {

		// Fallback: Zufällige freie Position
		final OptionalInt randomFreePos = randomElement(model.positions().filter(p -> model.isEmptyPosition(p)));

		// Wenn noch kein Stein meiner Farbe gesetzt ist, dann zufällig:
		if (model.positions(color).count() == 0) {
			reason("Setze an Position %d, weil noch kein Stein meiner Farbe gesetzt wurde", randomFreePos);
			return randomFreePos;
		}

		// Es gibt schon mindestens einen Stein meiner Farbe auf dem Brett.

		// Suche freie Position, an der Mühle meiner Farbe geschlossen werden kann:
		OptionalInt millClosingPos = randomElement(model.positionsForClosingMill(color));
		if (millClosingPos.isPresent()) {
			reason("Setze an Position %d, weil eigene Mühle geschlossen wird", millClosingPos);
			return millClosingPos;
		}

		// Finde gegnerische Position, an der Mühle geschlossen werden kann:
		OptionalInt otherMillClosingPos = randomElement(model.positionsForClosingMill(otherColor));
		if (otherMillClosingPos.isPresent()) {
			reason("Setze an Position %d, weil gegnerische Mühle verhindert wird", otherMillClosingPos);
			return otherMillClosingPos;
		}

		// Finde Position, an der 2 eigene Mühlen geöffnet werden können
		OptionalInt twoMillsOpeningPos = randomElement(model.positionsOpeningTwoMills(color));
		if (twoMillsOpeningPos.isPresent()) {
			reason("Setze an Position %d, weil zwei eigene Mühlen geöffnet werden", twoMillsOpeningPos);
			return twoMillsOpeningPos;
		}

		// Finde Position, an der eine eigene Mühle geöffnet werden kann
		OptionalInt millOpeningPos = randomElement(model.positionsForOpeningMill(color));
		if (millOpeningPos.isPresent()) {
			reason("Setze an Position %d, weil eigene Mühle geöffnet wird", millOpeningPos);
			return millOpeningPos;
		}

		// Finde Position, an der im nächsten Schritt 2 Mühlen geöffnet werden könnten
		OptionalInt twoMillsLater = randomElement(findTwoMillsLaterPositions());
		if (twoMillsLater.isPresent()) {
			reason("Setze an Position %d, weil später evtl. 2 eigene Mühlen geöffnet werden könnten", twoMillsLater);
			return twoMillsLater;
		}

		// Finde eine freie Position neben einem Stein meiner Farbe
		OptionalInt posWithFreeNeighbor = randomElement(model.positions(color).filter(p -> model.hasEmptyNeighbor(p)));
		if (posWithFreeNeighbor.isPresent()) {
			OptionalInt neighbor = randomElement(model.emptyNeighbors(posWithFreeNeighbor.getAsInt()));
			reason("Setze an Position %d, weil es eine freie Position neben eigenem Stein ist", neighbor);
			return neighbor;
		}

		// Fallback
		reason("Setze an Position %d, weil kein Spezialfall zutraf", randomFreePos);
		return randomFreePos;
	}

	private IntStream findTwoMillsLaterPositions() {
		return model.positions().filter(model::isEmptyPosition).filter(this::isTwoMillsLaterPosition);
	}

	private boolean isTwoMillsLaterPosition(int p) {
		Set<Integer> candidates = new HashSet<>();
		model.neighbors(p).filter(model::isEmptyPosition).forEach(q -> model.neighbors(q).forEach(candidates::add));
		candidates.remove(p);
		for (int q : candidates) {
			if (model.getStoneAt(q) == color) {
				// Finde gemeinsamen Nachbarn von p und q
				int commonNeighbor = model.neighbors(p).filter(n -> model.areNeighbors(n, q)).findFirst().getAsInt();
				Direction dir1 = model.getDirection(p, commonNeighbor).get();
				Direction dir2 = model.getDirection(q, commonNeighbor).get();
				OptionalInt neighbor1 = model.neighbor(p, dir1.opposite());
				OptionalInt neighbor2 = model.neighbor(q, dir2.opposite());
				if (neighbor1.isPresent() && neighbor2.isPresent() && model.isEmptyPosition(neighbor1.getAsInt())
						&& model.isEmptyPosition(neighbor2.getAsInt()))
					return true;
			}
		}
		return false;
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
				model.positionsForClosingMill(color).filter(to -> model.canCloseMill(from, to, color)));
		if (millClosingPos.isPresent()) {
			reason("Ziehe an Position %d, weil eigene Mühle geschlossen wird", millClosingPos);
			return millClosingPos;
		}
		return randomElement(model.emptyNeighbors(from));
	}
}