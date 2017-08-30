package de.amr.games.muehle.rules.impl;

import static de.amr.games.muehle.util.Util.randomElement;

import java.util.OptionalInt;
import java.util.function.BiFunction;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.player.api.Player;
import de.amr.games.muehle.rules.api.PlacingRule;

/**
 * Enumerates some placing rules.
 * 
 * @author Armin Reichert
 */
public enum PlacingRules implements PlacingRule {

	FIRST_STONE_RANDOM(
			"Setze Stein auf Position %d, weil noch kein Stein meiner Farbe gesetzt wurde",
			(board, player) -> randomElement(board.emptyPositions()),
			(board, player) -> board.positions(player.getColor()).count() == 0),

	CLOSE_MILL(
			"Setze Stein auf Position %d, weil eigene Mühle geschlossen wird",
			(board, player) -> randomElement(board.positionsClosingMill(player.getColor()))),

	DESTROY_MILL(
			"Setze Stein auf Position %d, weil gegnerische Mühle verhindert wird",
			(board, player) -> randomElement(board.positionsClosingMill(player.getColor().other()))),

	OPEN_TWO_MILLS(
			"Setze Stein auf Position %d, weil zwei eigene Mühlen geöffnet werden",
			(board, player) -> randomElement(board.positionsOpeningTwoMills(player.getColor()))),

	OPEN_ONE_MILL(
			"Setze Stein auf Position %d, weil eigene Mühle geöffnet wird",
			(board, player) -> randomElement(board.positionsOpeningMill(player.getColor()))),

	NEAR_OWN_COLOR(
			"Setze Stein auf Position %d, weil es eine freie Position neben eigenem Stein ist",
			(board, player) -> {
				OptionalInt emptyNeighborOfOwnColor = randomElement(
						board.positions(player.getColor()).filter(board::hasEmptyNeighbor));
				return emptyNeighborOfOwnColor.isPresent()
						? randomElement(board.emptyNeighbors(emptyNeighborOfOwnColor.getAsInt())) : OptionalInt.empty();
			}),

	RANDOM(
			"Setze Stein auf Position %d, weil kein Spezialfall zutraf",
			(board, player) -> randomElement(board.emptyPositions()))

	;

	@Override
	public OptionalInt supplyPlacingPosition(Player player) {
		return condition.apply(player.getBoard(), player) ? positionSupplier.apply(player.getBoard(), player)
				: OptionalInt.empty();
	}

	@Override
	public String getDescription() {
		return description;
	}

	private PlacingRules(String description, BiFunction<Board, Player, OptionalInt> positionSupplier,
			BiFunction<Board, Player, Boolean> condition) {
		this.description = description;
		this.positionSupplier = positionSupplier;
		this.condition = condition;
	}

	private PlacingRules(String description, BiFunction<Board, Player, OptionalInt> positionSupplier) {
		this(description, positionSupplier, (board, player) -> true);
	}

	private final String description;
	private final BiFunction<Board, Player, OptionalInt> positionSupplier;
	private final BiFunction<Board, Player, Boolean> condition;
}