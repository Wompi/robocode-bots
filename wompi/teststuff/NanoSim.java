package wompi.teststuff;

import robocode.Rules;
import robocode.util.Utils;
import wompi.teststuff.NatSim.PredictionStatus;

/**
 * My attempt to make a very very small modern precise predictor
 * Codesize: 168 bytes
 * @author Chase
 */
public class NanoSim
{
	public static void main(String[] args)
	{
		double angle = Math.toRadians(40);
		double direction = -1;

		NanoSimWompi.v = 8.0;
		for (int i = 0; i < 20; i++)
		{
			NanoSimWompi.simulate(angle, direction, 6);
			System.out.format("[%d] %11.5f %11.5f %11.5f %11.5f\n", i, NanoSimWompi.x, NanoSimWompi.y, NanoSimWompi.h, NanoSimWompi.v);
		}

		System.out.format("-------------------\n");

		int dir = (int) direction;
		NanoSimOrig ns = new NanoSimOrig();
		//		ns.velocity = 8.0;
		for (int i = 0; i < 10; i++)
		{
			ns.simulate(angle, dir);
			System.out.format("[%d] %11.5f %11.5f %11.5f %11.5f\n", i, ns.x, ns.y, ns.heading, ns.velocity);
		}

		System.out.format("-------------------\n");

		PredictionStatus depp = new NatSim.PredictionStatus(0, 0, 0, 0.0, 0);
		for (int i = 0; i < 10; i++)
		{
			depp = NatSim.predict(depp, angle);
			System.out.format("[%d] %11.5f %11.5f %11.5f %11.5f\n", i, depp.x, depp.y, depp.heading, depp.velocity);
		}

	}
}

class NanoSimWompi
{
	public static double	x, y, h, v;

	public static void simulate(double att, double d, double maxv)
	{
		if (d == 0) d = -Math.signum(v);
		if (v * d < 0) d *= 2;
		if (((d = v + d) * v) < 0) d /= 2.0;
		x += Math.sin(h = Utils.normalNearAbsoluteAngle(h + limit(Rules.getTurnRateRadians(v), att))) * (v = limit(maxv, d));
		y += Math.cos(h) * v;
	}

	private static double limit(double minmax, double value)
	{
		return Math.max(-minmax, Math.min(value, minmax));
	}

}

class NanoSimOrig
{
	public double	x, y, heading, velocity;

	public void simulate(double angleToTurn, int direction)
	{
		double turnRate = Rules.getTurnRateRadians(Math.abs(velocity));
		heading = Utils.normalNearAbsoluteAngle(heading + Math.max(-turnRate, Math.min(angleToTurn, turnRate)));
		if (direction == 0) direction = -(int) Math.signum(velocity);
		double nvelocity = velocity + ((velocity * direction < 0) ? Rules.DECELERATION * direction : Rules.ACCELERATION * direction);
		if (nvelocity * velocity < 0) nvelocity /= 2.0;
		velocity = Math.max(-Rules.MAX_VELOCITY, Math.min(nvelocity, Rules.MAX_VELOCITY));
		x += Math.sin(heading) * velocity;
		y += Math.cos(heading) * velocity;
	}
}
