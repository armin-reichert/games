package de.amr.games.muehle.rules.impl;

import static de.amr.games.muehle.util.Util.randomElement;

import java.util.OptionalInt;
import java.util.function.Function;

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
			player -> randomElement(player.getBoard().emptyPositions()),
			player -> player.getBoard().positions(player.getColor()).count() == 0),

	CLOSE_MILL(
			"Setze Stein auf Position %d, weil eigene Mühle geschlossen wird",
			player -> randomElement(player.getBoard().positionsClosingMill(player.getColor()))),

	DESTROY_MILL(
			"Setze Stein auf Position %d, weil gegnerische Mühle verhindert wird",
			player -> randomElement(player.getBoard().positionsClosingMill(player.getColor().other()))),

	OPEN_TWO_MILLS(
			"Setze Stein auf Position %d, weil zwei eigene Mühlen geöffnet werden",
			player -> randomElement(player.getBoard().positionsOpeningTwoMills(player.getColor()))),

	OPEN_ONE_MILL(
			"Setze Stein auf Position %d, weil eigene Mühle geöffnet wird",
			player -> randomElement(player.getBoard().positionsOpeningMill(player.getColor()))),

	NEAR_OWN_COLOR("Setze Stein auf Position %d, weil es eine freie Position neben eigenem Stein ist", player -> {
		OptionalInt emptyNeighborOfOwnColor = randomElement(
				player.getBoard().positions(player.getColor()).filter(player.getBoard()::hasEmptyNeighbor));
		return emptyNeighborOfOwnColor.isPresent()
				? randomElement(player.getBoard().emptyNeighbors(emptyNeighborOfOwnColor.getAsInt())) : OptionalInt.empty();
	}),

	RANDOM(
			"Setze Stein auf Position %d, weil kein Spezialfall zutraf",
			player -> randomElement(player.getBoard().emptyPositions()))

	;

	@Override
	public OptionalInt supplyPlacingPosition(Player player) {
		return condition.apply(player) ? positionSupplier.apply(player) : OptionalInt.empty();
	}

	@Override
	public String getDescription() {
		return description;
	}

	private PlacingRules(String description, Function<Player, OptionalInt> positionSupplier,
			Function<Player, Boolean> condition) {
		this.description = description;
		this.positionSupplier = positionSupplier;
		this.condition = condition;
	}

	private PlacingRules(String description, Function<Player, OptionalInt> positionSupplier) {
		this(description, positionSupplier, player -> true);
	}

	private final String description;
	private final Function<Player, OptionalInt> positionSupplier;
	private final Function<Player, Boolean> condition;
}