package wompi.irukandji;

import robocode.Rules;
import robocode.util.Utils;

public class IrukandjiSimPoints
{
	public IrukandjiSimPoints()
	{

	}

	/**
	 * This method should generate all possible points for the current movement. First generate all forward points till the max angle is not changing
	 * anymore. Then transpose these points to the other side and make this for the backward points also
	 * 
	 * - p0-p(a) - till maxAngle for the last point is maxTurn - p0-p(b) - p(b).y = -p(a).y new points for the opposite side
	 */
	private void calculateSimPoints()
	{
		int TICKS = 60; // TODO calculate the max ticks for the map
		int t = 0;

		while (true)
		{
			// initialize with combat data
			double x = 500;
			double y = 500;
			double h = 0;
			double v = 0;

			while (t++ <= TICKS)
			{
				if (d == 0) d = Math.signum(v);
				if (v * d < 0) d *= 2;
				if (((d = v + d) * v) < 0) d /= 2.0;

				double maxTurn = Rules.getTurnRateRadians(v);
				double deltaHead = limit(maxTurn, Utils.normalRelativeAngle(att));
				h = Utils.normalNearAbsoluteAngle(h + deltaHead);
				v = limit(maxv, d);
				x += Math.sin(h) * v;
				y += Math.cos(h) * v;

			}
		}
	}

	private double limit(double minmax, double value)
	{
		return Math.max(-minmax, Math.min(value, minmax));
	}

}
