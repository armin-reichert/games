package de.amr.samples.marbletoy.router;

import static de.amr.easy.game.math.Vector2.diff;
import static de.amr.samples.marbletoy.router.RoutingPoint.A;
import static de.amr.samples.marbletoy.router.RoutingPoint.B;
import static de.amr.samples.marbletoy.router.RoutingPoint.C;
import static de.amr.samples.marbletoy.router.RoutingPoint.D;
import static de.amr.samples.marbletoy.router.RoutingPoint.E;
import static de.amr.samples.marbletoy.router.RoutingPoint.F;
import static de.amr.samples.marbletoy.router.RoutingPoint.G;
import static de.amr.samples.marbletoy.router.RoutingPoint.H;
import static de.amr.samples.marbletoy.router.RoutingPoint.Initial;
import static de.amr.samples.marbletoy.router.RoutingPoint.X1;
import static de.amr.samples.marbletoy.router.RoutingPoint.X2;
import static de.amr.samples.marbletoy.router.RoutingPoint.X3;

import java.util.EnumMap;
import java.util.Map;

import de.amr.easy.fsm.FSM;
import de.amr.easy.fsm.FSMState;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2;
import de.amr.samples.marbletoy.entities.MarbleToy;

public class MarbleRouter extends FSM<RoutingPoint, Character> {

	private static final float MARBLE_SPEED = 1.5f;

	private final MarbleToy toy;
	private final GameEntity marble;

	@Override
	protected Map<RoutingPoint, FSMState<RoutingPoint, Character>> createStateMap() {
		return new EnumMap<>(RoutingPoint.class);
	}

	public MarbleRouter(MarbleToy toy) {
		this.toy = toy;
		this.marble = toy.getMarble();
		/*@formatter:off*/
		beginFSM()
			.description("Marble Router")
			.acceptedEvents('A', 'B', '*')
			.defaultEvent('*')
			.initialState(Initial)
			.state(Initial)
				.into(A).on('A').act(() -> placeMarbleCenteredAt(A))
				.into(B).on('B').act(() -> placeMarbleCenteredAt(B))
			.end()
			.state(A)
				.entering(() -> route(A, X1))
				.into(X1).when(() -> atLever(0))
				.keep().act(() -> marble.update())
			.end()
			.state(B)
				.entering(() -> route(B, X2))
				.into(X2).when(() -> atLever(1))
				.keep().act(() -> marble.update())
			.end()
			.state(X1)
				.entering(() -> route(X1, toy.getLever(0).pointsLeft() ? E : X3))
				.into(E).when(() -> at(E.location))
				.into(X3).when(() -> atLever(2))
				.keep().act(() -> marble.update())
			.end()
			.state(X2)
				.entering(() -> route(X2, toy.getLever(1).pointsLeft() ? X3 : F))
				.into(X3).when(() -> atLever(2))
				.into(F).when(() -> at(F.location))
				.keep().act(() -> marble.update())
			.end()
			.state(E)
				.entering(() -> route(E, G))
				.into(G).when(() -> at(G.location))
				.keep().act(() -> marble.update())
			.end()
			.state(X3)
				.entering(() -> route(X3, toy.getLever(2).pointsLeft() ? G : H))
				.into(G).when(() -> at(G.location))
				.into(H).when(() -> at(H.location))
				.keep().act(() -> marble.update())
			.end()
			.state(F)
				.entering(() -> route(F, H))
				.into(H).when(() -> at(H.location))
				.keep().act(() -> marble.update())
			.end()
			.state(G)
				.entering(() -> route(G, C))
				.into(C).when(() -> at(C.location))
				.keep().act(() -> marble.update())
			.end()
			.state(H)
				.entering(() -> route(H, D))
				.into(D).when(() -> at(D.location))
				.keep().act(() -> marble.update())
			.end()
			.state(C).keep().end()
			.state(D).keep().end()
		.endFSM();
		/*@formatter:on*/
	}

	private void placeMarbleCenteredAt(RoutingPoint p) {
		marble.tr.moveTo(p.location.x - marble.getWidth() / 2, p.location.y - marble.getHeight() / 2);
	}

	private void route(RoutingPoint from, RoutingPoint to) {
		placeMarbleCenteredAt(from);
		marble.tr.setVel(diff(to.location, from.location).normalize().times(MARBLE_SPEED));
	}

	private boolean atLever(int leverIndex) {
		return marble.getCollisionBox().intersects(toy.getLever(leverIndex).getCollisionBox());
	}

	private boolean at(Vector2 location) {
		return marble.getCollisionBox().contains(location.roundedX(), location.roundedY());
	}
}