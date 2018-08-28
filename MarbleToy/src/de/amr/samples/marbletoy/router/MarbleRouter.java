package de.amr.samples.marbletoy.router;

import static de.amr.easy.game.math.Vector2f.diff;
import static de.amr.easy.game.math.Vector2f.smul;
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

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.statemachine.StateMachine;
import de.amr.samples.marbletoy.entities.MarbleToy;

public class MarbleRouter extends StateMachine<RoutingPoint, Character> {

	private static final float MARBLE_SPEED = 1.5f;

	private final MarbleToy toy;
	private final GameEntity marble;

	public MarbleRouter(MarbleToy toy) {
		super("Marble Router", RoutingPoint.class, RoutingPoint.Initial);

		this.toy = toy;
		this.marble = toy.getMarble();

		changeOnInput('A', Initial, A, t -> placeMarbleCenteredAt(A));
		changeOnInput('B', Initial, B, t -> placeMarbleCenteredAt(B));

		state(A).entry = s -> routeMarble(A, X1);
		state(A).update = s -> marble.update();
		change(A, X1, () -> isMarbleAtLever(0));

		state(B).entry = s -> routeMarble(B, X2);
		state(B).update = s -> marble.update();
		change(B, X2, () -> isMarbleAtLever(1));

		state(X1).entry = s -> routeMarble(X1, toy.getLever(0).pointsLeft() ? E : X3);
		state(X1).update = s -> marble.update();
		change(X1, E, () -> isMarbleAt(E));
		change(X1, X3, () -> isMarbleAtLever(2));

		state(X2).entry = s -> routeMarble(X2, toy.getLever(1).pointsLeft() ? X3 : F);
		state(X2).update = s -> marble.update();
		change(X2, X3, () -> isMarbleAtLever(2));
		change(X2, F, () -> isMarbleAt(F));

		state(X3).entry = s -> routeMarble(X3, toy.getLever(2).pointsLeft() ? G : H);
		state(X3).update = s -> marble.update();
		change(X3, G, () -> isMarbleAt(G));
		change(X3, H, () -> isMarbleAt(H));

		state(E).entry = s -> routeMarble(E, G);
		state(E).update = s -> marble.update();
		change(E, G, () -> isMarbleAt(G));

		state(F).entry = s -> routeMarble(F, H);
		state(F).update = s -> marble.update();
		change(F, H, () -> isMarbleAt(H));

		state(G).entry = s -> routeMarble(G, C);
		state(G).update = s -> marble.update();
		change(G, C, () -> isMarbleAt(C));

		state(H).entry = s -> routeMarble(H, D);
		state(H).update = s -> marble.update();
		change(H, D, () -> isMarbleAt(D));
	}

	private void placeMarbleCenteredAt(RoutingPoint p) {
		marble.tf().moveTo(p.getLocation().x - marble.getWidth() / 2, p.getLocation().y - marble.getHeight() / 2);
	}

	private void routeMarble(RoutingPoint from, RoutingPoint to) {
		placeMarbleCenteredAt(from);
		marble.tf().setVelocity(smul(MARBLE_SPEED, diff(to.getLocation(), from.getLocation()).normalized()));
	}

	private boolean isMarbleAtLever(int leverIndex) {
		Vector2f leverLocation = toy.getLever(leverIndex).getCenter();
		return marble.getCollisionBox().contains(leverLocation.roundedX(), leverLocation.roundedY());
	}

	private boolean isMarbleAt(RoutingPoint point) {
		Vector2f pointLocation = point.getLocation();
		return marble.getCollisionBox().contains(pointLocation.roundedX(), pointLocation.roundedY());
	}
}