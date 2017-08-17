package de.amr.games.muehle.player;

import static de.amr.games.muehle.util.Util.randomElement;

import java.util.OptionalInt;

/**
 * Enumerates some placing rules.
 * 
 * @author Armin Reichert
 */
public enum PlacingRule {

	RANDOM_POSITION_BOARD_EMPTY(
			new PositionSupplyRule("Setze Stein auf Position %d, weil noch kein Stein meiner Farbe gesetzt wurde",
					(board, color) -> randomElement(board.positions().filter(board::isEmptyPosition)),
					(board, color) -> board.positions(color).count() == 0)),

	CLOSE_OWN_MILL(
			new PositionSupplyRule("Setze Stein auf Position %d, weil eigene Mühle geschlossen wird",
					(board, color) -> randomElement(board.positionsClosingMill(color)))),

	DESTROY_OPPONENT_MILL(
			new PositionSupplyRule("Setze Stein auf Position %d, weil gegnerische Mühle verhindert wird",
					(board, color) -> randomElement(board.positionsClosingMill(color.other())))),

	OPEN_TWO_OWN_MILLS(
			new PositionSupplyRule("Setze Stein auf Position %d, weil zwei eigene Mühlen geöffnet werden",
					(board, color) -> randomElement(board.positionsOpeningTwoMills(color)))),

	OPEN_OWN_MILL(
			new PositionSupplyRule("Setze Stein auf Position %d, weil eigene Mühle geöffnet wird",
					(board, color) -> randomElement(board.positionsOpeningMill(color)))),

	FREE_POSITION_NEARBY_OWN_COLOR(
			new PositionSupplyRule("Setze Stein auf Position %d, weil es eine freie Position neben eigenem Stein ist",
					(board, color) -> {
						OptionalInt posWithEmptyNeighbor = randomElement(board.positions(color).filter(board::hasEmptyNeighbor));
						if (posWithEmptyNeighbor.isPresent()) {
							return randomElement(board.emptyNeighbors(posWithEmptyNeighbor.getAsInt()));
						}
						return OptionalInt.empty();
					})),

	RANDOM_FREE_POSITION(
			new PositionSupplyRule("Setze Stein auf Position %d, weil kein Spezialfall zutraf",
					(board, color) -> randomElement(board.positions().filter(board::isEmptyPosition))));

	public PositionSupplyRule getRule() {
		return rule;
	}

	private PlacingRule(PositionSupplyRule rule) {
		this.rule = rule;
	}

	private PositionSupplyRule rule;

}
