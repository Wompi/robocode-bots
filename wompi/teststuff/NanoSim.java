package wompi.teststuff;

import robocode.Rules;

/**
 * My attempt to make a very very small modern precise predictor Codesize: 168 bytes
 * 
 * @author Chase
 */
public class NanoSim
{
	public static void main(String[] args)
	{
		double angle = 1.6574625364;
		WompiSim.x = 439.9389873050;
		WompiSim.y = 569.7187620753;
		WompiSim.h = 3.3954276057;
		WompiSim.v = 0.0000000000;
		double maxv = 6.8127032896;
		int ticks = 50;

		double dir = Math.signum(angle);

		if (Math.abs(Math.cos(angle)) > 0.5)
		{
			dir = -dir;
		}

		System.out.format("angle=%10.4f head=%10.4f cos=%10.4f \n", Math.toDegrees(angle), Math.toDegrees(WompiSim.h),
				Math.cos(angle));
		for (int i = 0; i < ticks; i++)
		{
			WompiSim.simulate(angle, dir, maxv);
			System.out.format("[%d] %11.4f %11.4f %11.4f %11.4f maxTurn=%10.4f\n", i, WompiSim.x, WompiSim.y,
					WompiSim.h, WompiSim.v, Rules.getTurnRateRadians(WompiSim.v));
		}

		System.out.format("-------------------\n");
	}
}

// class NanoSimOrig
// {
// public double x, y, heading, velocity;
//
// public void simulate(double angleToTurn, int direction)
// {
// double turnRate = Rules.getTurnRateRadians(Math.abs(velocity));
// heading = Utils.normalNearAbsoluteAngle(heading + Math.max(-turnRate,
// Math.min(angleToTurn, turnRate)));
// if (direction == 0) direction = -(int) Math.signum(velocity);
// double nvelocity = velocity + ((velocity * direction < 0) ?
// Rules.DECELERATION * direction : Rules.ACCELERATION * direction);
// if (nvelocity * velocity < 0) nvelocity /= 2.0;
// velocity = Math.max(-Rules.MAX_VELOCITY, Math.min(nvelocity,
// Rules.MAX_VELOCITY));
// x += Math.sin(heading) * velocity;
// y += Math.cos(heading) * velocity;
// }
// }
