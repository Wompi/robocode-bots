package wompi.teststuff;

import robocode.Rules;
import robocode.util.Utils;

/**
 * My attempt to make a very very small modern precise predictor
 * Codesize: 168 bytes
 * @author Chase
 */
public class NanoSim
{
	public static void main(String[] args)
	{
		double angle = 1.6574625364;
		NanoSimWompi.x = 439.9389873050;
		NanoSimWompi.y = 569.7187620753;
		NanoSimWompi.h = 3.3954276057;
		NanoSimWompi.v = 0.0000000000;
		double maxv = 6.8127032896;
		int ticks = 50;

		double dir = Math.signum(angle);
		if (Math.abs(Math.cos(angle)) > 0.5)
		{
			dir = -dir;
		}

		System.out.format("angle=%10.4f head=%10.4f cos=%10.4f \n", Math.toDegrees(angle), Math.toDegrees(NanoSimWompi.h), Math.cos(angle));
		for (int i = 0; i < ticks; i++)
		{
			angle = NanoSimWompi.simulate(angle, dir, maxv);
			System.out.format("[%d] %11.4f %11.4f %11.4f %11.4f maxTurn=%10.4f\n", i, NanoSimWompi.x, NanoSimWompi.y, NanoSimWompi.h, NanoSimWompi.v,
					Rules.getTurnRateRadians(NanoSimWompi.v));
		}

		System.out.format("-------------------\n");
	}
}

class NanoSimWompi
{
	public static double	x, y, h, v;

	public static double simulate(double att, double d, double maxv)
	{
		if (d == 0) d = Math.signum(v);
		if (v * d < 0) d *= 2;
		if (((d = v + d) * v) < 0) d /= 2.0;

		double r = limit(Rules.getTurnRateRadians(v), att);
		x += Math.sin(h = Utils.normalNearAbsoluteAngle(h + r)) * (v = limit(maxv, d));
		y += Math.cos(h) * v;
		return att - r;
	}

	private static double limit(double minmax, double value)
	{
		System.out.format("minmax=%10.4f value=%10.4f\n", minmax, value);
		return Math.max(-minmax, Math.min(value, minmax));
	}
}

//class NanoSimOrig
//{
//	public double	x, y, heading, velocity;
//
//	public void simulate(double angleToTurn, int direction)
//	{
//		double turnRate = Rules.getTurnRateRadians(Math.abs(velocity));
//		heading = Utils.normalNearAbsoluteAngle(heading + Math.max(-turnRate, Math.min(angleToTurn, turnRate)));
//		if (direction == 0) direction = -(int) Math.signum(velocity);
//		double nvelocity = velocity + ((velocity * direction < 0) ? Rules.DECELERATION * direction : Rules.ACCELERATION * direction);
//		if (nvelocity * velocity < 0) nvelocity /= 2.0;
//		velocity = Math.max(-Rules.MAX_VELOCITY, Math.min(nvelocity, Rules.MAX_VELOCITY));
//		x += Math.sin(heading) * velocity;
//		y += Math.cos(heading) * velocity;
//	}
//}
