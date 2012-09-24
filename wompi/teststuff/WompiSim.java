package wompi.teststuff;

import robocode.Rules;
import robocode.util.Utils;

public class WompiSim
{
	public static double	x, y, h, v;

	public static void simulate(double att, double d, double maxv)
	{
		if (d == 0) d = Math.signum(v);
		if (v * d < 0) d *= 2;
		if (((d = v + d) * v) < 0) d /= 2.0;

		x += Math.sin(h = Utils.normalNearAbsoluteAngle(h + limit(Rules.getTurnRateRadians(v), Utils.normalRelativeAngle(att))))
				* (v = limit(maxv, d));
		y += Math.cos(h) * v;
	}

	private static double limit(double minmax, double value)
	{
		//System.out.format("minmax=%10.4f value=%10.4f\n", minmax, value);
		return Math.max(-minmax, Math.min(value, minmax));
	}

}
