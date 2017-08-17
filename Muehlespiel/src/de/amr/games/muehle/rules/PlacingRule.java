package de.amr.games.muehle.rules;

import static de.amr.games.muehle.util.Util.randomElement;

import java.util.OptionalInt;
import java.util.function.BiFunction;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;

/**
 * Enumerates some placing rules.
 * 
 * @author Armin Reichert
 */
public enum PlacingRule implements PositionSelectionRule {

	RANDOM_POSITION_BOARD_EMPTY(
			"Setze Stein auf Position %d, weil noch kein Stein meiner Farbe gesetzt wurde",
			(board, color) -> randomElement(board.positions().filter(board::isEmptyPosition)),
			(board, color) -> board.positions(color).count() == 0),

	CLOSE_OWN_MILL(
			"Setze Stein auf Position %d, weil eigene Mühle geschlossen wird",
			(board, color) -> randomElement(board.positionsClosingMill(color))),

	DESTROY_OPPONENT_MILL(
			"Setze Stein auf Position %d, weil gegnerische Mühle verhindert wird",
			(board, color) -> randomElement(board.positionsClosingMill(color.other()))),

	OPEN_TWO_OWN_MILLS(
			"Setze Stein auf Position %d, weil zwei eigene Mühlen geöffnet werden",
			(board, color) -> randomElement(board.positionsOpeningTwoMills(color))),

	OPEN_OWN_MILL(
			"Setze Stein auf Position %d, weil eigene Mühle geöffnet wird",
			(board, color) -> randomElement(board.positionsOpeningMill(color))),

	FREE_POSITION_NEARBY_OWN_COLOR(
			"Setze Stein auf Position %d, weil es eine freie Position neben eigenem Stein ist",
			(board, color) -> {
				OptionalInt posWithEmptyNeighbor = randomElement(board.positions(color).filter(board::hasEmptyNeighbor));
				if (posWithEmptyNeighbor.isPresent()) {
					return randomElement(board.emptyNeighbors(posWithEmptyNeighbor.getAsInt()));
				}
				return OptionalInt.empty();
			}),

	RANDOM_FREE_POSITION(
			"Setze Stein auf Position %d, weil kein Spezialfall zutraf",
			(board, color) -> randomElement(board.positions().filter(board::isEmptyPosition))),

	;

	@Override
	public OptionalInt selectPosition(Board board, StoneColor color) {
		return condition.apply(board, color) ? positionSupplier.apply(board, color) : OptionalInt.empty();
	}

	@Override
	public String getDescription() {
		return description;
	}

	private PlacingRule(String description, BiFunction<Board, StoneColor, OptionalInt> placingPositionSupplier,
			BiFunction<Board, StoneColor, Boolean> condition) {
		this.description = description;
		this.positionSupplier = placingPositionSupplier;
		this.condition = condition;
	}

	private PlacingRule(String description, BiFunction<Board, StoneColor, OptionalInt> placingPositionSupplier) {
		this(description, placingPositionSupplier, (board, color) -> true);
	}

	private final String description;
	private final BiFunction<Board, StoneColor, OptionalInt> positionSupplier;
	private final BiFunction<Board, StoneColor, Boolean> condition;
}
